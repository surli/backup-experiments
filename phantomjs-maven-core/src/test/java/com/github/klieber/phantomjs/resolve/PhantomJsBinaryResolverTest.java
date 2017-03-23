/*
 * Copyright (c) 2014 Kyle Lieber
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.klieber.phantomjs.resolve;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

@RunWith(MockitoJUnitRunner.class)
public class PhantomJsBinaryResolverTest {

  private static final String PROJECT_ROOT = System.getProperty("user.dir");

  private static File phantomJsHome;

  @Before
  public void before() {
    assumeUnixOs();
  }

  @BeforeClass
  public static void beforeClass() throws FileNotFoundException {
    phantomJsHome = new File(PROJECT_ROOT+"/target/", "phantomjs");
    PrintWriter writer = new PrintWriter(phantomJsHome);
    writer.println("#!/bin/sh");
    writer.println("echo 1.9.0");
    writer.close();
    phantomJsHome.setExecutable(true);
  }

  @AfterClass
  public static void afterClass() {
    if (phantomJsHome != null && phantomJsHome.exists()) {
      phantomJsHome.delete();
    }
  }

  @Test
  public void testShouldNotResolve() {
    assertNull(getResolver("1.9.2").resolve("/tmp"));
  }

  @Test
  public void testShouldResolveBin() {
    assertEquals(phantomJsHome.getAbsolutePath(), getResolver("1.9.0").resolve(phantomJsHome.getParent()));
  }

  @Test
  public void testShouldResolveBinWrongVersion() {
    assertNull(getResolver("1.9.2", true).resolve(phantomJsHome.getParent()));
  }

  private PhantomJsBinaryResolver getResolver(String version) {
    return getResolver(version, false);
  }

  private PhantomJsBinaryResolver getResolver(String version, boolean enforce) {
    return new PhantomJsBinaryResolver(version, enforce);
  }

  private void assumeUnixOs() {
    String os = System.getProperty("os.name").toLowerCase();
    assumeTrue(os.contains("nux") || os.contains("mac"));
  }
}
