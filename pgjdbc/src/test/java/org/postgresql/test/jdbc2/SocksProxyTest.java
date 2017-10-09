package org.postgresql.test.jdbc2;

import org.junit.After;
import org.junit.Test;
import org.postgresql.test.TestUtil;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.assertNotNull;

/**
 * @author Joe Kutner on 10/9/17.
 *         Twitter: @codefinger
 */
public class SocksProxyTest {

  @After
  public void cleanup() {
    System.clearProperty("socksProxyHost");
    System.clearProperty("socksProxyPort");
    System.clearProperty("socksNonProxyHosts");
  }

  /**
   * Tests the connect method by connecting to the test database
   */
  @Test
  public void testConnectWithSocksNonProxyHost() throws Exception {
    System.setProperty("socksProxyHost", "fake-socks-proxy");
    System.setProperty("socksProxyPort", "9999");
    System.setProperty("socksNonProxyHosts", TestUtil.getServer());

    TestUtil.initDriver(); // Set up log levels, etc.

    Connection con =
        DriverManager.getConnection(TestUtil.getURL(), TestUtil.getUser(), TestUtil.getPassword());

    assertNotNull(con);
    con.close();
  }
}
