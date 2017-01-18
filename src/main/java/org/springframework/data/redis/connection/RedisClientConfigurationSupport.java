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
package org.springframework.data.redis.connection;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.util.Assert;

/**
 * @author Mark Paluch
 */
public abstract class RedisClientConfigurationSupport implements RedisClientConfiguration {

	private final int timeout;
	private final Optional<char[]> password;
	private final boolean useSsl;
	private final Optional<String> clientName;

	protected RedisClientConfigurationSupport(long timeout, Optional<char[]> password, boolean useSsl,
			Optional<String> clientName) {

		Assert.isTrue(timeout < Integer.MAX_VALUE, "Timeout must not exceed Integer.MAX_VALUE.");

		this.timeout = (int) timeout;
		this.password = password;
		this.useSsl = useSsl;
		this.clientName = clientName;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public Optional<String> getPassword() {
		return password.map(String::new);
	}

	@Override
	public boolean useSsl() {
		return useSsl;
	}

	@Override
	public Optional<String> getClientName() {
		return clientName;
	}

	public interface RedisClientConfigurationBuilder {

		RedisStandaloneClientConfigurationBuilder standalone(RedisStandaloneConfiguration standaloneConfiguration);

		RedisSentinelClientConfigurationBuilder sentinel(RedisSentinelConfiguration sentinelConfiguration);
	}

	enum DefaultRedisClientConfigurationBuilder implements RedisClientConfigurationBuilder {

		INSTANCE;

		public RedisStandaloneClientConfigurationBuilder standalone(RedisStandaloneConfiguration standaloneConfiguration) {
			return new RedisStandaloneClientConfigurationBuilder(standaloneConfiguration);
		}

		@Override
		public RedisSentinelClientConfigurationBuilder sentinel(RedisSentinelConfiguration sentinelConfiguration) {
			return new RedisSentinelClientConfigurationBuilder(sentinelConfiguration);
		}

	}

	public static abstract class AbstractRedisClientConfigurationBuilder<T extends AbstractRedisClientConfigurationBuilder<T>> {

		protected long timeout = TimeUnit.MILLISECONDS.convert(50, TimeUnit.SECONDS);
		protected Optional<char[]> password = Optional.empty();
		protected boolean useSsl = false;
		protected Optional<String> clientName = Optional.empty();

		AbstractRedisClientConfigurationBuilder timeout(long timeout, TimeUnit timeUnit) {

			Assert.isTrue(timeout >= 0, "Timeout must be greater or equal to zero!");
			Assert.notNull(timeUnit, "TimeUnit must not be null!");

			this.timeout = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);

			return this;
		}

		public T password(String password) {

			Assert.notNull(password, "Password must not be null!");

			this.password = Optional.of(password.toCharArray());

			return (T) this;
		}

		public T withoutPassword() {

			this.password = Optional.empty();

			return (T) this;
		}

		public T withSsl() {

			this.useSsl = true;

			return (T) this;
		}

		public T clientName(String clientName) {

			Assert.notNull(clientName, "Client name must not be null!");

			this.clientName = Optional.of(clientName);

			return (T) this;
		}

		public T withoutClientName() {

			this.clientName = Optional.empty();

			return (T) this;
		}

		public abstract RedisClientConfiguration build();
	}

	public static class RedisStandaloneClientConfigurationBuilder
			extends AbstractRedisClientConfigurationBuilder<RedisStandaloneClientConfigurationBuilder> {

		private final RedisStandaloneConfiguration standaloneConfiguration;
		private Optional<Integer> dbIndex = Optional.empty();

		/**
		 * Creates a new {@link RedisStandaloneClientConfigurationBuilder} given {@link RedisStandaloneClientConfiguration}.
		 * 
		 * @param standaloneConfiguration must not be {@literal null}.
		 */
		protected RedisStandaloneClientConfigurationBuilder(RedisStandaloneConfiguration standaloneConfiguration) {

			Assert.notNull(standaloneConfiguration, "RedisStandaloneConfiguration must not be null!");

			this.standaloneConfiguration = standaloneConfiguration;
		}

		public RedisStandaloneClientConfigurationBuilder dbIndex(int dbIndex) {

			Assert.isTrue(timeout >= 0, "DbIndex must be greater or equal to zero!");

			this.dbIndex = Optional.of(dbIndex);
			return this;
		}

		@Override
		public RedisClientConfiguration build() {
			return new RedisStandaloneClientConfiguration(timeout, password, useSsl, clientName, standaloneConfiguration,
					dbIndex);
		}

		static class RedisStandaloneClientConfiguration extends RedisClientConfigurationSupport {

			private final Optional<RedisStandaloneConfiguration> standaloneConfiguration;
			private final Optional<Integer> dbIndex;

			RedisStandaloneClientConfiguration(long timeout, Optional<char[]> password, boolean useSsl,
					Optional<String> clientName, RedisStandaloneConfiguration standaloneConfiguration,
					Optional<Integer> dbIndex) {

				super(timeout, password, useSsl, clientName);
				this.standaloneConfiguration = Optional.of(standaloneConfiguration);
				this.dbIndex = dbIndex;
			}

			@Override
			public Optional<RedisStandaloneConfiguration> getStandaloneConfiguration() {
				return standaloneConfiguration;
			}

			@Override
			public Optional<Integer> getDbIndex() {
				return dbIndex;
			}
		}
	}

	public static class RedisSentinelClientConfigurationBuilder
			extends AbstractRedisClientConfigurationBuilder<RedisSentinelClientConfigurationBuilder> {

		private final RedisSentinelConfiguration sentinelConfiguration;
		private Optional<Integer> dbIndex = Optional.empty();

		protected RedisSentinelClientConfigurationBuilder(RedisSentinelConfiguration sentinelConfiguration) {

			Assert.notNull(sentinelConfiguration, "RedisSentinelConfiguration must not be null!");

			this.sentinelConfiguration = sentinelConfiguration;
		}

		public RedisSentinelClientConfigurationBuilder dbIndex(int dbIndex) {

			Assert.isTrue(timeout >= 0, "DbIndex must be greater or equal to zero!");

			this.dbIndex = Optional.of(dbIndex);
			return this;
		}

		@Override
		public RedisClientConfiguration build() {

			return new RedisSentinelClientConfiguration(timeout, password, useSsl, clientName, sentinelConfiguration,
					dbIndex);
		}

		static class RedisSentinelClientConfiguration extends RedisClientConfigurationSupport {

			private final Optional<RedisSentinelConfiguration> sentinelConfiguration;
			private final Optional<Integer> dbIndex;

			RedisSentinelClientConfiguration(long timeout, Optional<char[]> password, boolean useSsl,
					Optional<String> clientName, RedisSentinelConfiguration sentinelConfiguration, Optional<Integer> dbIndex) {

				super(timeout, password, useSsl, clientName);

				this.sentinelConfiguration = Optional.of(sentinelConfiguration);
				this.dbIndex = dbIndex;
			}

			@Override
			public Optional<RedisSentinelConfiguration> getSentinelConfiguration() {
				return sentinelConfiguration;
			}

			@Override
			public Optional<Integer> getDbIndex() {
				return dbIndex;
			}
		}
	}

	public static class RedisClusterClientConfigurationBuilder
			extends AbstractRedisClientConfigurationBuilder<RedisClusterClientConfigurationBuilder> {

		private final RedisClusterConfiguration clusterConfiguration;

		RedisClusterClientConfigurationBuilder(RedisClusterConfiguration clusterConfiguration) {
			this.clusterConfiguration = clusterConfiguration;
		}

		@Override
		public RedisClientConfiguration build() {
			return new RedisClusterClientConfiguration(timeout, password, useSsl, clientName, clusterConfiguration);
		}

		static class RedisClusterClientConfiguration extends RedisClientConfigurationSupport {

			private final Optional<RedisClusterConfiguration> clusterConfiguration;

			RedisClusterClientConfiguration(long timeout, Optional<char[]> password, boolean useSsl,
					Optional<String> clientName, RedisClusterConfiguration clusterConfiguration) {
				super(timeout, password, useSsl, clientName);
				this.clusterConfiguration = Optional.of(clusterConfiguration);
			}

			@Override
			public Optional<RedisClusterConfiguration> getClusterConfiguration() {
				return clusterConfiguration;
			}
		}
	}
}
