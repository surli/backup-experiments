package cc.blynk.server.admin.http.logic.admin;

import cc.blynk.core.http.Response;
import cc.blynk.server.admin.http.logic.UsersLogic;
import cc.blynk.server.core.dao.FileManager;
import cc.blynk.server.core.dao.SessionDao;
import cc.blynk.server.core.dao.UserDao;
import cc.blynk.server.core.dao.UserKey;
import cc.blynk.server.core.model.AppName;
import cc.blynk.server.core.model.auth.Session;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.db.DBManager;
import io.netty.channel.EventLoop;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsersLogicTest {

    @Mock
    private UserDao userDao;
    @Spy
    private SessionDao sessionDao;
    @Mock
    private DBManager dbManager;

    private User user;

    @Mock
    private Session session;

    private UsersLogic usersLogic;
    private static final String TEST_USER = "test_user";
    private static Path userFile;
    private static Path deletedUserFile;
    private static final String DELETED_DATA_DIR_NAME = "deleted";

    @Before
    public void setUp() throws Exception {
        user = new User(TEST_USER, "123", AppName.BLYNK, "local", false, false);
        when(userDao.delete(any())).thenReturn(user);
        sessionDao.getOrCreateSessionByUser(new UserKey(user), mock(EventLoop.class));
        FileManager fileManager = new FileManager(null);
        usersLogic = new UsersLogic(userDao, sessionDao, dbManager, fileManager, null, "admin");

        userFile = Paths.get(System.getProperty("java.io.tmpdir"), "blynk", TEST_USER + ".Blynk.user");
        deletedUserFile = Paths.get(System.getProperty("java.io.tmpdir"), "blynk", DELETED_DATA_DIR_NAME, TEST_USER + ".Blynk.user");
        Files.deleteIfExists(userFile);
        Files.deleteIfExists(deletedUserFile);

        Files.createFile(userFile);
    }

    @Test
    public void deleteUserByName() throws Exception {
        Response response = usersLogic.deleteUserByName(TEST_USER + "-" + AppName.BLYNK);

        assertEquals(OK, response.status());
        assertFalse(Files.exists(userFile));
        assertTrue(Files.exists(deletedUserFile));
    }

    @Test
    public void deleteFakeUserByName() throws Exception {
        Response response = usersLogic.deleteUserByName("fake user" + "-" + AppName.BLYNK);

        assertEquals(NOT_FOUND, response.status());
    }

}