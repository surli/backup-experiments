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

import java.util.Optional;

import org.springframework.data.redis.connection.RedisClientConfiguration;
import org.springframework.data.redis.connection.RedisClientConfigurationSupport.RedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.RedisClientConfigurationSupport.RedisSentinelClientConfigurationBuilder;
import org.springframework.data.redis.connection.RedisClientConfigurationSupport.RedisStandaloneClientConfigurationBuilder;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

/**
 * {@link RedisClientConfiguration} for jedis. This configuration provides optional configuration elements such as
 * {@link JedisShardInfo} and {@link JedisPoolConfig}. Providing optional elements allows a more specific configuration
 * of the client.
 *
 * @author Mark Paluch
 * @since 2.0
 * @see RedisClientConfiguration
 * @see redis.clients.jedis.Jedis
 */
public interface JedisClientConfiguration extends RedisClientConfiguration {

	/**
	 * @return the optional {@link JedisShardInfo}.
	 */
	default Optional<JedisShardInfo> getShardInfo() {
		return Optional.empty();
	}

	/**
	 * @return the optional {@link JedisPoolConfig}.
	 */
	default Optional<JedisPoolConfig> getPoolConfig() {
		return Optional.empty();
	}

	/**
	 * Creates a new {@link JedisClientConfigurationBuilder} to build {@link JedisClientConfiguration} to be used with the
	 * jedis client.
	 * 
	 * @return a new {@link JedisClientConfigurationBuilder} to build {@link JedisClientConfiguration}.
	 * @see RedisClientConfiguration#builder()
	 */
	static JedisClientConfigurationBuilder builder() {
		return DefaultJedisClientConfigurationBuilder.INSTANCE;
	}

	/**
	 * Jedis-specific {@link RedisClientConfigurationBuilder}.
	 */
	interface JedisClientConfigurationBuilder extends RedisClientConfigurationBuilder {

		/**
		 * Prepare a standalone builder initialized from {@link JedisShardInfo}.
		 * 
		 * @param jedisShardInfo must not be {@literal null}.
		 * @return the {@link JedisStandaloneClientConfigurationBuilder}.
		 */
		JedisStandaloneClientConfigurationBuilder standalone(JedisShardInfo jedisShardInfo);

		JedisStandaloneClientConfigurationBuilder standalone(RedisStandaloneConfiguration standaloneConfiguration);

		JedisSentinelClientConfigurationBuilder sentinel(RedisSentinelConfiguration sentinelConfiguration);
	}

	/**
	 * Entry point for {@link JedisClientConfigurationBuilder}.
	 */
	enum DefaultJedisClientConfigurationBuilder implements JedisClientConfigurationBuilder {

		INSTANCE;

		public JedisStandaloneClientConfigurationBuilder standalone(JedisShardInfo jedisShardInfo) {
			return new JedisStandaloneClientConfigurationBuilder(jedisShardInfo);
		}

		@Override
		public JedisStandaloneClientConfigurationBuilder standalone(RedisStandaloneConfiguration standaloneConfiguration) {
			return new JedisStandaloneClientConfigurationBuilder(standaloneConfiguration);
		}

		@Override
		public JedisSentinelClientConfigurationBuilder sentinel(RedisSentinelConfiguration sentinelConfiguration) {
			return new JedisSentinelClientConfigurationBuilder(sentinelConfiguration);
		}

	}

	/**
	 * Builder for Redis Standalone use with jedis.
	 */
	class JedisStandaloneClientConfigurationBuilder extends RedisStandaloneClientConfigurationBuilder {

		private Optional<JedisShardInfo> jedisShardInfo = Optional.empty();
		private Optional<JedisPoolConfig> jedisPoolConfig = Optional.empty();

		/**
		 * @param jedisShardInfo must not be {@literal null}.
		 */
		protected JedisStandaloneClientConfigurationBuilder(JedisShardInfo jedisShardInfo) {

			super(new RedisStandaloneConfiguration(jedisShardInfo.getHost(), jedisShardInfo.getPort()));

			this.jedisShardInfo = Optional.of(jedisShardInfo);
			this.useSsl = jedisShardInfo.getSsl();
			this.dbIndex(jedisShardInfo.getDb());
		}

		protected JedisStandaloneClientConfigurationBuilder(RedisStandaloneConfiguration standaloneConfiguration) {
			super(standaloneConfiguration);
		}

		@Override
		public JedisStandaloneClientConfigurationBuilder password(String password) {

			super.password(password);
			return this;
		}

		@Override
		public JedisStandaloneClientConfigurationBuilder withoutPassword() {

			super.withoutPassword();
			return this;
		}

		@Override
		public JedisStandaloneClientConfigurationBuilder withSsl() {

			super.withSsl();
			return this;
		}

		@Override
		public JedisStandaloneClientConfigurationBuilder clientName(String clientName) {

			super.clientName(clientName);
			return this;
		}

		@Override
		public JedisStandaloneClientConfigurationBuilder withoutClientName() {

			super.withoutClientName();
			return this;
		}

		@Override
		public JedisStandaloneClientConfigurationBuilder dbIndex(int dbIndex) {

			super.dbIndex(dbIndex);
			return this;
		}

		@Override
		public JedisClientConfiguration build() {
			return new JedisStandaloneClientConfiguration(super.build(), jedisShardInfo, jedisPoolConfig);
		}

		static class JedisStandaloneClientConfiguration extends JedisClientConfigurationSupport
				implements JedisClientConfiguration {

			private final Optional<JedisShardInfo> jedisShardInfo;
			private final Optional<JedisPoolConfig> jedisPoolConfig;

			JedisStandaloneClientConfiguration(RedisClientConfiguration redisClientConfiguration,
					Optional<JedisShardInfo> jedisShardInfo, Optional<JedisPoolConfig> jedisPoolConfig) {

				super(redisClientConfiguration);

				this.jedisShardInfo = jedisShardInfo;
				this.jedisPoolConfig = jedisPoolConfig;
			}

			@Override
			public Optional<JedisShardInfo> getShardInfo() {
				return jedisShardInfo;
			}

			@Override
			public Optional<JedisPoolConfig> getPoolConfig() {
				return jedisPoolConfig;
			}
		}
	}

	class JedisSentinelClientConfigurationBuilder extends RedisSentinelClientConfigurationBuilder {

		private Optional<JedisPoolConfig> jedisPoolConfig = Optional.empty();

		protected JedisSentinelClientConfigurationBuilder(RedisSentinelConfiguration standaloneConfiguration) {
			super(standaloneConfiguration);
		}

		@Override
		public JedisSentinelClientConfigurationBuilder password(String password) {

			super.password(password);
			return this;
		}

		@Override
		public JedisSentinelClientConfigurationBuilder withoutPassword() {

			super.withoutPassword();
			return this;
		}

		@Override
		public JedisSentinelClientConfigurationBuilder withSsl() {

			super.withSsl();
			return this;
		}

		@Override
		public JedisSentinelClientConfigurationBuilder clientName(String clientName) {

			super.clientName(clientName);
			return this;
		}

		@Override
		public JedisSentinelClientConfigurationBuilder withoutClientName() {

			super.withoutClientName();
			return this;
		}

		@Override
		public JedisClientConfiguration build() {
			return new JedisSentinelClientConfiguration(super.build(), jedisPoolConfig);
		}

		static class JedisSentinelClientConfiguration extends JedisClientConfigurationSupport
				implements JedisClientConfiguration {

			private final Optional<JedisPoolConfig> jedisPoolConfig;

			JedisSentinelClientConfiguration(RedisClientConfiguration redisClientConfiguration,
					Optional<JedisPoolConfig> jedisPoolConfig) {

				super(redisClientConfiguration);

				this.jedisPoolConfig = jedisPoolConfig;
			}

			@Override
			public Optional<JedisPoolConfig> getPoolConfig() {
				return jedisPoolConfig;
			}
		}
	}

	static abstract class JedisClientConfigurationSupport implements JedisClientConfiguration {
		private final RedisClientConfiguration redisClientConfiguration;

		public JedisClientConfigurationSupport(RedisClientConfiguration redisClientConfiguration) {
			this.redisClientConfiguration = redisClientConfiguration;
		}

		@Override
		public Optional<RedisStandaloneConfiguration> getStandaloneConfiguration() {
			return redisClientConfiguration.getStandaloneConfiguration();
		}

		@Override
		public Optional<RedisSentinelConfiguration> getSentinelConfiguration() {
			return redisClientConfiguration.getSentinelConfiguration();
		}

		@Override
		public Optional<RedisClusterConfiguration> getClusterConfiguration() {
			return redisClientConfiguration.getClusterConfiguration();
		}

		@Override
		public int getTimeout() {
			return redisClientConfiguration.getTimeout();
		}

		@Override
		public Optional<String> getPassword() {
			return redisClientConfiguration.getPassword();
		}

		@Override
		public boolean useSsl() {
			return redisClientConfiguration.useSsl();
		}

		@Override
		public Optional<Integer> getDbIndex() {
			return redisClientConfiguration.getDbIndex();
		}

		@Override
		public Optional<String> getClientName() {
			return redisClientConfiguration.getClientName();
		}
	}

}
