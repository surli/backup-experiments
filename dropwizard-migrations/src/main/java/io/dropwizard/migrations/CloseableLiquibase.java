package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

import java.sql.SQLException;

public abstract class CloseableLiquibase extends Liquibase implements AutoCloseable {
    private final ManagedDataSource dataSource;

    public CloseableLiquibase(String changeLogFile, ResourceAccessor resourceAccessor, Database database, ManagedDataSource dataSource) throws LiquibaseException, SQLException {
        super(changeLogFile, resourceAccessor, database);
        this.dataSource = dataSource;
    }

    @Override
    public void close() throws Exception {
        try {
            database.close();
        } finally {
            dataSource.stop();
        }
    }
}
