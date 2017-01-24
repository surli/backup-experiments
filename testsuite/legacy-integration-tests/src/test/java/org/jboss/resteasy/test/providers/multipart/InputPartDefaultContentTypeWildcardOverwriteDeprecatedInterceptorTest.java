package org.jboss.resteasy.test.providers.multipart;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.ClientRequest; //@cs-: clientrequest (Old client test)
import org.jboss.resteasy.client.ClientResponse; //@cs-: clientresponse (Old client test)
import org.jboss.resteasy.test.providers.multipart.resource.InputPartDefaultContentTypeWildcardOverwriteContainerBean;
import org.jboss.resteasy.test.providers.multipart.resource.InputPartDefaultContentTypeWildcardOverwriteOldInterceptor;
import org.jboss.resteasy.test.providers.multipart.resource.InputPartDefaultContentTypeWildcardOverwriteService;
import org.jboss.resteasy.test.providers.multipart.resource.InputPartDefaultContentTypeWildcardOverwriteXmlBean;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @tpSubChapter Multipart provider
 * @tpChapter Integration tests
 * @tpTestCaseDetails MultiPart provider should be able to process xml, if wildcard is set. Wildcard is set in deprecated version of interceptor.
 * @tpSince RESTEasy 3.0.16
 */
@RunWith(Arquillian.class)
@RunAsClient
public class InputPartDefaultContentTypeWildcardOverwriteDeprecatedInterceptorTest {

    public static final String WILDCARD_WITH_CHARSET_UTF_8 = MediaType.APPLICATION_XML + "; charset=UTF-8"; // this mediatype works correctly

    @Deployment
    public static Archive<?> createTestArchive() {
        WebArchive war = TestUtil.prepareArchive(InputPartDefaultContentTypeWildcardOverwriteDeprecatedInterceptorTest.class.getSimpleName());
        war.addClasses(InputPartDefaultContentTypeWildcardOverwriteContainerBean.class);
        war.addClasses(InputPartDefaultContentTypeWildcardOverwriteXmlBean.class, InputPartDefaultContentTypeWildcardOverwriteDeprecatedInterceptorTest.class);
        return TestUtil.finishContainerPrepare(war, null, InputPartDefaultContentTypeWildcardOverwriteOldInterceptor.class,
                InputPartDefaultContentTypeWildcardOverwriteService.class);
    }

    /**
     * @tpTestDetails Test for old client
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testContentTypeOldClient() throws Exception {
        String message = "--boo\r\n"
                + "Content-Disposition: form-data; name=\"foo\"\r\n"
                + "Content-Transfer-Encoding: 8bit\r\n\r\n"
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<inputPartDefaultContentTypeWildcardOverwriteXmlBean><myInt>27</myInt><myString>Lorem Ipsum</myString></inputPartDefaultContentTypeWildcardOverwriteXmlBean>\r\n"
                + "--boo--\r\n";

        ClientRequest request = new ClientRequest(PortProviderUtil.generateURL("/mime", InputPartDefaultContentTypeWildcardOverwriteDeprecatedInterceptorTest.class.getSimpleName()));

        request.body("multipart/form-data; boundary=boo", message.getBytes("utf-8"));
        ClientResponse<String> response = request.post(String.class);
        Assert.assertEquals("MultiPart provider is unable to process xml, if media type is set in interceptor",
                20, response.getStatus() / 10);
        Assert.assertEquals("Response text is wrong", "27", response.getEntity());
    }

    /**
     * @tpTestDetails Test for new client
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testContentTypeNewClient() throws Exception {
        String message = "--boo\r\n"
                + "Content-Disposition: form-data; name=\"foo\"\r\n"
                + "Content-Transfer-Encoding: 8bit\r\n\r\n"
                + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<inputPartDefaultContentTypeWildcardOverwriteXmlBean><myInt>27</myInt><myString>Lorem Ipsum</myString></inputPartDefaultContentTypeWildcardOverwriteXmlBean>\r\n"
                + "--boo--\r\n";



        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(PortProviderUtil.generateURL("/mime", InputPartDefaultContentTypeWildcardOverwriteDeprecatedInterceptorTest.class.getSimpleName()));
        Entity entity = Entity.entity(message, "multipart/form-data; boundary=boo");
        Response response = target.request().post(entity);

        Assert.assertEquals("MultiPart provider is unable to process xml, if media type is set in interceptor",
                20, response.getStatus() / 10);
        Assert.assertEquals("Response text is wrong", "27", response.readEntity(String.class));
    }
}
