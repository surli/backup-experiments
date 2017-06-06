/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.cloud.dataflow.server.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.dataflow.core.ApplicationType;
import org.springframework.cloud.dataflow.registry.AppRegistration;
import org.springframework.cloud.dataflow.registry.AppRegistry;
import org.springframework.cloud.dataflow.server.configuration.TestDependencies;
import org.springframework.cloud.dataflow.server.registry.DataFlowAppRegistryPopulator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Gunnar Hillert
 * @author Ilayaperumal Gopinathan
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestDependencies.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class AppRegistryControllerTests {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private AppRegistry appRegistry;

	@Autowired
	private DataFlowAppRegistryPopulator uriRegistryPopulator;

	@Before
	public void setupMocks() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
				.defaultRequest(get("/").accept(MediaType.APPLICATION_JSON)).build();
		for (AppRegistration appRegistration : this.appRegistry.findAll()) {
			this.appRegistry.delete(appRegistration.getName(), appRegistration.getType());
		}
		this.uriRegistryPopulator.afterPropertiesSet();
	}

	@Test
	public void testRegisterApplication() throws Exception {
		mockMvc.perform(post("/apps/processor/blubba").param("uri", "file:///foo").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isCreated());
		assertThat(appRegistry.find("blubba", ApplicationType.processor).getUri().toString(), is("file:///foo"));
	}

	@Test
	public void testRegisterApplicationTwice() throws Exception {
		mockMvc.perform(post("/apps/processor/blubba").param("uri", "file:///foo").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isCreated());
		mockMvc.perform(post("/apps/processor/blubba").param("uri", "file:///foo").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isConflict());
	}

	@Test
	public void testRegisterFromPropertiesFile() throws Exception {
		mockMvc.perform(
				post("/apps").param("uri", "classpath:app-registry.properties").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isCreated());
		assertThat(appRegistry.find("foo", ApplicationType.sink).getUri().toString(), is("file:///bar"));
		assertThat(appRegistry.find("foo", ApplicationType.sink).getMetadataUri().toString(),
				is("file:///bar-metadata"));
		assertThat(appRegistry.find("bar", ApplicationType.source).getUri().toString(), is("file:///foo"));
	}

	@Test
	public void testRegisterAll() throws Exception {
		mockMvc.perform(post("/apps").param("apps", "sink.foo=file:///bar").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isCreated());
		assertThat(appRegistry.find("foo", ApplicationType.sink).getUri().toString(), is("file:///bar"));
	}

	@Test
	public void testRegisterAllWithoutForce() throws Exception {
		appRegistry.importAll(false, new ClassPathResource("META-INF/test-apps-overwrite.properties"));
		assertThat(appRegistry.find("time", ApplicationType.source).getUri().toString(),
				is("maven://org" + ".springframework.cloud.stream.app:time-source-rabbit:1.0.0.BUILD-SNAPSHOT"));
		assertThat(appRegistry.find("filter", ApplicationType.processor).getUri().toString(),
				is("maven://org" + ".springframework.cloud.stream.app:filter-processor-rabbit:1.0.0.BUILD-SNAPSHOT"));
		assertThat(appRegistry.find("log", ApplicationType.sink).getUri().toString(),
				is("maven://org.springframework" + ".cloud.stream.app:log-sink-rabbit:1.0.0.BUILD-SNAPSHOT"));
		assertThat(appRegistry.find("timestamp", ApplicationType.task).getUri().toString(),
				is("maven://org" + ".springframework.cloud.task.app:timestamp-task:1.0.0.BUILD-SNAPSHOT"));
	}

	@Test
	public void testRegisterAllWithForce() throws Exception {
		appRegistry.importAll(true, new ClassPathResource("META-INF/test-apps-overwrite.properties"));
		assertThat(appRegistry.find("time", ApplicationType.source).getUri().toString(),
				is("maven://org" + ".springframework.cloud.stream.app:time-source-kafka:1.0.0.BUILD-SNAPSHOT"));
		assertThat(appRegistry.find("filter", ApplicationType.processor).getUri().toString(),
				is("maven://org" + ".springframework.cloud.stream.app:filter-processor-kafka:1.0.0.BUILD-SNAPSHOT"));
		assertThat(appRegistry.find("log", ApplicationType.sink).getUri().toString(),
				is("maven://org.springframework" + ".cloud.stream.app:log-sink-kafka:1.0.0.BUILD-SNAPSHOT"));
		assertThat(appRegistry.find("timestamp", ApplicationType.task).getUri().toString(),
				is("maven://org" + ".springframework.cloud.task.app:timestamp-overwrite-task:1.0.0.BUILD-SNAPSHOT"));
	}

	@Test
	public void testRegisterAllWithBadApplication() throws Exception {
		mockMvc.perform(post("/apps").param("apps", "sink-foo=file:///bar").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().is5xxServerError());
	}

	@Test
	public void testListApplications() throws Exception {
		mockMvc.perform(get("/apps").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("content", hasSize(4)));
	}

	@Test
	public void testFindNonExistentApp() throws Exception {
		mockMvc.perform(get("/apps/source/foo").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().is4xxClientError()).andReturn().getResponse().getContentAsString()
				.contains("NoSuchAppRegistrationException");
	}

	@Test
	public void testRegisterAndListApplications() throws Exception {
		mockMvc.perform(get("/apps").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("content", hasSize(4)));
		mockMvc.perform(post("/apps/processor/blubba").param("uri", "file:///foo").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isCreated());
		mockMvc.perform(get("/apps").accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("content", hasSize(5)));
	}

	@Test
	public void testListSingleApplication() throws Exception {
		mockMvc.perform(get("/apps/source/time").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("name", is("time")))
				.andExpect(jsonPath("type", is("source")));
	}

	@Test
	public void testUnregisterApplication() throws Exception {
		mockMvc.perform(post("/apps/processor/blubba").param("uri", "file:///foo").accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isCreated());
		mockMvc.perform(delete("/apps/processor/blubba").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	public void testUnregisterApplicationNotFound() throws Exception {
		mockMvc.perform(delete("/apps/processor/blubba").accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isNotFound());
	}
}
