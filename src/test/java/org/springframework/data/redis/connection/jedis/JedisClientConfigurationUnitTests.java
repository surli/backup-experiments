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

import static org.assertj.core.api.Assertions.*;

import redis.clients.jedis.JedisShardInfo;

import java.net.URI;
import java.util.Optional;

import org.junit.Test;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;

/**
 * Unit tests for {@link JedisClientConfiguration}.
 * 
 * @author Mark Paluch
 */
public class JedisClientConfigurationUnitTests {

	@Test // DATAREDIS-574
	public void shouldBuildRedisStandaloneConfiguration() throws Exception {

		JedisClientConfiguration configuration = JedisClientConfiguration.builder() //
				.standalone(new JedisShardInfo(URI.create("rediss://:Walter@host:1234/5"))) //
				.clientName("Walter") //
				.password("White") //
				.build();

		assertThat(configuration.getPoolConfig()).isEmpty();
		assertThat(configuration.getShardInfo()).isNotEmpty();
		assertThat(configuration.getClientName()).isNotEmpty().isEqualTo(Optional.of("Walter"));
		assertThat(configuration.getDbIndex()).isNotEmpty().isEqualTo(Optional.of(5));
		assertThat(configuration.useSsl()).isTrue();

		assertThat(configuration.getPassword()).isNotEmpty().isEqualTo(Optional.of("White"));

		assertThat(configuration.getStandaloneConfiguration()).isNotEmpty();
		assertThat(configuration.getSentinelConfiguration()).isEmpty();
		assertThat(configuration.getClusterConfiguration()).isEmpty();

		configuration.getStandaloneConfiguration().ifPresent(standaloneConfiguration -> {

			assertThat(standaloneConfiguration.getHostName()).isEqualTo("host");
			assertThat(standaloneConfiguration.getPort()).isEqualTo(1234);

		});
	}

	@Test // DATAREDIS-574
	public void shouldBuildRedisSentinelConfiguration() throws Exception {

		JedisClientConfiguration configuration = JedisClientConfiguration.builder() //
				.sentinel(new RedisSentinelConfiguration()) //
				.clientName("Walter") //
				.password("White") //
				.build();

		assertThat(configuration.getPoolConfig()).isEmpty();
		assertThat(configuration.getShardInfo()).isEmpty();
		assertThat(configuration.getClientName()).isNotEmpty().isEqualTo(Optional.of("Walter"));
		assertThat(configuration.getDbIndex()).isEmpty();
		assertThat(configuration.useSsl()).isFalse();

		assertThat(configuration.getPassword()).isNotEmpty().isEqualTo(Optional.of("White"));

		assertThat(configuration.getStandaloneConfiguration()).isEmpty();
		assertThat(configuration.getSentinelConfiguration()).isNotEmpty();
		assertThat(configuration.getClusterConfiguration()).isEmpty();
	}
}
