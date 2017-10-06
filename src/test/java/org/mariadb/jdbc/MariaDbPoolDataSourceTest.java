package org.mariadb.jdbc;

import org.junit.Test;
import org.mariadb.jdbc.internal.util.pool.Pools;
import org.mariadb.jdbc.internal.util.scheduler.MariaDbThreadFactory;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MariaDbPoolDataSourceTest extends BaseTest {

    @Test
    public void testResetDatabase() throws SQLException {
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=1")) {
            try (Connection connection = pool.getConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("CREATE DATABASE IF NOT EXISTS testingReset");
                connection.setCatalog("testingReset");
            }

            try (Connection connection = pool.getConnection()) {
                assertEquals(database, connection.getCatalog());
                Statement statement = connection.createStatement();
                statement.execute("DROP DATABASE testingReset");
            }
        }
    }


    @Test
    public void testResetReadOnly() throws SQLException {
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=1")) {
            try (Connection connection = pool.getConnection()) {
                assertFalse(connection.isReadOnly());
                connection.setReadOnly(true);
                assertTrue(connection.isReadOnly());
            }

            try (Connection connection = pool.getConnection()) {
                assertFalse(connection.isReadOnly());
            }
        }
    }

    @Test
    public void testResetAutoCommit() throws SQLException {
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=1")) {
            try (Connection connection = pool.getConnection()) {
                assertTrue(connection.getAutoCommit());
                connection.setAutoCommit(false);
                assertFalse(connection.getAutoCommit());
            }

            try (Connection connection = pool.getConnection()) {
                assertTrue(connection.getAutoCommit());
            }
        }
    }

    @Test
    public void testResetAutoCommitOption() throws SQLException {
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=1&autocommit=false&poolName=PoolTest")) {
            try (Connection connection = pool.getConnection()) {
                assertFalse(connection.getAutoCommit());
                connection.setAutoCommit(true);
                assertTrue(connection.getAutoCommit());
            }

            try (Connection connection = pool.getConnection()) {
                assertFalse(connection.getAutoCommit());
            }
        }
    }

    @Test
    public void testResetTransactionIsolation() throws SQLException {
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=1")) {
            try (Connection connection = pool.getConnection()) {
                assertEquals(Connection.TRANSACTION_REPEATABLE_READ, connection.getTransactionIsolation());
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                assertEquals(Connection.TRANSACTION_SERIALIZABLE, connection.getTransactionIsolation());
            }

            try (Connection connection = pool.getConnection()) {
                assertEquals(Connection.TRANSACTION_REPEATABLE_READ, connection.getTransactionIsolation());
            }
        }
    }

    @Test
    public void testJmx() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName filter = new ObjectName("org.mariadb.jdbc.pool:type=PoolTest-*");
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=20&poolName=PoolTest")) {
            try (Connection connection = pool.getConnection()) {
                Set<ObjectName> objectNames = server.queryNames(filter, null);
                assertEquals(1, objectNames.size());
                ObjectName name = objectNames.iterator().next();

                MBeanInfo info = server.getMBeanInfo(name);
                assertEquals(4, info.getAttributes().length);
                checkJmxInfo(server, name, 1, 1, 0, 0);

                try (Connection connection2 = pool.getConnection()) {
                    checkJmxInfo(server, name, 2, 2, 0, 0);
                }
                checkJmxInfo(server, name, 1, 2, 1, 0);
            }
        }
    }

    private void checkJmxInfo(MBeanServer server,
                              ObjectName name,
                              long expectedActive,
                              long expectedTotal,
                              long expectedIdle,
                              long expectedRequest)
            throws Exception {

        assertEquals(expectedActive, ((Long) server.getAttribute(name, "ActiveConnections")).longValue());
        assertEquals(expectedTotal, ((Long) server.getAttribute(name, "TotalConnections")).longValue());
        assertEquals(expectedIdle, ((Long) server.getAttribute(name, "IdleConnections")).longValue());
        assertEquals(expectedRequest, ((Long) server.getAttribute(name, "ConnectionRequests")).longValue());
    }

    @Test
    public void testJmxDisable() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName filter = new ObjectName("org.mariadb.jdbc.pool:type=PoolTest-*");
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=2&registerJmxPool=false&poolName=PoolTest")) {
            try (Connection connection = pool.getConnection()) {
                Set<ObjectName> objectNames = server.queryNames(filter, null);
                assertEquals(0, objectNames.size());
            }
        }
    }

    @Test
    public void testResetRollback() throws SQLException {
        createTable("testResetRollback", "id int not null primary key auto_increment, test varchar(20)");
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=1")) {
            try (Connection connection = pool.getConnection()) {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("INSERT INTO testResetRollback (test) VALUES ('heja')");
                connection.setAutoCommit(false);
                stmt.executeUpdate("INSERT INTO testResetRollback (test) VALUES ('japp')");
            }

            try (Connection connection = pool.getConnection()) {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM testResetRollback");
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    @Test
    public void ensureUsingPool() throws Exception {
        ThreadPoolExecutor connectionAppender = new ThreadPoolExecutor(50, 100, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new MariaDbThreadFactory("testPool"));

        final long start = System.currentTimeMillis();
        Set<Integer> threadIds = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            connectionAppender.execute(() -> {
                try (Connection connection = DriverManager.getConnection(connUri + "&pool&staticGlobal&poolName=PoolTest")) {
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT CONNECTION_ID()");
                    rs.next();
                    threadIds.add(rs.getInt(1));
                    stmt.execute("SELECT * FROM mysql.user");
                    System.out.println("executed " + connectionAppender.getQueue().size());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
        connectionAppender.shutdown();
        connectionAppender.awaitTermination(30, TimeUnit.SECONDS);
        assertEquals(8, threadIds.size());
        assertTrue(3_000 > System.currentTimeMillis() - start);
        Pools.close("PoolTest");
    }

    @Test
    public void ensureClosed() throws SQLException {
        int initialConnection = getCurrentConnections();

        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource(connUri + "&maxPoolSize=10")) {
            try (Connection connection = pool.getConnection()) {
                connection.isValid(10_000);
            }
            assertEquals(initialConnection + 1, getCurrentConnections());

            //reuse IdleConnection
            try (Connection connection = pool.getConnection()) {
                connection.isValid(10_000);
            }

            //reuse IdleConnection
            try (Connection connection = pool.getConnection()) {
                connection.isValid(10_000);
            }

            assertEquals(initialConnection + 1, getCurrentConnections());
        }
        assertEquals(initialConnection, getCurrentConnections());
    }

    private int getCurrentConnections() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        ResultSet rs = stmt.executeQuery("show status where `variable_name` = 'Threads_connected'");
        assertTrue(rs.next());
        return rs.getInt(2);
    }


    @Test
    public void wrongUrlHandling() throws SQLException {

        int initialConnection = getCurrentConnections();
        try (MariaDbPoolDataSource pool = new MariaDbPoolDataSource("jdbc:mariadb://unknowhost/db?user=wrong&maxPoolSize=10&connectTimeout=500")) {
            long start = System.currentTimeMillis();
            try (Connection connection = pool.getConnection()) {
                fail();
            } catch (SQLException sqle) {
                assertTrue("timeout does not correspond to option. Elapsed time:" + (System.currentTimeMillis() - start),
                        (System.currentTimeMillis() - start) > 500 && (System.currentTimeMillis() - start) < 700);
                assertTrue(sqle.getMessage().contains("No connection available within the specified time (option 'connectTimeout': 500 ms)"));
            }
        }
    }


    //TODO check that threads are destroy when closing pool
}