/*
 * Copyright 2011-2017 the original author or authors.
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
package org.springframework.data.mongodb.core;

import static org.assertj.core.api.Assertions.*;

import org.bson.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test for {@link MongoTemplate}.
 *
 * @author Oliver Gierke
 * @author Thomas Risberg
 * @author Mark Paluch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:template-mapping.xml")
public class MongoTemplateMappingTests {

	@Autowired @Qualifier("mongoTemplate1") MongoTemplate template1;

	@Autowired @Qualifier("mongoTemplate2") MongoTemplate template2;

	@Rule public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		template1.dropCollection("person");
	}

	@Test
	public void insertsEntityCorrectly1() {

		addAndRetrievePerson(template1);
		checkPersonPersisted(template1);
	}

	@Test
	public void insertsEntityCorrectly2() {

		addAndRetrievePerson(template2);
		checkPersonPersisted(template2);
	}

	private static void addAndRetrievePerson(MongoTemplate template) {

		Person person = new Person("Oliver");
		person.setAge(25);
		template.insert(person, "person");

		Person result = template.findById(person.getId(), Person.class, "person");
		assertThat(result.getFirstName()).isEqualTo("Oliver");
		assertThat(result.getAge()).isEqualTo(25);
	}

	private void checkPersonPersisted(MongoTemplate template) {

		template.execute("person", collection -> {

			Document document = collection.find(new Document()).first();
			assertThat(document.get("name")).isEqualTo("Oliver");
			return null;
		});
	}
}
