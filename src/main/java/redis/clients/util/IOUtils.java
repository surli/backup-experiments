package redis.clients.util;

import java.io.IOException;
import java.net.Socket;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class IOUtils {
  private IOUtils() {
  }

  public static void closeQuietly(Socket sock) {
    // It's same thing as Apache Commons - IOUtils.closeQuietly()
    if (sock != null) {
      try {
        sock.close();
      } catch (IOException e) {
        // ignored
      }
    }
  }

  public static void closeQuietly(Jedis jedis) {
    if (jedis != null) {
      try {
        jedis.close();
      } catch (JedisException e) {
        // ignored
      }
    }
  }
}
