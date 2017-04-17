package cc.blynk.server.admin.http.logic;

import cc.blynk.core.http.CookiesBaseHttpHandler;
import cc.blynk.core.http.MediaType;
import cc.blynk.core.http.Response;
import cc.blynk.core.http.annotation.*;
import cc.blynk.core.http.model.Filter;
import cc.blynk.server.Holder;
import cc.blynk.server.core.dao.*;
import cc.blynk.server.core.model.AppName;
import cc.blynk.server.core.model.DashBoard;
import cc.blynk.server.core.model.auth.Session;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.device.Device;
import cc.blynk.server.db.DBManager;
import cc.blynk.utils.JsonParser;
import cc.blynk.utils.SHA256Util;
import io.netty.channel.ChannelHandler;

import java.util.List;

import static cc.blynk.core.http.Response.*;
import static cc.blynk.utils.AdminHttpUtil.sort;


/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 03.12.15.
 */
@Path("/users")
@ChannelHandler.Sharable
public class UsersLogic extends CookiesBaseHttpHandler {

    private final UserDao userDao;
    private final FileManager fileManager;
    private final DBManager dbManager;

    public UsersLogic(Holder holder, String rootPath) {
        super(holder, rootPath);
        this.userDao = holder.userDao;
        this.fileManager = holder.fileManager;
        this.dbManager = holder.dbManager;
    }

    //for tests only
    public UsersLogic(UserDao userDao, SessionDao sessionDao, DBManager dbManager, FileManager fileManager, TokenManager tokenManager, String rootPath) {
        super(tokenManager, sessionDao, null, rootPath);
        this.userDao = userDao;
        this.fileManager = fileManager;
        this.dbManager = dbManager;
    }

    @GET
    @Path("")
    public Response getUsers(@QueryParam("_filters") String filterParam,
                                 @QueryParam("_page") int page,
                                 @QueryParam("_perPage") int size,
                                 @QueryParam("_sortField") String sortField,
                                 @QueryParam("_sortDir") String sortOrder) {
        if (filterParam != null) {
            Filter filter = JsonParser.readAny(filterParam, Filter.class);
            filterParam = filter == null ? null : filter.name;
        }

        List<User> users = userDao.searchByUsername(filterParam, AppName.ALL);
        return appendTotalCountHeader(
                ok(sort(users, sortField, sortOrder), page, size), users.size()
        );
    }

    @GET
    @Path("/{id}")
    public Response getUserByName(@PathParam("id") String id) {
        String[] parts =  slitByLast(id);
        String email = parts[0];
        String appName = parts[1];
        User user = userDao.getByName(email, appName);
        if (user == null) {
            return notFound();
        }
        return ok(user);
    }

    @GET
    @Path("/names/getAll")
    public Response getAllUserNames() {
        return ok(userDao.users.keySet());
    }

    @GET
    @Path("/token/assign")
    public Response assignToken(@QueryParam("old") String oldToken, @QueryParam("new") String newToken) {
        TokenValue tokenValue = tokenManager.getUserByToken(oldToken);

        if (tokenValue == null) {
            return badRequest("This token not exists.");
        }

        tokenManager.assignToken(tokenValue.user, tokenValue.dashId, tokenValue.deviceId, newToken);
        return ok();
    }

    @GET
    @Path("/token/force")
    public Response forceToken(@QueryParam("email") String email,
                                    @QueryParam("app") String app,
                                    @QueryParam("dashId") int dashId,
                                    @QueryParam("deviceId") int deviceId,
                                    @QueryParam("new") String newToken) {

        User user = userDao.getUsers().get(new UserKey(email, app));

        if (user == null) {
            return badRequest("No user with such email.");
        }

        tokenManager.assignToken(user, dashId, deviceId, newToken);
        return ok();
    }

    @PUT
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id,
                                   User updatedUser) {

        log.debug("Updating user {}", id);

        String[] parts =  slitByLast(id);
        String name = parts[0];
        String appName = parts[1];

        User oldUser = userDao.getByName(name, appName);

        //name was changed, but not password - do not allow this.
        //as name is used as salt for pass generation
        if (!updatedUser.email.equals(oldUser.email) && updatedUser.pass.equals(oldUser.pass)) {
            return badRequest("You need also change password when changing email.");
        }

            //user name was changed
        if (!updatedUser.email.equals(oldUser.email)) {
            deleteUserByName(id);
            for (DashBoard dashBoard : oldUser.profile.dashBoards) {
                for (Device device : dashBoard.devices) {
                    tokenManager.assignToken(updatedUser, dashBoard.id, device.id, device.token);
                }
            }
        }

        //if pass was changed, call hash function.
        if (!updatedUser.pass.equals(oldUser.pass)) {
            log.debug("Updating pass for {}.", updatedUser.email);
            updatedUser.pass = SHA256Util.makeHash(updatedUser.pass, updatedUser.email);
        }

        userDao.add(updatedUser);
        updatedUser.lastModifiedTs = System.currentTimeMillis();
        log.debug("Adding new user {}", updatedUser.email);


        return ok(updatedUser);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUserByName(@PathParam("id") String id) {
        String[] parts =  slitByLast(id);
        String email = parts[0];
        String appName = parts[1];

        UserKey userKey = new UserKey(email, appName);
        User user = userDao.delete(userKey);
        if (user == null) {
            return notFound();
        }

        if (!fileManager.delete(email, appName)) {
            return notFound();
        }

        dbManager.deleteUser(userKey);

        Session session = sessionDao.userSession.remove(userKey);
        if (session != null) {
            session.closeAll();
        }

        log.info("User {} successfully removed.", email);

        return ok();
    }

    private String[] slitByLast(String id) {
        int i = id.lastIndexOf("-");
        return new String[] {
                id.substring(0, i),
                id.substring(i + 1)
        };
    }

}
