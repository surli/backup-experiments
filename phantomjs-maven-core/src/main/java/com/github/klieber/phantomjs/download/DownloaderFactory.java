package com.github.klieber.phantomjs.download;

import com.github.klieber.phantomjs.archive.PhantomJSArchive;
import com.github.klieber.phantomjs.cache.CachedFileFactory;
import com.github.klieber.phantomjs.locate.PhantomJsLocatorOptions;
import com.github.klieber.phantomjs.locate.RepositoryDetails;
import com.github.klieber.phantomjs.util.ArtifactBuilder;
import com.github.klieber.phantomjs.util.Predicate;
import com.github.klieber.phantomjs.util.Predicates;
import com.github.klieber.phantomjs.util.VersionUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DownloaderFactory {

  private static final String LEGACY_VERSION = "1.9.2";

  private static final Predicate<String> IS_LEGACY_VERSION = new Predicate<String>() {
    @Override
    public boolean apply(String version) {
      return VersionUtil.isGreaterThanOrEqualTo(LEGACY_VERSION, version);
    }
  };

  private static final String GOOGLE_CODE = "https://phantomjs.googlecode.com/files/";
  private static final String BITBUCKET = "https://bitbucket.org/ariya/phantomjs/downloads/";

  private final PhantomJsLocatorOptions options;
  private final RepositoryDetails repositoryDetails;

  public DownloaderFactory(PhantomJsLocatorOptions options,
                           RepositoryDetails repositoryDetails) {
    this.options = options;
    this.repositoryDetails = repositoryDetails;
  }

  public Downloader getDownloader(PhantomJSArchive phantomJSArchive) {
    ArtifactBuilder artifactBuilder = new ArtifactBuilder();
    CachedFileFactory cachedFileFactory = new CachedFileFactory(artifactBuilder);
    File cachedFile = cachedFileFactory.create(phantomJSArchive, repositoryDetails.getRepositorySystemSession());
    Downloader downloader;
    if (PhantomJsLocatorOptions.Source.REPOSITORY.equals(options.getSource())) {
      downloader = new RepositoryDownloader(artifactBuilder, repositoryDetails);
    } else if (options.getBaseUrl() == null) {
      Map<Downloader, Predicate<String>> rules = new HashMap<Downloader, Predicate<String>>();
      rules.put(new WebDownloader(GOOGLE_CODE, cachedFile), IS_LEGACY_VERSION);
      rules.put(new WebDownloader(BITBUCKET, cachedFile), Predicates.not(IS_LEGACY_VERSION));
      downloader = new RuleBasedDownloader(rules);
    } else {
      downloader = new WebDownloader(options.getBaseUrl(), cachedFile);
    }
    return downloader;
  }

}
