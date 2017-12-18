/*
 * Copyright (c) 2017, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.test.jdbc42;

import org.postgresql.test.TestUtil;
import org.postgresql.util.PSQLState;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test methods with small counts that return long and failure scenarios. This have two really big
 * and slow test, they are ignored for CI but can be tested locally to check that it works.
 */
public class LargeCountJdbc42Test {

  private Connection con;

  @Before
  public void setUp() throws Exception {
    con = TestUtil.openDB();
    TestUtil.createUnloggedTable(con, "largetable", "a boolean");
  }

  @After
  public void tearDown() throws SQLException {
    TestUtil.dropTable(con, "largetable");
    TestUtil.closeDB(con);
  }

  /*
   * FINEST: simple execute, handler=org.postgresql.jdbc.PgStatement$StatementResultHandler@38cccef, maxRows=0, fetchSize=0, flags=21
   * FINEST: FE=> Parse(stmt=null,query="insert into largetable select true from generate_series($1, $2)",oids={20,20})
   * FINEST: FE=> Bind(stmt=null,portal=null,$1=<1>,$2=<2147483757>)
   * FINEST: FE=> Describe(portal=null)
   * FINEST: FE=> Execute(portal=null,limit=1)
   * FINEST: FE=> Sync
   * FINEST: <=BE ParseComplete [null]
   * FINEST: <=BE BindComplete [unnamed]
   * FINEST: <=BE NoData
   * FINEST: <=BE CommandStatus(INSERT 0 2147483757)
   * FINEST: <=BE ReadyForQuery(I)
   * FINEST: simple execute, handler=org.postgresql.jdbc.PgStatement$StatementResultHandler@5679c6c6, maxRows=0, fetchSize=0, flags=21
   * FINEST: FE=> Parse(stmt=null,query="delete from largetable",oids={})
   * FINEST: FE=> Bind(stmt=null,portal=null)
   * FINEST: FE=> Describe(portal=null)
   * FINEST: FE=> Execute(portal=null,limit=1)
   * FINEST: FE=> Sync
   * FINEST: <=BE ParseComplete [null]
   * FINEST: <=BE BindComplete [unnamed]
   * FINEST: <=BE NoData
   * FINEST: <=BE CommandStatus(DELETE 2147483757)
   */

  /*
   * Warning: BIG and SLOW, it's ignored.
   * Test PreparedStatement.executeLargeUpdate() and Statement.executeLargeUpdate(String sql)
   */
  @Ignore
  @Test
  public void testExecuteLargeUpdateBIG() throws Exception {
    long expected = Integer.MAX_VALUE + 110L;
    con.setAutoCommit(false);
    // Test PreparedStatement.executeLargeUpdate()
    try (PreparedStatement stmt = con.prepareStatement("insert into largetable "
        + "select true from generate_series(?, ?)")) {
      stmt.setLong(1, 1);
      stmt.setLong(2, 2_147_483_757L); // Integer.MAX_VALUE + 110L
      long count = stmt.executeLargeUpdate();
      Assert.assertEquals("PreparedStatement 110 rows more than Integer.MAX_VALUE", expected, count);
    }
    // Test Statement.executeLargeUpdate(String sql)
    try (Statement stmt = con.createStatement()) {
      long count = stmt.executeLargeUpdate("delete from largetable");
      Assert.assertEquals("Statement 110 rows more than Integer.MAX_VALUE", expected, count);
    }
    con.setAutoCommit(true);
  }

  /*
   * Test Statement.executeLargeUpdate(String sql)
   */
  @Test
  public void testExecuteLargeUpdateStatementSMALL() throws Exception {
    try (Statement stmt = con.createStatement()) {
      long count = stmt.executeLargeUpdate("insert into largetable "
          + "select true from generate_series(1, 1010)");
      long expected = 1010L;
      Assert.assertEquals("Small long return 1010L", expected, count);
    }
  }

  /*
   * Test PreparedStatement.executeLargeUpdate();
   */
  @Test
  public void testExecuteLargeUpdatePreparedStatementSMALL() throws Exception {
    try (PreparedStatement stmt = con.prepareStatement("insert into largetable "
        + "select true from generate_series(?, ?)")) {
      stmt.setLong(1, 1);
      stmt.setLong(2, 1010L);
      long count = stmt.executeLargeUpdate();
      long expected = 1010L;
      Assert.assertEquals("Small long return 1010L", expected, count);
    }
  }

  /*
   * Test Statement.getLargeUpdateCount();
   */
  @Test
  public void testGetLargeUpdateCountStatementSMALL() throws Exception {
    try (Statement stmt = con.createStatement()) {
      boolean isResult = stmt.execute("insert into largetable "
          + "select true from generate_series(1, 1010)");
      Assert.assertFalse("False if it is an update count or there are no results", isResult);
      long count = stmt.getLargeUpdateCount();
      long expected = 1010L;
      Assert.assertEquals("Small long return 1010L", expected, count);
    }
  }

  /*
   * Test PreparedStatement.getLargeUpdateCount();
   */
  @Test
  public void testGetLargeUpdateCountPreparedStatementSMALL() throws Exception {
    try (PreparedStatement stmt = con.prepareStatement("insert into largetable "
        + "select true from generate_series(?, ?)")) {
      stmt.setInt(1, 1);
      stmt.setInt(2, 1010);
      boolean isResult = stmt.execute();
      Assert.assertFalse("False if it is an update count or there are no results", isResult);
      long count = stmt.getLargeUpdateCount();
      long expected = 1010L;
      Assert.assertEquals("Small long return 1010L", expected, count);
    }
  }

  /*
   * Test fail SELECT Statement.executeLargeUpdate(String sql)
   */
  @Test
  public void testExecuteLargeUpdateStatementSELECT() throws Exception {
    try (Statement stmt = con.createStatement()) {
      long count = stmt.executeLargeUpdate("select true from generate_series(1, 5)");
      Assert.fail("A result was returned when none was expected. Returned: " + count);
    } catch (SQLException e) {
      Assert.assertEquals(PSQLState.TOO_MANY_RESULTS.getState(), e.getSQLState());
    }
  }

  /*
   * Test fail SELECT PreparedStatement.executeLargeUpdate();
   */
  @Test
  public void testExecuteLargeUpdatePreparedStatementSELECT() throws Exception {
    try (PreparedStatement stmt = con.prepareStatement("select true from generate_series(?, ?)")) {
      stmt.setLong(1, 1);
      stmt.setLong(2, 5L);
      long count = stmt.executeLargeUpdate();
      Assert.fail("A result was returned when none was expected. Returned: " + count);
    } catch (SQLException e) {
      Assert.assertEquals(PSQLState.TOO_MANY_RESULTS.getState(), e.getSQLState());
    }
  }

  /*
   * Test Statement.getLargeUpdateCount();
   */
  @Test
  public void testGetLargeUpdateCountStatementSELECT() throws Exception {
    try (Statement stmt = con.createStatement()) {
      boolean isResult = stmt.execute("select true from generate_series(1, 5)");
      Assert.assertTrue("True since this is a SELECT", isResult);
      long count = stmt.getLargeUpdateCount();
      long expected = -1L;
      Assert.assertEquals("-1 if the current result is a ResultSet object", expected, count);
    }
  }

  /*
   * Test PreparedStatement.getLargeUpdateCount();
   */
  @Test
  public void testGetLargeUpdateCountPreparedStatementSELECT() throws Exception {
    try (PreparedStatement stmt = con.prepareStatement("select true from generate_series(?, ?)")) {
      stmt.setLong(1, 1);
      stmt.setLong(2, 5L);
      boolean isResult = stmt.execute();
      Assert.assertTrue("True since this is a SELECT", isResult);
      long count = stmt.getLargeUpdateCount();
      long expected = -1L;
      Assert.assertEquals("-1 if the current result is a ResultSet object", expected, count);
    }
  }

  // ********************* BATCH LARGE UPDATES *********************

  /*
   * FINEST: batch execute 3 queries, handler=org.postgresql.jdbc.BatchResultHandler@3d04a311, maxRows=0, fetchSize=0, flags=21
   * FINEST: FE=> Parse(stmt=null,query="insert into largetable select true from generate_series($1, $2)",oids={23,23})
   * FINEST: FE=> Bind(stmt=null,portal=null,$1=<1>,$2=<200>)
   * FINEST: FE=> Describe(portal=null)
   * FINEST: FE=> Execute(portal=null,limit=1)
   * FINEST: FE=> Parse(stmt=null,query="insert into largetable select true from generate_series($1, $2)",oids={23,20})
   * FINEST: FE=> Bind(stmt=null,portal=null,$1=<1>,$2=<3000000000>)
   * FINEST: FE=> Describe(portal=null)
   * FINEST: FE=> Execute(portal=null,limit=1)
   * FINEST: FE=> Parse(stmt=null,query="insert into largetable select true from generate_series($1, $2)",oids={23,23})
   * FINEST: FE=> Bind(stmt=null,portal=null,$1=<1>,$2=<50>)
   * FINEST: FE=> Describe(portal=null)
   * FINEST: FE=> Execute(portal=null,limit=1)
   * FINEST: FE=> Sync
   * FINEST: <=BE ParseComplete [null]
   * FINEST: <=BE BindComplete [unnamed]
   * FINEST: <=BE NoData
   * FINEST: <=BE CommandStatus(INSERT 0 200)
   * FINEST: <=BE ParseComplete [null]
   * FINEST: <=BE BindComplete [unnamed]
   * FINEST: <=BE NoData
   * FINEST: <=BE CommandStatus(INSERT 0 3000000000)
   * FINEST: <=BE ParseComplete [null]
   * FINEST: <=BE BindComplete [unnamed]
   * FINEST: <=BE NoData
   * FINEST: <=BE CommandStatus(INSERT 0 50)
   */

  /*
   * Warning: BIG and SLOW, it's ignored.
   * Test simple PreparedStatement.executeLargeBatch();
   */
  @Ignore
  @Test
  public void testExecuteLargeBatchStatementBIG() throws Exception {
    con.setAutoCommit(false);
    try (PreparedStatement stmt = con.prepareStatement("insert into largetable "
        + "select true from generate_series(?, ?)")) {
      stmt.setInt(1, 1);
      stmt.setInt(2, 200);
      stmt.addBatch(); // statement one
      stmt.setInt(1, 1);
      stmt.setLong(2, 3_000_000_000L);
      stmt.addBatch(); // statement two
      stmt.setInt(1, 1);
      stmt.setInt(2, 50);
      stmt.addBatch(); // statement three
      long[] actual = stmt.executeLargeBatch();
      Assert.assertArrayEquals("Large rows inserted via 3 batch", new long[]{200L, 3_000_000_000L, 50L}, actual);
    }
    con.setAutoCommit(true);
  }

  /*
   * Test simple Statement.executeLargeBatch();
   */
  @Test
  public void testExecuteLargeBatchStatementSMALL() throws Exception {
    try (Statement stmt = con.createStatement()) {
      stmt.addBatch("insert into largetable(a) select true"); // statement one
      stmt.addBatch("insert into largetable select false"); // statement two
      stmt.addBatch("insert into largetable(a) values(true)"); // statement three
      stmt.addBatch("insert into largetable values(false)"); // statement four
      long[] actual = stmt.executeLargeBatch();
      Assert.assertArrayEquals("Rows inserted via 4 batch", new long[]{1L, 1L, 1L, 1L}, actual);
    }
  }

  /*
   * Test simple PreparedStatement.executeLargeBatch();
   */
  @Test
  public void testExecuteLargePreparedStatementStatementSMALL() throws Exception {
    try (PreparedStatement stmt = con.prepareStatement("insert into largetable "
        + "select true from generate_series(?, ?)")) {
      stmt.setInt(1, 1);
      stmt.setInt(2, 200);
      stmt.addBatch(); // statement one
      stmt.setInt(1, 1);
      stmt.setInt(2, 100);
      stmt.addBatch(); // statement two
      stmt.setInt(1, 1);
      stmt.setInt(2, 50);
      stmt.addBatch(); // statement three
      stmt.addBatch(); // statement four, same parms as three
      long[] actual = stmt.executeLargeBatch();
      Assert.assertArrayEquals("Rows inserted via 4 batch", new long[]{200L, 100L, 50L, 50L}, actual);
    }
  }

  /*
   * Test loop PreparedStatement.executeLargeBatch();
   */
  @Test
  public void testExecuteLargePreparedStatementStatementLoopSMALL() throws Exception {
    long[] loop = {200, 100, 50, 300, 20, 60, 2};
    try (PreparedStatement stmt = con.prepareStatement("insert into largetable "
        + "select true from generate_series(?, ?)")) {
      for (long i : loop) {
        stmt.setInt(1, 1);
        stmt.setLong(2, i);
        stmt.addBatch();
      }
      long[] actual = stmt.executeLargeBatch();
      Assert.assertArrayEquals("Rows inserted via 7 batch", loop, actual);
    }
  }

  /*
   * Test null PreparedStatement.executeLargeBatch();
   */
  @Test
  public void testNullExecuteLargeBatchStatement() throws Exception {
    try (Statement stmt = con.createStatement()) {
      long[] actual = stmt.executeLargeBatch();
      Assert.assertArrayEquals("addBatch() not called batchStatements is null", new long[0], actual);
    }
  }

  /*
   * Test empty PreparedStatement.executeLargeBatch();
   */
  @Test
  public void testEmptyExecuteLargeBatchStatement() throws Exception {
    try (Statement stmt = con.createStatement()) {
      stmt.addBatch("");
      stmt.clearBatch();
      long[] actual = stmt.executeLargeBatch();
      Assert.assertArrayEquals("clearBatch() called, batchStatements.isEmpty()", new long[0], actual);
    }
  }

  /*
   * Test null PreparedStatement.executeLargeBatch();
   */
  @Test
  public void testNullExecuteLargeBatchPreparedStatement() throws Exception {
    try (PreparedStatement stmt = con.prepareStatement("")) {
      long[] actual = stmt.executeLargeBatch();
      Assert.assertArrayEquals("addBatch() not called batchStatements is null", new long[0], actual);
    }
  }

  /*
   * Test empty PreparedStatement.executeLargeBatch();
   */
  @Test
  public void testEmptyExecuteLargeBatchPreparedStatement() throws Exception {
    try (PreparedStatement stmt = con.prepareStatement("")) {
      stmt.addBatch();
      stmt.clearBatch();
      long[] actual = stmt.executeLargeBatch();
      Assert.assertArrayEquals("clearBatch() called, batchStatements.isEmpty()", new long[0], actual);
    }
  }

}
