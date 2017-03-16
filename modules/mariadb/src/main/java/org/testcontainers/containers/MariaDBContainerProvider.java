package org.testcontainers.containers;

/**
 * Factory for MariaDB containers.
 */
public class MariaDBContainerProvider extends JdbcDatabaseContainerProvider {
    @Override
    public boolean supports(String databaseType) {
        return databaseType.equals(MariaDBContainer.NAME);
    }

    @Override
    public JdbcDatabaseContainer newInstance(String tag) {
        return new MariaDBContainer(MariaDBContainer.IMAGE + ":" + tag);
    }
}
