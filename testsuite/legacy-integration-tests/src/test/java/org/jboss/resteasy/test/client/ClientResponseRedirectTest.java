package org.jboss.resteasy.test.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.ClientRequest; //@cs-: clientrequest (Old client test)
import org.jboss.resteasy.client.ClientResponse; //@cs-: clientresponse (Old client test)
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.test.client.resource.ClientResponseRedirectClientResponseOld;
import org.jboss.resteasy.test.client.resource.ClientResponseRedirectIntf;
import org.jboss.resteasy.test.client.resource.ClientResponseRedirectResource;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @tpSubChapter Resteasy-client
 * @tpChapter Integration tests
 * @tpSince RESTEasy 3.0.16
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ClientResponseRedirectTest extends ClientTestBase{


    protected static final Logger logger = LogManager.getLogger(ClientResponseRedirectTest.class.getName());
    static ResteasyClient client;

    @Deployment
    public static Archive<?> deploy() {
        WebArchive war = TestUtil.prepareArchive(ClientResponseRedirectTest.class.getSimpleName());
        war.addClass(ClientTestBase.class);
        return TestUtil.finishContainerPrepare(war, null, ClientResponseRedirectResource.class, PortProviderUtil.class);
    }

    @Before
    public void init() {
        client = new ResteasyClientBuilder().build();
    }

    @After
    public void after() throws Exception {
        client.close();
    }

    /**
     * @tpTestDetails Tests redirection of the request using deprecated ProxyFactory client
     * @tpPassCrit The header 'location' contains the redirected target
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testRedirectProxyFactory() throws Exception {
        testRedirect(ProxyFactory.create(ClientResponseRedirectClientResponseOld.class, PortProviderUtil.generateBaseUrl(ClientResponseRedirectTest.class.getSimpleName())).get());
    }

    /**
     * @tpTestDetails Tests redirection of the request using ProxyBuilder client
     * @tpPassCrit The header 'location' contains the redirected target
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testRedirectProxyBuilder() throws Exception {
        testRedirect(ProxyBuilder.builder(ClientResponseRedirectIntf.class, client.target(generateURL(""))).build().get());
    }

    /**
     * @tpTestDetails Tests redirection of the request using deprecated ClientRequest
     * @tpPassCrit The header 'location' contains the redirected target
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testRedirectClientRequest() throws Exception {
        testRedirect(new ClientRequest(generateURL("/redirect")).get());
    }

    /**
     * @tpTestDetails Tests redirection of the request using Client Webtarget request
     * @tpPassCrit The header 'location' contains the redirected target
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testRedirectClientTargetRequest() throws Exception {
        testRedirect(client.target(generateURL("/redirect")).request().get());
    }

    /**
     * @tpTestDetails Tests redirection of the request using HttpUrlConnection
     * @tpPassCrit The header 'location' contains the redirected target
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testRedirectHttpUrlConnection() throws Exception {
        URL url = PortProviderUtil.createURL("/redirect", ClientResponseRedirectTest.class.getSimpleName());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("GET");
        for (Object name : conn.getHeaderFields().keySet()) {
            logger.info(name);
        }
        logger.info("The response from the server was: " + conn.getResponseCode());
        Assert.assertEquals(HttpResponseCodes.SC_SEE_OTHER, conn.getResponseCode());
    }

    private void testRedirect(ClientResponse response) {
        MultivaluedMap headers = response.getResponseHeaders();
        logger.info("size: " + headers.size());
        for (Object name : headers.keySet()) {
            logger.info(name + ":" + headers.getFirst(name.toString()));
        }
        Assert.assertEquals("The location header doesn't have the expected value", generateURL("/redirect/data"), headers.getFirst("location"));
    }

    @SuppressWarnings(value = "unchecked")
    private void testRedirect(Response response) {
        MultivaluedMap headers = response.getHeaders();
        logger.info("size: " + headers.size());
        for (Object name : headers.keySet()) {
            logger.info(name + ":" + headers.getFirst(name.toString()));
        }
        Assert.assertEquals("The location header doesn't have the expected value", generateURL("/redirect/data"), headers.getFirst("location"));
    }

}
