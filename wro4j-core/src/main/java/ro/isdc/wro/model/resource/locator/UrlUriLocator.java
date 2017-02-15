/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;


/**
 * UriLocator capable to read the resources from some URL. Usually, this uriLocator will be the last in the chain of
 * uriLocators.
 *
 * @author Alex Objelean
 * @created Created on Nov 10, 2008
 */
public class UrlUriLocator extends WildcardUriLocatorSupport {
  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "uri";
  private int timeout = WroConfiguration.DEFAULT_CONNECTION_TIMEOUT;
  /**
   * {@inheritDoc}
   */
  public boolean accept(final String uri) {
    return isValid(uri);
  }

  /**
   * Check if a uri is a URL resource.
   *
   * @param uri to check.
   * @return true if the uri is a URL resource.
   */
  public static boolean isValid(final String uri) {
    // if creation of URL object doesn't throw an exception, the uri can be
    // accepted.
    try {
      new URL(uri);
    } catch (final MalformedURLException e) {
      return false;
    }
    return true;
  }


  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
    throws IOException {
    notNull(uri, "uri cannot be NULL!");
    if (getWildcardStreamLocator().hasWildcard(uri)) {
      final String fullPath = FilenameUtils.getFullPath(uri);
      final URL url = new URL(fullPath);
      return getWildcardStreamLocator().locateStream(uri, new File(URLDecoder.decode(url.getFile(), "UTF-8")));
    }
    final URL url = new URL(uri);
    final URLConnection connection = url.openConnection();
    // avoid jar file locking on Windows.
    connection.setUseCaches(false);
    //add explicit user agent header. This is required by some cdn resources which otherwise would return 403 status code.
    connection.setRequestProperty("User-Agent", "java");
    // setting these timeouts ensures the client does not deadlock indefinitely
    // when the server has problems.
    connection.setConnectTimeout(timeout);
    connection.setReadTimeout(timeout);

    return new BufferedInputStream(connection.getInputStream());
  }

  /**
   * The read & connect timeout value (in milliseconds) used to limit wait period to a reasonable value. By default
   * {@value WroConfiguration#DEFAULT_CONNECTION_TIMEOUT} ms is used.
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
}
