/*
 * Copyright 2005-2017 Dozer Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dozer.builder.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.MappingException;
import org.dozer.config.BeanContainer;
import org.dozer.osgi.Activator;
import org.dozer.util.DozerClassLoader;
import org.dozer.util.MappingUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the schema specified in the mapping.xml.
 * Attempts to find locally on the classpath or by resolving the URL.
 **/
public class SchemaLSResourceResolver implements LSResourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaLSResourceResolver.class);

    private static final String VERSION_5_XSD = "http://dozer.sourceforge.net/schema/beanmapping.xsd";
    private static final String VERSION_6_XSD = "http://dozermapper.github.io/schema/bean-mapping.xsd";

    private final BeanContainer beanContainer;

    public SchemaLSResourceResolver(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        LOG.info("Trying to resolve XML entity with public ID [{}] and system ID [{}]", publicId, systemId);

        isValidSystemID(systemId);

        InputStream source = null;
        try {
            source = resolveFromClassPath(systemId);
        } catch (IOException exClasspath) {
            LOG.error("{}", exClasspath.getMessage());
            LOG.debug("Exception: {}", exClasspath);

            try {
                source = resolveFromURL(systemId);
            } catch (IOException exURL) {
                MappingUtils.throwMappingException(exURL);
            }
        }

        InputStreamLSInput lsInput = null;
        try {
            lsInput = new InputStreamLSInput(publicId, systemId, baseURI, new StringReader(IOUtils.toString(source, Charset.forName("UTF-8"))));

            LOG.info("Resolved public ID [{}] and system ID [{}]", publicId, systemId);
        } catch (IOException ex) {
            MappingUtils.throwMappingException("Could not resolve bean-mapping XML Schema [" + systemId + "]", ex);
        } finally {
            IOUtils.closeQuietly(source);
        }

        return lsInput;
    }

    private void isValidSystemID(String systemId) throws MappingException {
        if (StringUtils.isBlank(systemId)) {
            MappingUtils.throwMappingException("System ID is empty. Expected: " + VERSION_6_XSD
                                               + "'. Please see migration guide @ https://dozermapper.github.io/gitbook");
        }

        if (VERSION_5_XSD.equalsIgnoreCase(systemId)) {
            MappingUtils.throwMappingException("Dozer >= v6.0.0 uses a new XSD location. Your current config needs to be upgraded. "
                                               + "Found v5 XSD: '"
                                               + VERSION_5_XSD
                                               + "'. Expected v6 XSD: '"
                                               + VERSION_6_XSD
                                               + "'. Please see migration guide @ https://dozermapper.github.io/gitbook");
        }
    }

    /**
     * 1. Checks if DozerClassloader (i.e.: maybe the user is using a local XSD for some reason)
     * 2. Trys classloader for dozer-schema.jar
     * 3. Trys bundle context for dozer-schema.jar
     *
     * @param systemId systemId used by XSD
     * @return stream to XSD
     * @throws IOException if fails to find XSD
     */
    private InputStream resolveFromClassPath(String systemId) throws IOException {
        InputStream source;

        String xsdPath = systemId.substring(systemId.indexOf("/", "http://".length()) + 1);

        ClassLoader localClassLoader = getClass().getClassLoader();

        LOG.debug("Trying to locate [{}] via ClassLoader [{}]", xsdPath, localClassLoader.getClass().getSimpleName());

        //Attempt to find within this JAR
        URL url = localClassLoader.getResource("schema/" + xsdPath);
        if (url == null) {
            //Attempt to find via user defined class loader
            DozerClassLoader dozerClassLoader = beanContainer.getClassLoader();

            LOG.debug("Trying to locate [{}] via DozerClassLoader [{}]", xsdPath, dozerClassLoader.getClass().getSimpleName());

            url = dozerClassLoader.loadResource(xsdPath);
        }

        if (url == null) {
            //Attempt to find via OSGi
            Bundle bundle = Activator.getBundle();
            if (bundle != null) {
                LOG.debug("Trying to locate [{}] via Bundle [{}]", xsdPath, bundle.getClass().getSimpleName());

                url = bundle.getResource("schema/" + xsdPath);
            }
        }

        if (url == null) {
            throw new IOException("Could not resolve bean-mapping XML Schema [" + systemId + "]: not found in classpath; " + xsdPath);
        }

        try {
            source = url.openStream();
        } catch (IOException ex) {
            throw new IOException("Could not resolve bean-mapping XML Schema [" + systemId + "]: not found in classpath; " + xsdPath, ex);
        }

        LOG.debug("Found bean-mapping XML Schema [{}] in classpath @ [{}]", systemId, url.toString());

        return source;
    }

    /**
     * Attempts to open a http connection for the systemId resource, and follows the first redirect
     *
     * @param systemId url to the XSD
     * @return stream to XSD
     * @throws IOException if fails to find XSD
     */
    private InputStream resolveFromURL(String systemId) throws IOException {
        LOG.debug("Trying to download [{}]", systemId);

        URL obj = new URL(systemId);
        HttpURLConnection conn = (HttpURLConnection)obj.openConnection();

        int status = conn.getResponseCode();
        if ((status != HttpURLConnection.HTTP_OK) &&
            (status == HttpURLConnection.HTTP_MOVED_TEMP
             || status == HttpURLConnection.HTTP_MOVED_PERM
             || status == HttpURLConnection.HTTP_SEE_OTHER)) {

            LOG.debug("Received status of {}, attempting to follow Location header", status);

            String newUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection)new URL(newUrl).openConnection();
        }

        return conn.getInputStream();
    }
}
