package cc.blynk.server.db;

import cc.blynk.server.core.BlockingIOProcessor;
import cc.blynk.server.core.dao.UserKey;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.enums.GraphType;
import cc.blynk.server.core.reporting.average.AggregationKey;
import cc.blynk.server.core.reporting.average.AggregationValue;
import cc.blynk.server.core.stats.model.Stat;
import cc.blynk.server.db.dao.*;
import cc.blynk.server.db.model.FlashedToken;
import cc.blynk.server.db.model.Purchase;
import cc.blynk.server.db.model.Redeem;
import cc.blynk.utils.ServerProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 19.02.16.
 */
public class DBManager implements Closeable {

    public static final String DB_PROPERTIES_FILENAME = "db.properties";
    private static final Logger log = LogManager.getLogger(DBManager.class);
    private final HikariDataSource ds;

    private final BlockingIOProcessor blockingIOProcessor;
    private final boolean cleanOldReporting;
    protected UserDBDao userDBDao;
    protected ReportingDBDao reportingDBDao;
    protected RedeemDBDao redeemDBDao;
    protected PurchaseDBDao purchaseDBDao;
    protected FlashedTokensDBDao flashedTokensDBDao;

    public DBManager(BlockingIOProcessor blockingIOProcessor, boolean isEnabled) {
        this(DB_PROPERTIES_FILENAME, blockingIOProcessor, isEnabled);
    }

    public DBManager(String propsFilename, BlockingIOProcessor blockingIOProcessor, boolean isEnabled) {
        this.blockingIOProcessor = blockingIOProcessor;

        if (!isEnabled) {
            log.info("Separate DB storage disabled.");
            this.ds = null;
            this.cleanOldReporting = false;
            return;
        }

        ServerProperties serverProperties;
        try {
            serverProperties = new ServerProperties(propsFilename);
            if (serverProperties.size() == 0) {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            log.warn("No {} file found. Separate DB storage disabled.", propsFilename);
            this.ds = null;
            this.cleanOldReporting = false;
            return;
        }

        HikariConfig config = initConfig(serverProperties);

        log.info("DB url : {}", config.getJdbcUrl());
        log.info("DB user : {}", config.getUsername());
        log.info("Connecting to DB...");

        HikariDataSource hikariDataSource;
        try {
            hikariDataSource = new HikariDataSource(config);
        } catch (Exception e) {
            log.error("Not able connect to DB. Skipping. Reason : {}", e.getMessage());
            this.ds = null;
            this.cleanOldReporting = false;
            return;
        }

        this.ds = hikariDataSource;
        this.reportingDBDao = new ReportingDBDao(hikariDataSource);
        this.userDBDao = new UserDBDao(hikariDataSource);
        this.redeemDBDao = new RedeemDBDao(hikariDataSource);
        this.purchaseDBDao = new PurchaseDBDao(hikariDataSource);
        this.flashedTokensDBDao = new FlashedTokensDBDao(hikariDataSource);
        this.cleanOldReporting = serverProperties.getBoolProperty("clean.reporting");

        checkDBVersion();

        log.info("Connected to database successfully.");
    }

    private void checkDBVersion() {
        try {
            int dbVersion = userDBDao.getDBVersion();
            if (dbVersion < 90500) {
                log.error("Current Postgres version is lower than minimum required 9.5.0 version. PLEASE UPDATE YOUR DB.");
            }
        } catch (Exception e) {
            log.error("Error getting DB version.", e.getMessage());
        }
    }

    private HikariConfig initConfig(ServerProperties serverProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(serverProperties.getProperty("jdbc.url"));
        config.setUsername(serverProperties.getProperty("user"));
        config.setPassword(serverProperties.getProperty("password"));

        config.setAutoCommit(false);
        config.setConnectionTimeout(serverProperties.getLongProperty("connection.timeout.millis"));
        config.setMaximumPoolSize(3);
        config.setMaxLifetime(0);
        config.setConnectionTestQuery("SELECT 1");
        return config;
    }

    public void deleteUser(UserKey userKey) {
        if (isDBEnabled() && userKey != null) {
            blockingIOProcessor.execute(() -> userDBDao.deleteUser(userKey));
        }
    }

    public void saveUsers(ArrayList<User> users) {
        if (isDBEnabled() && users.size() > 0) {
            blockingIOProcessor.execute(() -> userDBDao.save(users));
        }
    }

    public void insertStat(String region, Stat stat) {
        if (isDBEnabled()) {
            reportingDBDao.insertStat(region, stat);
        }
    }

    public void insertReporting(Map<AggregationKey, AggregationValue> map, GraphType graphType) {
        if (isDBEnabled() && map.size() > 0) {
            blockingIOProcessor.execute(() -> reportingDBDao.insert(map, graphType));
        }
    }

    public void insertReportingRaw(Map<AggregationKey, Object> rawData) {
        if (isDBEnabled() && rawData.size() > 0) {
            blockingIOProcessor.execute(() -> reportingDBDao.insertRawData(rawData));
        }
    }

    public void cleanOldReportingRecords(Instant now) {
        if (isDBEnabled() && cleanOldReporting) {
            blockingIOProcessor.execute(() -> reportingDBDao.cleanOldReportingRecords(now));
        }
    }

    public Redeem selectRedeemByToken(String token) throws Exception {
        if (isDBEnabled()) {
            return redeemDBDao.selectRedeemByToken(token);
        }
        return null;
    }

    public boolean updateRedeem(String email, String token) throws Exception {
        return redeemDBDao.updateRedeem(email, token);
    }

    public void insertRedeems(List<Redeem> redeemList) {
        if (isDBEnabled() && redeemList.size() > 0) {
            redeemDBDao.insertRedeems(redeemList);
        }
    }

    public FlashedToken selectFlashedToken(String token, String appName) {
        if (isDBEnabled()) {
            return flashedTokensDBDao.selectFlashedToken(token, appName);
        }
        return null;
    }

    public boolean activateFlashedToken(String token, String appName) {
        return flashedTokensDBDao.activateFlashedToken(token, appName);
    }

    public boolean insertFlashedTokens(FlashedToken[] flashedTokenList) throws Exception {
        if (isDBEnabled() && flashedTokenList.length > 0) {
            flashedTokensDBDao.insertFlashedTokens(flashedTokenList);
            return true;
        }
        return false;
    }


    public void insertPurchase(Purchase purchase) {
        if (isDBEnabled()) {
            purchaseDBDao.insertPurchase(purchase);
        }
    }

    public boolean isDBEnabled() {
        return ds != null;
    }

    public void executeSQL(String sql) throws Exception {
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            connection.commit();
        }
    }

    public Connection getConnection() throws Exception {
        return ds.getConnection();
    }

    @Override
    public void close() {
        if (isDBEnabled()) {
            System.out.println("Closing DB...");
            ds.close();
        }
    }

}
