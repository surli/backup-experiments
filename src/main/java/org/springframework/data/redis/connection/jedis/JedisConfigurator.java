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

import org.springframework.data.redis.connection.RedisClientConfigurator;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

/**
 * @author Mark Paluch
 */
public interface JedisConfigurator<T extends JedisConfigurator<T>> extends RedisClientConfigurator<T> {

	T poolConfig(JedisPoolConfig jedisPoolConfig);

	interface JedisSentinelClientConfigurator extends JedisConfigurator<JedisSentinelClientConfigurator>,
			RedisSentinelClientConfigurator<JedisSentinelClientConfigurator>,
			RedisClientNameConfigurer<JedisSentinelClientConfigurator> {}

	interface JedisStandaloneClientConfigurator extends JedisConfigurator<JedisStandaloneClientConfigurator>,
			RedisStandaloneClientConfigurator<JedisStandaloneClientConfigurator>,
			RedisClientNameConfigurer<JedisStandaloneClientConfigurator> {

	}

	interface JedisConfiguratorEntryPoint {

		JedisStandaloneClientConfigurator standalone(RedisStandaloneConfiguration standaloneConfiguration);

		JedisShardInfoConfigurer standalone(JedisShardInfo host);

		JedisStandaloneClientConfigurator standalone();

		JedisStandaloneClientConfigurator standalone(String host);

		JedisStandaloneClientConfigurator standalone(String host, int port);

		JedisSentinelClientConfigurator sentinel();

		JedisRedisSentinelConfigurer sentinel(RedisSentinelConfiguration redisSentinelConfiguration);

		JedisSentinelClientConfigurator sentinel(String host);

		JedisSentinelClientConfigurator sentinel(String host, int port);

	}

	interface JedisShardInfoConfigurer extends JedisConnectionPoolConfigurer<JedisShardInfoConfigurer>,
			RedisClientNameConfigurer<JedisShardInfoConfigurer> {

	}

	interface JedisRedisSentinelConfigurer extends JedisConnectionPoolConfigurer<JedisShardInfoConfigurer>,
			RedisClientNameConfigurer<JedisShardInfoConfigurer> {

	}

	interface JedisConnectionPoolConfigurer<T extends JedisConnectionPoolConfigurer<T>> {

		T poolConfig(JedisPoolConfig jedisPoolConfig);

	}

}
