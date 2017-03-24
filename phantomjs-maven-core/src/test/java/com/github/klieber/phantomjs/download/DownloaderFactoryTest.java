package com.github.klieber.phantomjs.download;

import com.github.klieber.phantomjs.archive.PhantomJSArchive;
import com.github.klieber.phantomjs.locate.PhantomJsLocatorOptions;
import com.github.klieber.phantomjs.locate.RepositoryDetails;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloaderFactoryTest {

  @Mock
  private PhantomJsLocatorOptions options;

  @Mock
  private RepositoryDetails repositoryDetails;

  @Mock
  private PhantomJSArchive phantomJSArchive;

  @InjectMocks
  private DownloaderFactory downloaderFactory;

  public void test() {
    downloaderFactory.getDownloader(phantomJSArchive);
  }

}
