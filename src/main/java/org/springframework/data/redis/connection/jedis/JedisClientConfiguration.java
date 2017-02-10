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
import redis.clients.jedis.Protocol;

import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.data.redis.connection.Timeout;
import org.springframework.util.Assert;

/**
 * Redis client configuration for jedis. This configuration provides optional configuration elements such as
 * {@link SSLSocketFactory} and {@link JedisPoolConfig} specific to jedis client features.
 * <p>
 * Providing optional elements allows a more specific configuration of the client:
 * <ul>
 * <li>Whether to use SSL</li>
 * <li>Optional {@link SSLSocketFactory}</li>
 * <li>Optional {@link SSLParameters}</li>
 * <li>Optional {@link HostnameVerifier}</li>
 * <li>Whether to use connection-pooling</li>
 * <li>Optional {@link JedisPoolConfig}</li>
 * <li>Connect {@link Timeout}</li>
 * <li>Read {@link Timeout}</li>
 * </ul>
 *
 * @author Mark Paluch
 * @since 2.0
 * @see redis.clients.jedis.Jedis
 * @see org.springframework.data.redis.connection.RedisStandaloneConfiguration
 * @see org.springframework.data.redis.connection.RedisSentinelConfiguration
 * @see org.springframework.data.redis.connection.RedisClusterConfiguration
 */
public interface JedisClientConfiguration {

	/**
	 * @return {@literal true} to use SSL.
	 */
	boolean useSsl();

	/**
	 * @return the optional {@link SSLSocketFactory}.
	 */
	Optional<SSLSocketFactory> getSslSocketFactory();

	/**
	 * @return the optional {@link SSLParameters}.
	 */
	Optional<SSLParameters> getSslParameters();

	/**
	 * @return the optional {@link HostnameVerifier}.
	 */
	Optional<HostnameVerifier> getHostnameVerifier();

	/**
	 * @return {@literal true} to use connection-pooling.
	 */
	boolean usePooling();

	/**
	 * @return the optional {@link JedisPoolConfig}.
	 */
	Optional<JedisPoolConfig> getPoolConfig();

	/**
	 * @return the optional client name to be set with {@code CLIENT SETNAME}.
	 */
	Optional<String> getClientName();

	/**
	 * @return the connection timeout.
	 * @see java.net.Socket#connect(SocketAddress, int)
	 */
	Timeout getConnectTimeout();

	/**
	 * @return the read timeout.
	 * @see java.net.Socket#setSoTimeout(int)
	 */
	Timeout getReadTimeout();

	/**
	 * Creates a new {@link JedisClientConfigurationBuilder} to build {@link JedisClientConfiguration} to be used with the
	 * jedis client.
	 * 
	 * @return a new {@link JedisClientConfigurationBuilder} to build {@link JedisClientConfiguration}.
	 */
	static JedisClientConfigurationBuilder builder() {
		return new DefaultJedisClientConfigurationBuilder();
	}

	/**
	 * Builder for {@link JedisClientConfiguration}.
	 */
	interface JedisClientConfigurationBuilder {

		/**
		 * Enable SSL connections.
		 * 
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder useSsl();

		/**
		 * @param sslEnabled {@literal true} to use SSL connections, {@literal false} otherwise.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder useSsl(boolean sslEnabled);

		/**
		 * @param sslSocketFactory must not be {@literal null}.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory);

		/**
		 * @param sslParameters must not be {@literal null}.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder sslParameters(SSLParameters sslParameters);

		/**
		 * @param hostnameVerifier must not be {@literal null}.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder hostnameVerifier(HostnameVerifier hostnameVerifier);

		/**
		 * Enable connection-pooling.
		 *
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder usePooling();

		/**
		 * @param poolingEnabled {@literal true} to use connection-pooling, {@literal false} to create and close
		 *          {@link redis.clients.jedis.Jedis} connections instead of reusing those.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder usePooling(boolean poolingEnabled);

		/**
		 * @param poolConfig must not be {@literal null}.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder poolConfig(JedisPoolConfig poolConfig);

		/**
		 * Configure a {@code clientName} to be set with {@code CLIENT SETNAME}.
		 * 
		 * @param clientName must not be {@literal null}.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder clientName(String clientName);

		/**
		 * Configure a read timeout.
		 *
		 * @param readTimeout must not be {@literal null}.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder readTimeout(Timeout readTimeout);

		/**
		 * Configure a connection timeout.
		 *
		 * @param connectTimeout must not be {@literal null}.
		 * @return {@literal this} builder.
		 */
		JedisClientConfigurationBuilder connectTimeout(Timeout connectTimeout);

		/**
		 * Build the {@link JedisClientConfiguration} with the configuration applied from this builder.
		 *
		 * @return a new {@link JedisClientConfiguration} object.
		 */
		JedisClientConfiguration build();
	}

	/**
	 * Entry point for {@link JedisClientConfigurationBuilder}.
	 */
	class DefaultJedisClientConfigurationBuilder implements JedisClientConfigurationBuilder {

		private boolean useSsl;
		private SSLSocketFactory sslSocketFactory;
		private SSLParameters sslParameters;
		private HostnameVerifier hostnameVerifier;
		private boolean usePooling;
		private JedisPoolConfig poolConfig = new JedisPoolConfig();
		private String clientName;
		private Timeout readTimeout = Timeout.create(Protocol.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
		private Timeout connectTimeout = Timeout.create(Protocol.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

		private DefaultJedisClientConfigurationBuilder() {}

		@Override
		public JedisClientConfigurationBuilder useSsl() {
			return useSsl(true);
		}

		@Override
		public JedisClientConfigurationBuilder useSsl(boolean sslEnabled) {

			this.useSsl = sslEnabled;
			return this;
		}

		@Override
		public JedisClientConfigurationBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory) {

			Assert.notNull(sslSocketFactory, "SSLSocketFactory must not be null!");

			this.sslSocketFactory = sslSocketFactory;
			return this;
		}

		@Override
		public JedisClientConfigurationBuilder sslParameters(SSLParameters sslParameters) {

			Assert.notNull(sslParameters, "SSLParameters must not be null!");

			this.sslParameters = sslParameters;
			return this;
		}

		@Override
		public JedisClientConfigurationBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {

			Assert.notNull(hostnameVerifier, "HostnameVerifier must not be null!");

			this.hostnameVerifier = hostnameVerifier;
			return this;
		}

		@Override
		public JedisClientConfigurationBuilder usePooling() {
			return usePooling(true);
		}

		@Override
		public JedisClientConfigurationBuilder usePooling(boolean poolingEnabled) {

			this.usePooling = poolingEnabled;
			return this;
		}

		@Override
		public JedisClientConfigurationBuilder poolConfig(JedisPoolConfig poolConfig) {

			Assert.notNull(poolConfig, "JedisPoolConfig must not be null!");

			this.poolConfig = poolConfig;
			return this;
		}

		@Override
		public JedisClientConfigurationBuilder clientName(String clientName) {

			Assert.hasText(clientName, "Client name must not be null or empty!");

			this.clientName = clientName;
			return this;
		}

		@Override
		public JedisClientConfigurationBuilder readTimeout(Timeout readTimeout) {

			Assert.notNull(readTimeout, "Timeout must not be null!");

			this.readTimeout = readTimeout;
			return this;
		}

		@Override
		public JedisClientConfigurationBuilder connectTimeout(Timeout connectTimeout) {

			Assert.notNull(connectTimeout, "Timeout must not be null!");

			this.connectTimeout = connectTimeout;
			return this;
		}

		@Override
		public JedisClientConfiguration build() {

			return new DefaultJedisClientConfiguration(useSsl, sslSocketFactory, sslParameters, hostnameVerifier, usePooling,
					poolConfig, clientName, readTimeout, connectTimeout);
		}
	}

}
