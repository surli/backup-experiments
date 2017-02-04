package io.dropwizard.migrations;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedPooledDataSource;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import net.jcip.annotations.NotThreadSafe;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class CloseableLiquibaseTest {

    CloseableLiquibase liquibase;
    ManagedPooledDataSource dataSource;

    @Before
    public void setUp() throws Exception {
        DataSourceFactory factory = new DataSourceFactory();

        factory.setDriverClass(org.h2.Driver.class.getName());
        factory.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis());
        factory.setUser("DbTest");

        dataSource = (ManagedPooledDataSource) factory.build(new MetricRegistry(), "DbTest");
        DatabaseConnection conn = new JdbcConnection(dataSource.getConnection());
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn);
        liquibase = new CloseableLiquibaseWithClassPathMigrationsFile(dataSource, database, "migrations.xml");
    }

    @Test
    public void testWhenClosingAllConnectionsInPoolIsReleased() throws Exception {
        ConnectionPool pool = dataSource.getPool();
        liquibase.close();

        assertThat(pool.getActive()).isZero();
        assertThat(pool.getIdle()).isZero();
        assertThat(pool.isClosed()).isTrue();
    }
}
