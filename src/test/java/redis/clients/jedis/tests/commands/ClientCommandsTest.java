package redis.clients.jedis.tests.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static redis.clients.jedis.params.ClientKillParams.Type;
import static redis.clients.jedis.params.ClientKillParams.SkipMe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.ClientKillParams;

public class ClientCommandsTest extends JedisCommandTestBase {

  @Test
  public void idString() {
    String name = "fancy_jedis_name";
    Jedis j = createJedis();
    j.clientSetname(name);

    String info = findInClientList(name);
    Matcher matcher = Pattern.compile("\\bid=(\\d+)\\b").matcher(info);
    matcher.find();
    String id = matcher.group(1);

    long clients = jedis.clientKill(new ClientKillParams().id(id));
    assertEquals(1, clients);

    assertDisconnected(j);
    j.close();
  }

  @Test
  public void idBinary() {
    byte[] name = "fancy_jedis_name".getBytes();
    Jedis j = createJedis();
    j.clientSetname(name);

    String info = findInClientList(new String(name));
    Matcher matcher = Pattern.compile("\\bid=(\\d+)\\b").matcher(info);
    matcher.find();
    byte[] id = matcher.group(1).getBytes();

    long clients = jedis.clientKill(new ClientKillParams().id(id));
    assertEquals(1, clients);

    assertDisconnected(j);
    j.close();
  }

  @Test
  public void typeNormal() {
    Jedis j = createJedis();

    long clients = jedis.clientKill(new ClientKillParams().type(Type.NORMAL));
    assertTrue(clients > 0);

    assertDisconnected(j);
    j.close();
  }

  @Test
  public void skipmeNo() {
    jedis.clientKill(new ClientKillParams().type(Type.NORMAL).skipMe(SkipMe.NO));
    assertDisconnected(jedis);
  }

  @Test
  public void skipmeYesNo() {
    jedis.clientKill(new ClientKillParams().type(Type.NORMAL).skipMe(SkipMe.YES));
    long clients = jedis.clientKill(new ClientKillParams().type(Type.NORMAL).skipMe(SkipMe.NO));
    assertEquals(1, clients);
    assertDisconnected(jedis);
  }

  @Test
  public void addrString() {
    String name = "fancy_jedis_name";
    Jedis j = createJedis();
    j.clientSetname(name);

    String info = findInClientList(name);
    Matcher matcher = Pattern.compile("\\baddr=(\\S+)\\b").matcher(info);
    matcher.find();
    String addr = matcher.group(1);

    long clients = jedis.clientKill(new ClientKillParams().addr(addr));
    assertEquals(1, clients);

    assertDisconnected(j);
    j.close();
  }

  @Test
  public void addrBinary() {
    byte[] name = "fancy_jedis_name".getBytes();
    Jedis j = createJedis();
    j.clientSetname(name);

    String info = findInClientList(new String(name));
    Matcher matcher = Pattern.compile("\\baddr=(\\S+)\\b").matcher(info);
    matcher.find();
    String addr = matcher.group(1);

    long clients = jedis.clientKill(new ClientKillParams().addr(addr));
    assertEquals(1, clients);

    assertDisconnected(j);
    j.close();
  }

  @Test
  public void addrIpPort() {
    String name = "fancy_jedis_name";
    Jedis j = createJedis();
    j.clientSetname(name);

    String info = findInClientList(name);
    Matcher matcher = Pattern.compile("\\baddr=(\\S+)\\b").matcher(info);
    matcher.find();
    String addr = matcher.group(1);
    String[] hp = HostAndPort.extractParts(addr);

    long clients = jedis.clientKill(new ClientKillParams().addr(hp[0], Integer.parseInt(hp[1])));
    assertEquals(1, clients);

    assertDisconnected(j);
    j.close();
  }

  private void assertDisconnected(Jedis j) {    
    try {
      j.ping();
      fail("Jedis connection should be disconnected");
    } catch(JedisConnectionException jce) {
      // should be here
    } 
  }

  private String findInClientList(String name) {
    Pattern pattern = Pattern.compile("\\bname=" + name + "\\b");
    for (String clientInfo : jedis.clientList().split("\n")) {
      if (pattern.matcher(clientInfo).find()) {
        return clientInfo;
      }
    }
    return null;
  }
}
