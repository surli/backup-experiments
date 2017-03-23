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
package com.github.klieber.phantomjs.download;

import com.github.klieber.phantomjs.archive.PhantomJSArchive;
import com.github.klieber.phantomjs.util.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.hasMessage;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PhantomJSArchive.class)
public class RuleBasedDownloaderTest {

  private static final String VERSION = "1.2";

  @Mock
  private Predicate<String> predicateA;

  @Mock
  private Predicate<String> predicateB;

  @Mock
  private Downloader downloaderA;

  @Mock
  private Downloader downloaderB;

  @Mock
  private PhantomJSArchive phantomJsArchive;

  @Mock
  private File file;

  private RuleBasedDownloader ruleBasedDownloader;

  @Before
  public void before() {
    Map<Downloader, Predicate<String>> rules = new LinkedHashMap<Downloader, Predicate<String>>();
    rules.put(downloaderA, predicateA);
    rules.put(downloaderB, predicateB);

    this.ruleBasedDownloader = new RuleBasedDownloader(rules);
  }

  @Test
  public void shouldUseDownloaderA() throws Exception  {
    when(phantomJsArchive.getVersion()).thenReturn(VERSION);
    when(predicateA.apply(VERSION)).thenReturn(true);
    when(predicateB.apply(VERSION)).thenReturn(false);

    this.ruleBasedDownloader.download(phantomJsArchive);

    verify(downloaderA).download(phantomJsArchive);
    verifyNoMoreInteractions(downloaderB);
  }

  @Test
  public void shouldUseDownloaderB() throws Exception  {
    when(phantomJsArchive.getVersion()).thenReturn(VERSION);
    when(predicateA.apply(VERSION)).thenReturn(false);
    when(predicateB.apply(VERSION)).thenReturn(true);

    this.ruleBasedDownloader.download(phantomJsArchive);

    verify(downloaderB).download(phantomJsArchive);
    verifyNoMoreInteractions(downloaderA);
  }

  @Test
  public void shouldUseDownloaderBAfterAFails() throws Exception  {
    when(phantomJsArchive.getVersion()).thenReturn(VERSION);
    when(predicateA.apply(VERSION)).thenReturn(true);
    when(predicateB.apply(VERSION)).thenReturn(true);

    doThrow(new DownloadException("DownloaderA Failed")).when(downloaderA).download(phantomJsArchive);

    this.ruleBasedDownloader.download(phantomJsArchive);

    verify(downloaderA).download(phantomJsArchive);
    verify(downloaderB).download(phantomJsArchive);
  }

  @Test
  public void shouldFailDownloadDueToDownloaderAFailure() throws Exception  {
    when(phantomJsArchive.getVersion()).thenReturn(VERSION);
    when(predicateA.apply(VERSION)).thenReturn(true);
    when(predicateB.apply(VERSION)).thenReturn(false);

    doThrow(new DownloadException("DownloaderA Failed")).when(downloaderA).download(phantomJsArchive);

    catchException(this.ruleBasedDownloader).download(phantomJsArchive);
    assertThat(caughtException(), allOf(
        is(instanceOf(DownloadException.class)),
        hasMessage("DownloaderA Failed")
    ));

    verify(downloaderA).download(phantomJsArchive);
    verifyNoMoreInteractions(downloaderB);
  }

  @Test
  public void shouldFailDownloadDueToNoMatches() throws Exception  {
    when(phantomJsArchive.getVersion()).thenReturn(VERSION);
    when(predicateA.apply(VERSION)).thenReturn(false);
    when(predicateB.apply(VERSION)).thenReturn(false);

    catchException(this.ruleBasedDownloader).download(phantomJsArchive);

    assertThat(caughtException(),allOf(
        is(instanceOf(DownloadException.class)),
        hasMessage("No matching Downloader found.")
    ));

    verifyNoMoreInteractions(downloaderA);
    verifyNoMoreInteractions(downloaderB);
  }
}
