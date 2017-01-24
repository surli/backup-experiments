package org.jboss.resteasy.test.providers.jackson2;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.test.providers.jackson2.resource.ExceptionMapperMarshalErrorMessage;
import org.jboss.resteasy.test.providers.jackson2.resource.ExceptionMapperMarshalMyCustomException;
import org.jboss.resteasy.test.providers.jackson2.resource.ExceptionMapperMarshalName;
import org.jboss.resteasy.test.providers.jackson2.resource.ExceptionMapperMarshalMyCustomExceptionMapper;
import org.jboss.resteasy.test.providers.jackson2.resource.ExceptionMapperMarshalResource;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.jboss.resteasy.util.Types;
import org.jboss.resteasy.utils.PortProviderUtil;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @tpSubChapter Jackson2 provider
 * @tpChapter Integration tests
 * @tpTestCaseDetails Regression test for RESTEASY-937
 * @tpSince RESTEasy 3.0.16
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ExceptionMapperMarshalTest {

    protected static final Logger logger = Logger.getLogger(ProxyWithGenericReturnTypeJacksonTest.class.getName());
    static ResteasyClient client;

    @Deployment
    public static Archive<?> deploy() {
        WebArchive war = TestUtil.prepareArchive(ProxyWithGenericReturnTypeJacksonTest.class.getSimpleName());
        war.addClass(Jackson2Test.class);
        return TestUtil.finishContainerPrepare(war, null, ExceptionMapperMarshalErrorMessage.class, ExceptionMapperMarshalMyCustomException.class,
                ExceptionMapperMarshalMyCustomExceptionMapper.class, ExceptionMapperMarshalName.class, ExceptionMapperMarshalResource.class);
    }

    @Before
    public void init() {
        client = new ResteasyClientBuilder().build();
    }

    @After
    public void after() throws Exception {
        client.close();
    }

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, ProxyWithGenericReturnTypeJacksonTest.class.getSimpleName());
    }

    /**
     * @tpTestDetails Tests usage of custom ExceptionMapper producing json response
     * @tpPassCrit The resource returns Success response
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    public void testCustomUsed() {
        Type exceptionType = Types.getActualTypeArgumentsOfAnInterface(ExceptionMapperMarshalMyCustomExceptionMapper.class, ExceptionMapper.class)[0];
        Assert.assertEquals(ExceptionMapperMarshalMyCustomException.class, exceptionType);

        Response response = client.target(generateURL("/resource/custom")).request().get();
        Assert.assertEquals(response.getStatus(), HttpResponseCodes.SC_OK);
        List<ExceptionMapperMarshalErrorMessage> errors = response.readEntity(new GenericType<List<ExceptionMapperMarshalErrorMessage>>() {
        });
        Assert.assertEquals("The response has unexpected content", "error", errors.get(0).getError());
    }
}
