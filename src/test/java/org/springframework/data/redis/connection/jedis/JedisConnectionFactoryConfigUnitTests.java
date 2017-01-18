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
package org.springframework.data.redis.connection.jedis;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;

import java.net.URI;

import org.junit.Test;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;

/**
 * Test for {@link JedisConnectionFactory} using a configurer callback.
 * 
 * @author Mark Paluch
 */
public class JedisConnectionFactoryConfigUnitTests {

	@Test // DATAREDIS-574
	public void standalone() throws Exception {

		new JedisConnectionFactory(config -> {
			config.standalone().withSsl().dbIndex(10);
		});

		new JedisConnectionFactory(config -> {
			config.standalone(new JedisShardInfo(URI.create("rediss://:password@host:1234/0"))) //
					.poolConfig(new JedisPoolConfig()) //
					.clientName("client-name");
		});
	}

	@Test // DATAREDIS-574
	public void sentinel() throws Exception {

		new JedisConnectionFactory(config -> {

			config.sentinel(new RedisSentinelConfiguration()) //
					.poolConfig(new JedisPoolConfig()) //
					.clientName("client-name");
		});

	}
}
