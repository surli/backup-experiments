package org.jboss.resteasy.test.providers.jackson2;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.resteasy.category.ExpectedFailing;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.test.core.basic.resource.ApplicationTestScannedApplication;
import org.jboss.resteasy.test.providers.jackson2.resource.JacksonDatatypeEndPoint;
import org.jboss.resteasy.test.providers.jackson2.resource.JacksonDatatypeJacksonProducer;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

/**
 * @tpSubChapter Jackson2 provider
 * @tpChapter Integration tests
 * @tpTestCaseDetails Test for WFLY-5916. Integration tests for jackson-datatype-jsr310 and jackson-datatype-jdk8 modules
 * @tpSince RESTEasy 3.1.0.CR3
 */
@RunWith(Arquillian.class)
@RunAsClient
// This test passes with WildFly master branch, but don't pass with WildFly 10.1
// Next annotation needs to be removed after new version of WildFly would be used - FIXME
@Category({ExpectedFailing.class})
public class JacksonDatatypeTest {
    private static final String DEFAULT_DEPLOYMENT = String.format("%sDefault",
            JacksonDatatypeTest.class.getSimpleName());
    private static final String DEPLOYMENT_WITH_DATATYPE = String.format("%sWithDatatypeSupport",
            JacksonDatatypeTest.class.getSimpleName());

    static ResteasyClient client;
    protected static final Logger logger = Logger.getLogger(JacksonDatatypeTest.class.getName());

    @BeforeClass
    public static void init() {
        client = new ResteasyClientBuilder().build();
    }

    @AfterClass
    public static void close() {
        client.close();
    }

    @Deployment(name = "default")
    public static Archive<?> deploy() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEFAULT_DEPLOYMENT + ".war");
        war.addClasses(ApplicationTestScannedApplication.class, JacksonDatatypeEndPoint.class);
        return war;
    }

    @Deployment(name = "withDatatype")
    public static Archive<?> deployJettison() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_WITH_DATATYPE + ".war");
        war.addClasses(JacksonDatatypeEndPoint.class,
                JacksonDatatypeJacksonProducer.class, ApplicationTestScannedApplication.class);
        return war;
    }

    private String requestHelper(String endPath, String deployment) {
        String url = PortProviderUtil.generateURL(String.format("/scanned/%s", endPath), deployment);
        WebTarget base = client.target(url);
        Response response = base.request().get();
        Assert.assertEquals(HttpResponseCodes.SC_OK, response.getStatus());
        String strResponse = response.readEntity(String.class);
        logger.info(String.format("Url: %s", url));
        logger.info(String.format("Response: %s", strResponse));
        return strResponse;
    }

    /**
     * @tpTestDetails Check string type without datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeNotSupportedString() throws Exception {
        String strResponse = requestHelper("string", DEFAULT_DEPLOYMENT);
        Assert.assertThat("Wrong conversion of String", strResponse, containsString("someString"));
    }

    /**
     * @tpTestDetails Check date type without datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeNotSupportedDate() throws Exception {
        String strResponse = requestHelper("date", DEFAULT_DEPLOYMENT);
        Assert.assertThat("Wrong conversion of Date", strResponse.matches("^[0-9]*$"), is(true));
    }

    /**
     * @tpTestDetails Check duration type without datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeNotSupportedDuration() throws Exception {
        String strResponse = requestHelper("duration", DEFAULT_DEPLOYMENT);
        Assert.assertThat("Wrong conversion of Duration", strResponse, not(containsString("PT5.000000006S")));
    }

    /**
     * @tpTestDetails Check null optional type without datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeNotSupportedOptionalNull() throws Exception {
        String strResponse = requestHelper("optional/true", DEFAULT_DEPLOYMENT);
        Assert.assertThat("Wrong conversion of Optional (null)", strResponse, not(containsString("null")));
    }

    /**
     * @tpTestDetails Check not null optional type without datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeNotSupportedOptionalNotNull() throws Exception {
        String strResponse = requestHelper("optional/false", DEFAULT_DEPLOYMENT);
        Assert.assertThat("Wrong conversion of Optional (not null)", strResponse, not(containsString("info@example.com")));
    }

    /**
     * @tpTestDetails Check string type with datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeSupportedString() throws Exception {
        String strResponse = requestHelper("string", DEPLOYMENT_WITH_DATATYPE);
        Assert.assertThat("Wrong conversion of String", strResponse, containsString("someString"));
    }

    /**
     * @tpTestDetails Check date type with datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeSupportedDate() throws Exception {
        String strResponse = requestHelper("date", DEPLOYMENT_WITH_DATATYPE);
        Assert.assertThat("Wrong conversion of Date", strResponse.matches("^[0-9]*$"), is(false));
    }

    /**
     * @tpTestDetails Check duration type with datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeSupportedDuration() throws Exception {
        String strResponse = requestHelper("duration", DEPLOYMENT_WITH_DATATYPE);
        Assert.assertThat("Wrong conversion of Duration", strResponse, containsString("PT5.000000006S"));
    }

    /**
     * @tpTestDetails Check null optional type with datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeSupportedOptionalNull() throws Exception {
        String strResponse = requestHelper("optional/true", DEPLOYMENT_WITH_DATATYPE);
        Assert.assertThat("Wrong conversion of Optional (null)", strResponse, containsString("null"));
    }

    /**
     * @tpTestDetails Check not null optional type with datatype supported
     * @tpSince RESTEasy 3.1.0.CR3
     */
    @Test
    public void testDatatypeSupportedOptionalNotNull() throws Exception {
        String strResponse = requestHelper("optional/false", DEPLOYMENT_WITH_DATATYPE);
        Assert.assertThat("Wrong conversion of Optional (not null)", strResponse, containsString("info@example.com"));
    }
}
