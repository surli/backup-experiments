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

import org.springframework.data.redis.connection.RedisClientConfigurationSupport.DefaultRedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.RedisClientConfigurationSupport.RedisClientConfigurationBuilder;

/**
 * Generic Redis client configuration. This configuration can be used with Redis Standalone, Redis Sentinel, and Redis
 * Cluster while only one configuration may be active.
 * <p>
 * {@link RedisClientConfiguration} can be built with {@link RedisClientConfigurationBuilder}, for example:
 * 
 * <pre class="code">
 * RedisClientConfiguration.builder().standalone(new RedisStandaloneConfiguration("localhost")).build();
 * </pre>
 * <p>
 * A {@link RedisConnectionFactory} may use a specific {@link RedisClientConfiguration} that provides client-specific
 * configuration options.
 * 
 * @author Mark Paluch
 * @since 2.0
 */
public interface RedisClientConfiguration {

	/**
	 * Returns the optional {@link RedisStandaloneConfiguration}.
	 * 
	 * @return the optional {@link RedisStandaloneConfiguration}.
	 */
	default Optional<RedisStandaloneConfiguration> getStandaloneConfiguration() {
		return Optional.empty();
	}

	/**
	 * Returns the optional {@link RedisSentinelConfiguration}.
	 *
	 * @return the optional {@link RedisSentinelConfiguration}.
	 */
	default Optional<RedisSentinelConfiguration> getSentinelConfiguration() {
		return Optional.empty();
	}

	/**
	 * Returns the optional {@link RedisClusterConfiguration}.
	 *
	 * @return the optional {@link RedisClusterConfiguration}.
	 */
	default Optional<RedisClusterConfiguration> getClusterConfiguration() {
		return Optional.empty();
	}

	/**
	 * Returns the default timeout in {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
	 * 
	 * @return the default timeout in {@link java.util.concurrent.TimeUnit#MILLISECONDS}.
	 */
	int getTimeout();

	/**
	 * Returns the optional {@code password}. Non-empty passwords are uses to authenticate with Redis.
	 * 
	 * @return the optional {@code password}.
	 */
	Optional<String> getPassword();

	/**
	 * @return {@literal true} whether to use SSL.
	 */
	boolean useSsl();

	/**
	 * Returns the optional {@code dbIndex}. The database index is only applicable for Redis Standalone/Redis Sentinel use
	 * but not for Redis Cluster.
	 *
	 * @return the optional {@code dbIndex}.
	 */
	default Optional<Integer> getDbIndex() {
		return Optional.empty();
	}

	/**
	 * Returns the optional {@code clientName}.
	 *
	 * @return the optional {@code clientName}.
	 */
	Optional<String> getClientName();

	/**
	 * Entry point to create a new {@link RedisClientConfigurationBuilder}.
	 * 
	 * @return a new {@link RedisClientConfigurationBuilder}.
	 */
	static RedisClientConfigurationBuilder builder() {
		return DefaultRedisClientConfigurationBuilder.INSTANCE;
	}

}
