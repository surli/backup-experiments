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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final String poolTag;
    private ScheduledThreadPoolExecutor idleConnectionChecker;
    private GlobalStateInfo globalInfo;

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
        poolTag = generatePoolTag(poolIndex);

        //one thread to add new connection to pool.
        connectionAppender = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2),
                new MariaDbThreadFactory(poolTag + "-appender"));
        connectionAppender.allowCoreThreadTimeOut(true);

        //one thread that will handle removal of connection exception and removing idle connection
        connectionRemover = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(options.maxPoolSize),
                new MariaDbThreadFactory(poolTag + "-remover"));
        connectionRemover.allowCoreThreadTimeOut(true);

        if (options.minPoolSize < options.maxPoolSize) {
            idleConnectionChecker = new ScheduledThreadPoolExecutor(1, new MariaDbThreadFactory(poolTag + "-maxTimeoutIdle"));
            idleConnectionChecker.scheduleAtFixedRate(this::removeIdleTimeoutConnection,
                    Options.MAX_IDLE_TIME_MIN_VALUE / 3,
                    Options.MAX_IDLE_TIME_MIN_VALUE / 3, TimeUnit.SECONDS);
        }

        if (options.registerJmxPool) {
            try {
                registerJmx();
            } catch (Exception ex) {
                logger.info("pool " + poolTag + " not registered due to exception : " + ex.getMessage());
            }
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
            }
        }
    }

    private void removeIdleTimeoutConnection() {
        for (MariaDbPooledConnection item : connections) {
            if (connections.size() > options.minPoolSize
                    && (System.nanoTime() - item.getLastUsed().get()) > TimeUnit.SECONDS.toNanos(options.maxIdleTime)
                    && item.getState().compareAndSet(STATE_IDLE, STATE_CLOSE)) {

                connections.remove(item);
                try {
                    item.getConnection().close();
                } catch (SQLException ex) {
                    //eat exception
                }
                logger.debug("pool {} connection removed due to inactivity (total:{}, maxPoolSize:{}, pending:{})",
                        poolTag, connections.size(), options.maxPoolSize, pendingRequestNumber.get());

            }
        }
    }

    private MariaDbConnection getIdleConnection() {
        for (MariaDbPooledConnection item : connections) {
            if (item.getState().compareAndSet(STATE_IDLE, STATE_IN_USE)) {

                //check that connection has been used recently (server will close socket after @@WAIT_TIMEOUT
                if (globalInfo != null) {
                    long justBeforeTimeout = TimeUnit.SECONDS.toNanos(Math.max(1, globalInfo.getWaitTimeout() - 20));
                    if (System.nanoTime() - item.getLastUsed().get() >  justBeforeTimeout) {
                        forceCloseConnection(item);
                    }
                }

                if (validateItem(item)) return item.getConnection();
            }
        }
        return null;
    }

    private boolean validateItem(MariaDbPooledConnection item) {
        MariaDbConnection connection = item.getConnection();
        try {
            if (connection.isValid(10)) { //10 seconds timeout
                item.lastUsedToNow();
                return true;
            }
        } catch (SQLException sqle) {
            //eat
        }
        forceCloseConnection(item);
        logger.debug("pool {} connection removed from pool (total:{}, maxPoolSize:{}, pending:{})",
                poolTag, connections.size(), options.maxPoolSize, pendingRequestNumber.get());

        return false;
    }

    private void returnConnectionToPool(MariaDbPooledConnection item) {
        if (item.getState().compareAndSet(STATE_IN_USE, STATE_RESETTING)) {
            try {
                item.getConnection().reset();
                item.getState().set(STATE_IDLE);
            } catch (SQLException sqle) {
                //sql exception during reset, removing connection from pool
                forceCloseConnection(item);
            }
        }
    }

    private void forceCloseConnection(MariaDbPooledConnection item) {
        item.getState().set(STATE_CLOSE);
        connections.remove(item);
        try {
            item.getConnection().pooledConnection = null;
            item.getConnection().abort(connectionRemover);
        } catch (SQLException ex) {
            //eat exception
        }
        logger.trace("connection removed from pool {}", poolTag);
    }

    /**
     * Add new connection if needed.
     * Only one thread create new connection, so new connection request will wait to newly created connection or
     * for a released connection.
     */
    private void increaseConnectionNumber() {
        if (connections.size() < options.maxPoolSize && connectionAppender.getQueue().size() == 0) {
            //sync on queue to ensure not filling the queue unnecessarily
            synchronized (connectionAppender.getQueue()) {
                if (connectionAppender.getQueue().size() == 0 && poolState.get() == POOL_STATE_OK) {
                    try {
                        connectionAppender.execute(() -> {
                            do {
                                if (connections.size() < options.maxPoolSize) {
                                    try {
                                        //TODO add verifier that ensure that a connection never hang, blocking pool indefinitely

                                        //create new connection
                                        Protocol protocol = Utils.retrieveProxy(urlParser, globalInfo);
                                        MariaDbConnection connection = new MariaDbConnection(protocol);

                                        if (options.staticGlobal) {

                                            //on first connection load initial state
                                            initializePoolGlobalState(connection);

                                            //set default transaction isolation level to permit resetting to initial state
                                            connection.setDefaultTransactionIsolation(globalInfo.getDefaultTransactionIsolation());

                                        } else {

                                            //set default transaction isolation level to permit resetting to initial state
                                            connection.setDefaultTransactionIsolation(connection.getTransactionIsolation());

                                        }

                                        addConnection(connection);

                                    } catch (SQLException sqle) {
                                        break;
                                    }
                                }
                            } while (pendingRequestNumber.get() > 1 && connections.size() < options.maxPoolSize);
                        });
                    } catch (RejectedExecutionException rejecting) {
                        //in case executor is shutdown (pool is closing)
                        rejecting.printStackTrace();
                    }
                }
            }
        }
    }

    private void addConnection(MariaDbConnection connection) {
        MariaDbPooledConnection pooledConnection = new MariaDbPooledConnection(connection);
        pooledConnection.addConnectionEventListener(new ConnectionEventListener() {

            @Override
            public void connectionClosed(ConnectionEvent event) {
                returnConnectionToPool((MariaDbPooledConnection) event.getSource());
            }

            @Override
            public void connectionErrorOccurred(ConnectionEvent event) {
                forceCloseConnection((MariaDbPooledConnection) event.getSource());
            }
        });

        connections.add(pooledConnection);

        logger.debug("pool {} new physical connection created (total:{}, maxPoolSize:{}, pending:{})",
                poolTag, connections.size(), options.maxPoolSize, pendingRequestNumber.get());
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
        //try to get one idle connection
        MariaDbConnection connection = getIdleConnection();
        if (connection != null) {
            logger.debug("idle connection retrieved from pool {}", poolTag);
            return connection;
        }

        //increment pending request
        pendingRequestNumber.incrementAndGet();
        try {

            // add "pending request" new connections (limited to maxPoolSize)
            increaseConnectionNumber();

            //wait to have a free connection
            long initialTime = System.nanoTime();
            do {
                connection = getIdleConnection();
                if (connection != null) {
                    logger.debug("new connection retrieved from pool {}", poolTag);
                    return connection;
                }

                //free some CPU
                try {
                    Thread.sleep(0, 10_000);
                } catch (InterruptedException ie) {
                    //eat
                }
            } while (options.connectTimeout - TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - initialTime) > 0);
        } finally {
            pendingRequestNumber.decrementAndGet();
        }

        throw ExceptionMapper.connException("No connection available within the specified time "
                        + "(option 'connectTimeout': " + NumberFormat.getInstance().format(options.connectTimeout) + " ms)",
                null);
    }

    /**
     * Get new connection from pool.
     * If username and password are different from pool, will return a not pooled connection.
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
                        try {
                            item.getConnection().pooledConnection = null;
                            item.getConnection().abort(connectionRemover);
                        } catch (SQLException ex) {
                            //eat exception
                        }
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
                    try {
                        item.getConnection().pooledConnection = null;
                        item.getConnection().abort(connectionRemover);
                    } catch (SQLException ex) {
                        //eat exception
                    }
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
            sb.append("Conn: ").append(pooledConnection.getConnection().getServerThreadId());
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
}
