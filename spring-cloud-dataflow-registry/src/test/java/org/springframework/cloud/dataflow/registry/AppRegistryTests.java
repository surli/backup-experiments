/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.dataflow.registry;

import java.net.URI;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import org.springframework.cloud.dataflow.registry.support.NoSuchAppRegistrationException;
import org.springframework.cloud.deployer.resource.registry.InMemoryUriRegistry;
import org.springframework.cloud.deployer.resource.registry.UriRegistry;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.cloud.dataflow.core.ApplicationType.sink;
import static org.springframework.cloud.dataflow.core.ApplicationType.source;

/**
 * Unit tests for {@link AppRegistry}.
 *
 * @author Eric Bottard
 */
public class AppRegistryTests {

	private UriRegistry uriRegistry = new InMemoryUriRegistry();

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private AppRegistry appRegistry = new AppRegistry(uriRegistry, resourceLoader);

	@Test
	public void testNotFound() {
		AppRegistration registration = appRegistry.find("foo", source);
		assertThat(registration, Matchers.nullValue());
	}

	@Test
	public void testFound() {
		Resource resource = new ClassPathResource("/foo");
		Resource metadataMesource = new ClassPathResource("/foo-metadata");
		uriRegistry.register("source.foo", URI.create("classpath:/foo"));
		uriRegistry.register("source.foo.metadata", URI.create("classpath:/foo-metadata"));

		AppRegistration registration = appRegistry.find("foo", source);
		assertThat(registration.getName(), is("foo"));
		assertThat(registration.getType(), is(source));
		assertThat(registration.getResource(), is(resource));
		assertThat(registration.getMetadataResource(), is(metadataMesource));
	}

	@Test
	public void testFindAll() {
		uriRegistry.register("source.foo", URI.create("classpath:/foo-source"));
		uriRegistry.register("sink.foo", URI.create("classpath:/foo-sink"));
		uriRegistry.register("source.foo.metadata", URI.create("classpath:/foo-source-metadata"));
		uriRegistry.register("source.bar", URI.create("classpath:/bar-source"));
		uriRegistry.register("source.bar.metadata", URI.create("classpath:/bar-source-metadata"));

		List<AppRegistration> registrations = appRegistry.findAll();

		assertThat(registrations, containsInAnyOrder(
				allOf(hasProperty("name", is("foo")), hasProperty("uri", is(URI.create("classpath:/foo-source"))),
						hasProperty("metadataUri", is(URI.create("classpath:/foo-source-metadata"))),
						hasProperty("type", is(source))),
				allOf(hasProperty("name", is("bar")), hasProperty("uri", is(URI.create("classpath:/bar-source"))),
						hasProperty("metadataUri", is(URI.create("classpath:/bar-source-metadata"))),
						hasProperty("type", is(source))),
				allOf(hasProperty("name", is("foo")), hasProperty("uri", is(URI.create("classpath:/foo-sink"))),
						hasProperty("metadataUri", nullValue()), hasProperty("type", is(sink)))));
	}

	@Test
	public void testSave() {
		appRegistry.save("foo", source, URI.create("classpath:/foo"), URI.create("foo-metadata"));

		AppRegistration registration = appRegistry.find("foo", source);

		Resource resource = new ClassPathResource("/foo");
		Resource metadataMesource = new ClassPathResource("/foo-metadata");

		assertThat(registration.getName(), is("foo"));
		assertThat(registration.getType(), is(source));
		assertThat(registration.getResource(), is(resource));
		assertThat(registration.getMetadataResource(), is(metadataMesource));
	}

	@Test
	public void testImportAll() {
		// pre-register an app
		appRegistry.save("foo", source, URI.create("classpath:/previous-foo-source"), null);

		appRegistry.importAll(false, new ClassPathResource("AppRegistryTests-importAll.properties", getClass()));
		List<AppRegistration> registrations = appRegistry.findAll();

		assertThat(registrations,
				containsInAnyOrder(allOf(hasProperty("name", is("foo")),
						hasProperty("uri", is(URI.create("classpath:/previous-foo-source"))),
						hasProperty("metadataUri", nullValue()), hasProperty("type", is(source))),
						allOf(hasProperty("name", is("bar")),
								hasProperty("uri", is(URI.create("classpath:/bar-source"))),
								hasProperty("metadataUri", is(URI.create("classpath:/bar-source-metadata"))),
								hasProperty("type", is(source))),
						allOf(hasProperty("name", is("foo")), hasProperty("uri", is(URI.create("classpath:/foo-sink"))),
								hasProperty("metadataUri", nullValue()), hasProperty("type", is(sink)))));

		// Now import with overwrite = true
		appRegistry.importAll(true, new ClassPathResource("AppRegistryTests-importAll.properties", getClass()));
		registrations = appRegistry.findAll();

		assertThat(registrations, containsInAnyOrder(
				allOf(hasProperty("name", is("foo")), hasProperty("uri", is(URI.create("classpath:/foo-source"))),
						hasProperty("metadataUri", is(URI.create("classpath:/foo-source-metadata"))),
						hasProperty("type", is(source))),
				allOf(hasProperty("name", is("bar")), hasProperty("uri", is(URI.create("classpath:/bar-source"))),
						hasProperty("metadataUri", is(URI.create("classpath:/bar-source-metadata"))),
						hasProperty("type", is(source))),
				allOf(hasProperty("name", is("foo")), hasProperty("uri", is(URI.create("classpath:/foo-sink"))),
						hasProperty("metadataUri", nullValue()), hasProperty("type", is(sink)))));
	}

	@Test
	public void testDelete() {
		// pre-register an app
		appRegistry.save("foo", source, URI.create("classpath:/previous-foo-source"), null);
		assertThat(appRegistry.find("foo", source), notNullValue());

		appRegistry.delete("foo", source);
		assertThat(appRegistry.find("foo", source), nullValue());

		try {
			appRegistry.delete("foo", source);
			fail();
		}
		catch (NoSuchAppRegistrationException expected) {
			assertThat(expected.getMessage(), is("The 'source:foo' application could not be found."));
		}
	}
}
