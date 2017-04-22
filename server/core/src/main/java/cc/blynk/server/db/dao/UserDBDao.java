package cc.blynk.server.db.dao;

import cc.blynk.server.core.dao.UserKey;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.utils.JsonParser;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static cc.blynk.utils.DateTimeUtils.UTC_CALENDAR;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 09.03.16.
 */
public class UserDBDao {

    public static final String upsertUser = "INSERT INTO users (email, appName, region, name, pass, last_modified, last_logged, last_logged_ip, is_facebook_user, is_super_admin, energy, json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (email, appName) DO UPDATE SET pass = EXCLUDED.pass, name = EXCLUDED.name, last_modified = EXCLUDED.last_modified, last_logged = EXCLUDED.last_logged, last_logged_ip = EXCLUDED.last_logged_ip, is_facebook_user = EXCLUDED.is_facebook_user, is_super_admin = EXCLUDED.is_super_admin, energy = EXCLUDED.energy, json = EXCLUDED.json, region = EXCLUDED.region";
    public static final String selectAllUsers = "SELECT * from users";
    public static final String deleteUser = "DELETE FROM users WHERE email = ? AND appName = ?";

    private static final Logger log = LogManager.getLogger(UserDBDao.class);
    private final HikariDataSource ds;

    public UserDBDao(HikariDataSource ds) {
        this.ds = ds;
    }

    public int getDBVersion() throws Exception {
        ResultSet rs = null;
        int dbVersion = 0;
        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {

            rs = statement.executeQuery("SELECT current_setting('server_version_num')");
            rs.next();
            dbVersion = rs.getInt(1);
            connection.commit();
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return dbVersion;
    }

    public ConcurrentMap<UserKey, User> getAllUsers() throws Exception {
        ResultSet rs = null;
        ConcurrentMap<UserKey, User> users = new ConcurrentHashMap<>();

        try (Connection connection = ds.getConnection();
             Statement statement = connection.createStatement()) {

            rs = statement.executeQuery(selectAllUsers);

            while (rs.next()) {
                User user = new User();

                user.email = rs.getString("email");
                user.appName = rs.getString("appName");
                user.region = rs.getString("region");
                user.name = rs.getString("name");
                user.pass = rs.getString("pass");
                user.lastModifiedTs = rs.getTimestamp("last_modified", UTC_CALENDAR).getTime();
                user.lastLoggedAt = rs.getTimestamp("last_logged", UTC_CALENDAR).getTime();
                user.lastLoggedIP = rs.getString("last_logged_ip");
                user.isFacebookUser = rs.getBoolean("is_facebook_user");
                user.isSuperAdmin = rs.getBoolean("is_super_admin");
                user.energy = rs.getInt("energy");
                user.profile = JsonParser.parseProfileFromString(rs.getString("json"));

                users.put(new UserKey(user), user);
            }
            connection.commit();
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        log.info("Loaded {} users.", users.size());

        return users;
    }

    public void save(ArrayList<User> users) {
        long start = System.currentTimeMillis();
        log.info("Storing users...");

        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(upsertUser)) {

            for (User user : users) {
                ps.setString(1, user.email);
                ps.setString(2, user.appName);
                ps.setString(3, user.region);
                ps.setString(4, user.name);
                ps.setString(5, user.pass);
                ps.setTimestamp(6, new Timestamp(user.lastModifiedTs), UTC_CALENDAR);
                ps.setTimestamp(7, new Timestamp(user.lastLoggedAt), UTC_CALENDAR);
                ps.setString(8, user.lastLoggedIP);//finish
                ps.setBoolean(9, user.isFacebookUser);
                ps.setBoolean(10, user.isSuperAdmin);
                ps.setInt(11, user.energy);
                ps.setString(12, user.profile.toString());
                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();
        } catch (Exception e) {
            log.error("Error upserting users in DB.", e);
        }
        log.info("Storing users finished. Time {}. Users saved {}", System.currentTimeMillis() - start, users.size());
    }

    public boolean deleteUser(UserKey userKey) {
        int removed = 0;

        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteUser)) {

            ps.setString(1, userKey.email);
            ps.setString(2, userKey.appName);

            removed = ps.executeUpdate();

            connection.commit();
        } catch (Exception e) {
            log.error("Error removing user {} from DB.", userKey, e);
        }

        return removed > 0;
    }
}
