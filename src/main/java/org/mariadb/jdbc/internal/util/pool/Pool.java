/*
 *
 * MariaDB Client for Java
 *
 * Copyright (c) 2012-2014 Monty Program Ab.
 * Copyright (c) 2015-2017 MariaDB Ab.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to Monty Program Ab info@montyprogram.com.
 *
 */

package org.mariadb.jdbc.internal.util.pool;

import org.mariadb.jdbc.MariaDbConnection;
import org.mariadb.jdbc.MariaDbPooledConnection;
import org.mariadb.jdbc.UrlParser;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.mariadb.jdbc.internal.protocol.Protocol;
import org.mariadb.jdbc.internal.util.Options;
import org.mariadb.jdbc.internal.util.Utils;
import org.mariadb.jdbc.internal.util.exceptions.ExceptionMapper;
import org.mariadb.jdbc.internal.util.scheduler.MariaDbThreadFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Pool implements AutoCloseable, PoolMBean {

    private static final Logger logger = LoggerFactory.getLogger(Pool.class);

    private static final int STATE_IDLE = 0;
    private static final int STATE_IN_USE = 1;
    private static final int STATE_CLOSE = 2;
    private static final int STATE_RESETTING = 3;

    private static final int POOL_STATE_OK = 0;
    private static final int POOL_STATE_CLOSING = 1;

    private final AtomicInteger poolState = new AtomicInteger();

    private final UrlParser urlParser;
    private final Options options;
    private final AtomicInteger pendingRequestNumber = new AtomicInteger();
    private final CopyOnWriteArrayList<MariaDbPooledConnection> connections = new CopyOnWriteArrayList<>();
    private final ThreadPoolExecutor connectionRemover;
    private final ThreadPoolExecutor connectionAppender;
    private final BlockingQueue<Runnable> connectionAppenderQueue;
    private final String poolTag;
    private ScheduledThreadPoolExecutor idleConnectionChecker;
    private GlobalStateInfo globalInfo;

    private final Lock lock = new ReentrantLock();
    private final Condition hasIdleConnection = lock.newCondition();
    private int maxIdleTime;

    /**
     * Create pool from configuration.
     *
     * @param urlParser configuration parser
     * @param poolIndex pool index to permit distinction of thread name
     * @throws SQLException if any connection error occur.
     */
    public Pool(UrlParser urlParser, int poolIndex) throws SQLException {

        this.urlParser = urlParser;
        options = urlParser.getOptions();
        this.maxIdleTime = options.maxIdleTime;
        poolTag = generatePoolTag(poolIndex);

        //one thread to add new connection to pool.
        connectionAppenderQueue = new ArrayBlockingQueue<>(options.maxPoolSize);
        connectionAppender = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS,
                connectionAppenderQueue, new MariaDbThreadFactory(poolTag + "-appender"));
        connectionAppender.allowCoreThreadTimeOut(true);
        connectionAppender.prestartCoreThread(); //create workers, since only interacting with queue after that (i.e. not using .execute() )

        //one thread that will handle removal of connection exception and removing idle connection
        connectionRemover = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(options.maxPoolSize), new MariaDbThreadFactory(poolTag + "-remover"));
        connectionRemover.allowCoreThreadTimeOut(true);

        int scheduleDelay = Math.min(30, maxIdleTime / 2);
        idleConnectionChecker = new ScheduledThreadPoolExecutor(1, new MariaDbThreadFactory(poolTag + "-maxTimeoutIdle"));
        idleConnectionChecker.scheduleAtFixedRate(this::removeIdleTimeoutConnection, scheduleDelay, scheduleDelay, TimeUnit.SECONDS);

        if (options.registerJmxPool) {
            try {
                registerJmx();
            } catch (Exception ex) {
                logger.error("pool " + poolTag + " not registered due to exception : " + ex.getMessage());
            }
        }

        //create minimal connection in pool
        for ( int i = 0 ; i < options.minPoolSize; i++) {
            addConnectionRequest();
        }

    }

    /**
     * Checking connection that have not been used since more than option "maxIdleTime".
     * Close them and recreate connection to reach minimal number of connection.
     */
    private void removeIdleTimeoutConnection() {
        for (MariaDbPooledConnection item : connections) {

            if ((System.nanoTime() - item.getLastUsed().get()) > TimeUnit.SECONDS.toNanos(maxIdleTime)
                    && item.getState().compareAndSet(STATE_IDLE, STATE_CLOSE)) {

                connections.remove(item);
                silentCloseConnection(item);

                //recreate connection to reach minimal number of connection if needed
                addConnectionRequest();

                if (logger.isDebugEnabled()) {
                    logger.debug("pool {} connection removed due to inactivity (total:{}, active:{}, pending:{})",
                            poolTag, connections.size(), getActiveConnections(), pendingRequestNumber.get());
                }

            }
        }
    }

    /**
     * Get an existing connection in pool in IDLE state.
     *
     * @return an IDLE connection.
     */
    private MariaDbPooledConnection getIdleConnection() {
        for (MariaDbPooledConnection item : connections) {
            if (item.getState().compareAndSet(STATE_IDLE, STATE_IN_USE) && validateConnection(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Validate connection.
     *
     * @return True if connection is validated, or have been recently used.
     */
    private boolean validateConnection(MariaDbPooledConnection item) {
        MariaDbConnection connection = item.getConnection();

        try {
            if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - item.getLastUsed().get()) > options.poolValidMinDelay) {

                //validate connection
                if (connection.isValid(10)) { //10 seconds timeout
                    item.lastUsedToNow();
                    return true;
                }

            } else {

                // connection has been retrieve recently, avoid connection validation
                item.lastUsedToNow();
                return true;

            }

        } catch (SQLException sqle) {
            //eat
        }

        // validation failed
        asyncConnectionRemove(item);
        addConnectionRequest();

        if (logger.isDebugEnabled()) {
            logger.debug("pool {} connection removed from pool due to failed validation (total:{}, active:{}, pending:{})",
                    poolTag, connections.size(), getActiveConnections(), pendingRequestNumber.get());
        }
        return false;
    }

    /**
     * Async closing connection and removing from pool.
     *
     * @param item pooledItem
     */
    private void asyncConnectionRemove(MariaDbPooledConnection item) {

        item.getState().set(STATE_CLOSE);
        connections.remove(item);
        silentAbortConnection(item, connectionRemover);
    }



    /**
     * Add new connection if needed.
     * Only one thread create new connection, so new connection request will wait to newly created connection or
     * for a released connection.
     */
    private void addConnectionRequest() {

        if (connections.size() < options.maxPoolSize && poolState.get() == POOL_STATE_OK) {

            //ensure to have one worker if was timeout
            connectionAppender.prestartCoreThread();
            connectionAppenderQueue.offer(() -> {

                if ((connections.size() < options.minPoolSize || pendingRequestNumber.get() > 0) && connections.size() < options.maxPoolSize) {
                    try {
                        //TODO add verifier that ensure that a connection never hang, blocking pool indefinitely ?

                        //create new connection
                        Protocol protocol = Utils.retrieveProxy(urlParser, globalInfo);
                        MariaDbConnection connection = new MariaDbConnection(protocol);

                        if (options.staticGlobal) {
                            //on first connection load initial state
                            if (globalInfo == null) initializePoolGlobalState(connection);
                            //set default transaction isolation level to permit resetting to initial state
                            connection.setDefaultTransactionIsolation(globalInfo.getDefaultTransactionIsolation());
                        } else {
                            //set default transaction isolation level to permit resetting to initial state
                            connection.setDefaultTransactionIsolation(connection.getTransactionIsolation());
                        }

                        MariaDbPooledConnection pooledConnection = createPoolConnection(connection);

                        lock.lock();
                        try {
                            if (poolState.get() == POOL_STATE_OK) {
                                synchronized (connections) {
                                    if (connections.size() < options.maxPoolSize) {
                                        connections.add(pooledConnection);
                                        hasIdleConnection.signal();
                                    } else {
                                        silentCloseConnection(pooledConnection);
                                    }
                                }
                            } else {
                                silentCloseConnection(pooledConnection);
                            }
                        } finally {
                            lock.unlock();
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("pool {} new physical connection created (total:{}, active:{}, pending:{})",
                                    poolTag, connections.size(), getActiveConnections(), pendingRequestNumber.get());
                        }

                    } catch (SQLException sqle) {
                        //eat
                    }
                }
            });
        }
    }

    private void silentCloseConnection(MariaDbPooledConnection item) {
        try {
            item.close();
        } catch (SQLException ex) {
            //eat exception
        }
    }

    private void silentAbortConnection(MariaDbPooledConnection item, Executor executor) {
        try {
            item.abort(executor);
        } catch (SQLException ex) {
            //eat exception
        }
    }

    private MariaDbPooledConnection createPoolConnection(MariaDbConnection connection) {
        MariaDbPooledConnection pooledConnection = new MariaDbPooledConnection(connection);
        pooledConnection.addConnectionEventListener(new ConnectionEventListener() {

            @Override
            public void connectionClosed(ConnectionEvent event) {
                returnConnectionToPool((MariaDbPooledConnection) event.getSource());
            }

            @Override
            public void connectionErrorOccurred(ConnectionEvent event) {
                asyncConnectionRemove((MariaDbPooledConnection) event.getSource());
                logger.debug("connection removed from pool {} due to having throw a Connection exception", poolTag);
            }

        });
        return pooledConnection;
    }

    private void returnConnectionToPool(MariaDbPooledConnection item) {
        if (item.getState().compareAndSet(STATE_IN_USE, STATE_RESETTING)) {
            try {
                item.getConnection().reset();
                lock.lock();
                try {
                    item.getState().set(STATE_IDLE);
                    hasIdleConnection.signal();
                } finally {
                    lock.unlock();
                }

            } catch (SQLException sqle) {
                //sql exception during reset, removing connection from pool
                asyncConnectionRemove(item);
                logger.debug("connection removed from pool {} due to error during reset", poolTag);
                addConnectionRequest();
            }
        }
    }

    /**
     * Retrieve new connection.
     * If possible return idle connection,
     * if not, stack connection query, ask for a connection creation,
     * and loop until a connection become idle / a new connection is created.
     *
     * @return a connection object
     * @throws SQLException if no connection is created when reaching timeout (connectTimeout option)
     **/
    public MariaDbConnection getConnection() throws SQLException {

        MariaDbPooledConnection pooledConnection;

        //try to get one idle connection
        if (pendingRequestNumber.get() == 0) {
            pooledConnection = getIdleConnection();
            if (pooledConnection != null) {
                logger.debug("idle connection retrieved from pool {}", poolTag);
                return pooledConnection.getConnection();
            }
        }

        //increment pending request
        pendingRequestNumber.incrementAndGet();

        long timeoutValue = TimeUnit.MILLISECONDS.toNanos(options.connectTimeout) + System.nanoTime();

        // ask for new connection creation if max is not reached
        addConnectionRequest();

        lock.lock();
        try {
            do {
                //will wait until there is some Idle connections.
                //release is done in fifo order
                while (getIdleConnections() == 0) {
                    if (hasIdleConnection.awaitNanos(timeoutValue - System.nanoTime()) < 0) {
                        throw ExceptionMapper.connException("No connection available within the specified time "
                                        + "(option 'connectTimeout': " + NumberFormat.getInstance().format(options.connectTimeout) + " ms)");
                    }
                }

                if ((pooledConnection = getIdleConnection()) != null) {
                    logger.debug("new connection retrieved from pool {}", poolTag);
                    return pooledConnection.getConnection();
                }
            } while (timeoutValue - System.nanoTime() < 0); //must normally never loop

            throw ExceptionMapper.connException("No connection available within the specified time "
                            + "(option 'connectTimeout': " + NumberFormat.getInstance().format(options.connectTimeout) + " ms)");

        } catch (InterruptedException interrupted) {
            throw ExceptionMapper.connException("Thread was interrupted", interrupted);
        } finally {
            lock.unlock();
            pendingRequestNumber.decrementAndGet();
        }

    }

    /**
     * Get new connection from pool if user and password correspond to pool.
     * If username and password are different from pool, will return a dedicated connection.
     *
     * @param username username
     * @param password password
     * @return connection
     * @throws SQLException if any error occur during connection
     */
    public MariaDbConnection getConnection(String username, String password) throws SQLException {

        try {

            if ((urlParser.getUsername() != null ? urlParser.getUsername().equals(username) : username == null)
                && (urlParser.getPassword() != null ? urlParser.getPassword().equals(password) : password == null)) {
                return getConnection();
            }

            UrlParser tmpUrlParser = (UrlParser) urlParser.clone();
            tmpUrlParser.setUsername(username);
            tmpUrlParser.setPassword(password);
            Protocol protocol = Utils.retrieveProxy(tmpUrlParser, globalInfo);
            return new MariaDbConnection(protocol);

        } catch (CloneNotSupportedException cloneException) {
            //cannot occur
            throw new SQLException("Error getting connection, parameters cannot be cloned", cloneException);
        }
    }

    private String generatePoolTag(int poolIndex) {
        if (options.poolName == null) {
            options.poolName = "MariaDB-pool";
        }
        return options.poolName + "-" + poolIndex;
    }

    public UrlParser getUrlParser() {
        return urlParser;
    }

    /**
     * Close pool and underlying connections.
     *
     * @throws InterruptedException if interrupted
     */
    public void close() throws InterruptedException {
        synchronized (this) {
            Pools.remove(this);
            poolState.set(POOL_STATE_CLOSING);
            connectionAppender.shutdown();

            //increase executor thread number to ensure that remaining connection are closed
            connectionRemover.setMaximumPoolSize(options.maxPoolSize);

            //loop for up to 10 seconds to close not used connection
            long start = System.nanoTime();
            do {

                for (MariaDbPooledConnection item : connections) {
                    if (item.getState().get() == STATE_CLOSE || item.getState().compareAndSet(STATE_IDLE, STATE_CLOSE)) {
                        //remove is done in background thread
                        connections.remove(item);
                        silentAbortConnection(item, connectionRemover);
                    }
                }

                if (!connections.isEmpty()) {
                    Thread.sleep(0, 10_00);
                }

            } while (!connections.isEmpty() && TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) < 10_000);

            //after having wait for 10 seconds, force removal, even if used
            if (!connections.isEmpty()) {
                for (MariaDbPooledConnection item : connections) {
                    //remove is done in background thread
                    connections.remove(item);
                    silentAbortConnection(item, connectionRemover);
                }
            }

            connectionRemover.shutdown();

            try {
                unRegisterJmx();
            } catch (Exception exeption) {
                //eat
            }

            //If there are still used connection after 10 seconds loop, just detach them from pool,
            //so that close is really closing connection
            for (MariaDbPooledConnection item : connections) {
                item.getConnection().pooledConnection = null;
            }

            connectionAppender.awaitTermination(10, TimeUnit.SECONDS);
            connectionRemover.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    private void initializePoolGlobalState(MariaDbConnection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT @@max_allowed_packet,"
                            + "@@wait_timeout,"
                            + "@@autocommit,"
                            + "@@auto_increment_increment,"
                            + "@@time_zone,"
                            + "@@system_time_zone,"
                            + "@@tx_isolation")) {

                rs.next();

                int transactionIsolation = Utils.transactionFromString(rs.getString(7)); // tx_isolation

                globalInfo = new GlobalStateInfo(
                        rs.getLong(1),      // max_allowed_packet
                        rs.getInt(2),       // wait_timeout
                        rs.getBoolean(3),   // autocommit
                        rs.getInt(4),       // autoIncrementIncrement
                        rs.getString(5),    // time_zone
                        rs.getString(6),    // system_time_zone
                        transactionIsolation);

                //ensure that the options "maxIdleTime" is not > to server wait_timeout
                //removing 45s since scheduler check  status every 30s
                maxIdleTime = Math.min(options.maxIdleTime, globalInfo.getWaitTimeout() - 45);
            }
        }
    }

    public String getPoolTag() {
        return poolTag;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Pool pool = (Pool) obj;

        return poolTag.equals(pool.poolTag);
    }

    @Override
    public int hashCode() {
        return poolTag.hashCode();
    }

    public GlobalStateInfo getGlobalInfo() {
        return globalInfo;
    }

    @Override
    public long getActiveConnections() {
        return connections.stream().filter((item) -> item.getState().get() == STATE_IN_USE).count();
    }

    @Override
    public long getTotalConnections() {
        return connections.size();
    }

    @Override
    public long getIdleConnections() {
        return connections.stream().filter((item) -> item.getState().get() == STATE_IDLE).count();
    }

    public long getConnectionRequests() {
        return pendingRequestNumber.get();
    }


    private void registerJmx() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String jmxName = poolTag.replace(":", "_");
        ObjectName name = new ObjectName("org.mariadb.jdbc.pool:type=" + jmxName);

        if (!mbs.isRegistered(name)) mbs.registerMBean(this, name);
    }

    private void unRegisterJmx() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String jmxName = poolTag.replace(":", "_");
        ObjectName name = new ObjectName("org.mariadb.jdbc.pool:type=" + jmxName);

        if (mbs.isRegistered(name)) mbs.unregisterMBean(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pool{url=").append(urlParser.getInitialUrl()).append(", connections={");


        final NumberFormat numberFormat = DecimalFormat.getInstance();
        for (MariaDbPooledConnection pooledConnection : connections) {
            sb.append(" Conn:").append(pooledConnection.getConnection().getServerThreadId());
            sb.append(" state:");
            switch (pooledConnection.getState().get()) {
                case STATE_IDLE :
                    sb.append("IDLE");
                    break;
                case STATE_IN_USE :
                    sb.append("IN USE");
                    break;
                case STATE_CLOSE :
                    sb.append("CLOSE");
                    break;
                case STATE_RESETTING :
                    sb.append("RESETTING");
                    break;
                default:
                    sb.append("Unknown(").append(pooledConnection.getState().get()).append(")");
                    break;
            }
            sb.append(" last fetched:")
                    .append(numberFormat.format(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - pooledConnection.getLastUsed().get())))
                    .append("\n");
        }
        sb.append("}}");
        return sb.toString();
    }


    /**
     * For testing purpose only.
     * @return current thread id's
     */
    public List<Long> testGetConnectionThreadIds() {
        List<Long> threadIds = new ArrayList<>();
        for (MariaDbPooledConnection pooledConnection : connections) {
            threadIds.add(pooledConnection.getConnection().getServerThreadId());
        }
        return threadIds;
    }

    /**
     * JMX method to remove state (will be reinitialized on next connection creation).
     */
    public void resetStaticGlobal() {
        globalInfo = null;
    }
}
