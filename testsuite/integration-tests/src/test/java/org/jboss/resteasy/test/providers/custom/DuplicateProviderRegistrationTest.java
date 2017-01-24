package org.jboss.resteasy.test.providers.custom;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.category.NotForForwardCompatibility;
import org.jboss.resteasy.test.providers.custom.resource.DuplicateProviderRegistrationFeature;
import org.jboss.resteasy.test.providers.custom.resource.DuplicateProviderRegistrationFilter;
import org.jboss.resteasy.test.providers.custom.resource.DuplicateProviderRegistrationInterceptor;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.ReaderInterceptor;

/**
 * @tpSubChapter Providers
 * @tpChapter Integration tests
 * @tpTestCaseDetails Regression test for JBEAP-4703
 * @tpSince RESTEasy 3.0.17
 */
@RunWith(Arquillian.class)
public class DuplicateProviderRegistrationTest {

    private static final String ERR_MSG = "Wrong cound of RESTEASY002155 warning message";
    @SuppressWarnings(value = "unchecked")
    @Deployment
    public static Archive<?> createTestArchive() {
        WebArchive war = TestUtil.prepareArchive(DuplicateProviderRegistrationTest.class.getSimpleName());
        war.addClasses(DuplicateProviderRegistrationFeature.class, DuplicateProviderRegistrationFilter.class,
                TestUtil.class, DuplicateProviderRegistrationInterceptor.class);
        war.addClass(NotForForwardCompatibility.class);
        return TestUtil.finishContainerPrepare(war, null, (Class<?>[]) null);
    }

    private static int getWarningCount() {
        return TestUtil.getWarningCount("RESTEASY002155", true);
    }

    /**
     * @tpTestDetails Basic test
     * @tpSince RESTEasy 3.0.17
     */
    @Test
    @Category({NotForForwardCompatibility.class})
    public void testDuplicateProvider() {
        int initWarningCount = getWarningCount();
        Client client = ClientBuilder.newClient();
        try {
            WebTarget webTarget = client.target("http://www.changeit.com");
            // DuplicateProviderRegistrationFeature will be registered third on the same webTarget even if
            //   webTarget.getConfiguration().isRegistered(DuplicateProviderRegistrationFeature.class)==true
            webTarget.register(DuplicateProviderRegistrationFeature.class).register(new DuplicateProviderRegistrationFeature()).register(new DuplicateProviderRegistrationFeature());
        } finally {
            client.close();
        }
        Assert.assertEquals(ERR_MSG, 2, getWarningCount() - initWarningCount);
    }

    /**
     * @tpTestDetails This test is taken from javax.ws.rs.core.Configurable javadoc
     * @tpSince RESTEasy 3.0.17
     */
    @Test
    @Category({NotForForwardCompatibility.class})
    public void testFromJavadoc() {
        int initWarningCount = getWarningCount();
        Client client = ClientBuilder.newClient();
        try {
            WebTarget webTarget = client.target("http://www.changeit.com");
            webTarget.register(DuplicateProviderRegistrationInterceptor.class, ReaderInterceptor.class);
            webTarget.register(DuplicateProviderRegistrationInterceptor.class);       // Rejected by runtime.
            webTarget.register(new DuplicateProviderRegistrationInterceptor());       // Rejected by runtime.
            webTarget.register(DuplicateProviderRegistrationInterceptor.class, 6500); // Rejected by runtime.

            webTarget.register(new DuplicateProviderRegistrationFeature());
            webTarget.register(DuplicateProviderRegistrationFeature.class);   // rejected by runtime.
            webTarget.register(DuplicateProviderRegistrationFeature.class, Feature.class);  // Rejected by runtime.
        } finally {
            client.close();
        }
        Assert.assertEquals(ERR_MSG, 5, getWarningCount() - initWarningCount);
    }
}