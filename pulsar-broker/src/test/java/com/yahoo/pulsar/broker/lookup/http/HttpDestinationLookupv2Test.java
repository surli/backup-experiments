/**
 * Copyright 2016 Yahoo Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yahoo.pulsar.broker.lookup.http;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.yahoo.pulsar.broker.PulsarService;
import com.yahoo.pulsar.broker.ServiceConfiguration;
import com.yahoo.pulsar.broker.admin.AdminResource;
import com.yahoo.pulsar.broker.authorization.AuthorizationManager;
import com.yahoo.pulsar.broker.cache.ConfigurationCacheService;
import com.yahoo.pulsar.broker.lookup.DestinationLookup;
import com.yahoo.pulsar.broker.lookup.NamespaceData;
import com.yahoo.pulsar.broker.lookup.RedirectData;
import com.yahoo.pulsar.broker.namespace.NamespaceService;
import com.yahoo.pulsar.broker.service.BrokerService;
import com.yahoo.pulsar.broker.web.PulsarWebResource;
import com.yahoo.pulsar.broker.web.RestException;
import com.yahoo.pulsar.common.policies.data.ClusterData;
import com.yahoo.pulsar.common.policies.data.Policies;
import com.yahoo.pulsar.zookeeper.ZooKeeperChildrenCache;
import com.yahoo.pulsar.zookeeper.ZooKeeperDataCache;

/**
 * HTTP lookup unit tests.
 *
 *
 */
public class HttpDestinationLookupv2Test {

    private PulsarService pulsar;
    private NamespaceService ns;
    private AuthorizationManager auth;
    private ServiceConfiguration config;
    private ConfigurationCacheService mockConfigCache;
    private ZooKeeperChildrenCache clustersListCache;
    private ZooKeeperDataCache<ClusterData> clustersCache;
    private ZooKeeperDataCache<Policies> policiesCache;
    private Set<String> clusters;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp() throws Exception {
        pulsar = mock(PulsarService.class);
        ns = mock(NamespaceService.class);
        auth = mock(AuthorizationManager.class);
        mockConfigCache = mock(ConfigurationCacheService.class);
        clustersListCache = mock(ZooKeeperChildrenCache.class);
        clustersCache = mock(ZooKeeperDataCache.class);
        policiesCache = mock(ZooKeeperDataCache.class);
        config = spy(new ServiceConfiguration());
        config.setClusterName("use");
        clusters = new TreeSet<String>();
        clusters.add("use");
        clusters.add("usc");
        clusters.add("usw");
        ClusterData useData = new ClusterData("http://broker.messaging.use.example.com:8080");
        ClusterData uscData = new ClusterData("http://broker.messaging.usc.example.com:8080");
        ClusterData uswData = new ClusterData("http://broker.messaging.usw.example.com:8080");
        doReturn(config).when(pulsar).getConfiguration();
        doReturn(mockConfigCache).when(pulsar).getConfigurationCache();
        doReturn(clustersListCache).when(mockConfigCache).clustersListCache();
        doReturn(clustersCache).when(mockConfigCache).clustersCache();
        doReturn(policiesCache).when(mockConfigCache).policiesCache();
        doReturn(useData).when(clustersCache).get(AdminResource.path("clusters", "use"));
        doReturn(uscData).when(clustersCache).get(AdminResource.path("clusters", "usc"));
        doReturn(uswData).when(clustersCache).get(AdminResource.path("clusters", "usw"));
        doReturn(clusters).when(clustersListCache).get();
        doReturn(ns).when(pulsar).getNamespaceService();
        BrokerService brokerService = mock(BrokerService.class);
        doReturn(brokerService).when(pulsar).getBrokerService();
        doReturn(auth).when(brokerService).getAuthorizationManager();
    }

    @Test
    public void crossColoLookup() throws Exception {

        DestinationLookup destLookup = spy(new DestinationLookup());
        destLookup.setPulsar(pulsar);
        doReturn("null").when(destLookup).clientAppId();
        Field uriField = PulsarWebResource.class.getDeclaredField("uri");
        uriField.setAccessible(true);
        UriInfo uriInfo = mock(UriInfo.class);
        uriField.set(destLookup, uriInfo);
        URI uri = URI.create("http://localhost:8080/lookup/v2/destination/topic/myprop/usc/ns2/topic1");
        doReturn(uri).when(uriInfo).getRequestUri();
        doReturn(true).when(config).isAuthorizationEnabled();
        try {
            destLookup.lookupDestination("myprop", "usc", "ns2", "topic1", false);
            fail("Should have raised exception to redirect request");
        } catch (WebApplicationException wae) {
            // OK
            assertEquals(wae.getResponse().getStatus(), Status.TEMPORARY_REDIRECT.getStatusCode());
        }
    }

    @Test
    public void testValidateReplicationSettingsOnNamespace() throws Exception {

        final String property = "my-prop";
        final String cluster = "global";
        final String ns1 = "ns1";
        final String ns2 = "ns2";
        Policies policies1 = new Policies();
        doReturn(policies1).when(policiesCache).get(AdminResource.path("policies", property, cluster, ns1));
        Policies policies2 = new Policies();
        policies2.replication_clusters = Lists.newArrayList("invalid-localCluster");
        doReturn(policies2).when(policiesCache).get(AdminResource.path("policies", property, cluster, ns2));

        DestinationLookup destLookup = spy(new DestinationLookup());
        destLookup.setPulsar(pulsar);
        doReturn("null").when(destLookup).clientAppId();
        Field uriField = PulsarWebResource.class.getDeclaredField("uri");
        uriField.setAccessible(true);
        UriInfo uriInfo = mock(UriInfo.class);
        uriField.set(destLookup, uriInfo);
        doReturn(false).when(config).isAuthorizationEnabled();
        try {
            destLookup.lookupDestination(property, cluster, ns1, "empty-cluster", false);
            fail("Should have raised exception to redirect request");
        } catch (RestException e) {
            // OK
        }

        try {
            destLookup.lookupDestination(property, cluster, ns2, "invalid-localCluster", false);
            fail("Should have raised exception for invalid cluster");
        } catch (RestException e) {
            // OK
        }
    }

    @Test
    public void testDataPojo() {
        final String url = "localhost:8080";
        NamespaceData data1 = new NamespaceData(url);
        assertEquals(data1.getBrokerUrl(), url);
        RedirectData data2 = new RedirectData(url);
        assertEquals(data2.getRedirectLookupAddress(), url);
    }

}
