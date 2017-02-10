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
package org.springframework.data.redis.connection.jedis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.redis.ExceptionTranslationStrategy;
import org.springframework.data.redis.PassThroughExceptionTranslationStrategy;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.*;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Connection factory creating <a href="http://github.com/xetorthio/jedis">Jedis</a> based connections.
 * 
 * @author Costin Leau
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 * @author Fu Jian
 */
public class JedisConnectionFactory implements InitializingBean, DisposableBean, RedisConnectionFactory {

	private final static Log log = LogFactory.getLog(JedisConnectionFactory.class);
	private static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION = new PassThroughExceptionTranslationStrategy(
			JedisConverters.exceptionConverter());

	private static final Method SET_TIMEOUT_METHOD;
	private static final Method GET_TIMEOUT_METHOD;

	static {

		// We need to configure Jedis socket timeout via reflection since the method-name was changed between releases.
		Method setTimeoutMethodCandidate = ReflectionUtils.findMethod(JedisShardInfo.class, "setTimeout", int.class);
		if (setTimeoutMethodCandidate == null) {
			// Jedis V 2.7.x changed the setTimeout method to setSoTimeout
			setTimeoutMethodCandidate = ReflectionUtils.findMethod(JedisShardInfo.class, "setSoTimeout", int.class);
		}
		SET_TIMEOUT_METHOD = setTimeoutMethodCandidate;

		Method getTimeoutMethodCandidate = ReflectionUtils.findMethod(JedisShardInfo.class, "getTimeout");
		if (getTimeoutMethodCandidate == null) {
			getTimeoutMethodCandidate = ReflectionUtils.findMethod(JedisShardInfo.class, "getSoTimeout");
		}

		GET_TIMEOUT_METHOD = getTimeoutMethodCandidate;
	}

	private final JedisClientConfiguration clientConfiguration;
	private JedisShardInfo shardInfo;
	private String password;
	private Pool<Jedis> pool;
	private boolean convertPipelineAndTxResults = true;
	private RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration("localhost",
			Protocol.DEFAULT_PORT);
	private RedisSentinelConfiguration sentinelConfig;
	private RedisClusterConfiguration clusterConfig;
	private JedisCluster cluster;
	private ClusterCommandExecutor clusterCommandExecutor;

	/**
	 * Constructs a new <code>JedisConnectionFactory</code> instance with default settings (default connection pooling, no
	 * shard information).
	 */
	public JedisConnectionFactory() {
		this(new MutableJedisClientConfiguration());
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance given {@link JedisClientConfiguration}.
	 *
	 * @param clientConfig must not be {@literal null}
	 * @since 2.0
	 */
	public JedisConnectionFactory(JedisClientConfiguration clientConfig) {

		Assert.notNull(clientConfig, "JedisClientConfiguration must not be null!");
		this.clientConfiguration = clientConfig;
	}

	/**
	 * Constructs a new <code>JedisConnectionFactory</code> instance. Will override the other connection parameters passed
	 * to the factory.
	 * 
	 * @param shardInfo shard information
	 */
	public JedisConnectionFactory(JedisShardInfo shardInfo) {
		this(MutableJedisClientConfiguration.create(shardInfo));
		this.shardInfo = shardInfo;
	}

	/**
	 * Constructs a new <code>JedisConnectionFactory</code> instance using the given pool configuration.
	 * 
	 * @param poolConfig pool configuration
	 */
	public JedisConnectionFactory(JedisPoolConfig poolConfig) {
		this((RedisSentinelConfiguration) null, poolConfig);
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link JedisPoolConfig} applied to
	 * {@link JedisSentinelPool}.
	 * 
	 * @param sentinelConfig
	 * @since 1.4
	 */
	public JedisConnectionFactory(RedisSentinelConfiguration sentinelConfig) {
		this(sentinelConfig, new MutableJedisClientConfiguration());
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link JedisPoolConfig} applied to
	 * {@link JedisSentinelPool}.
	 * 
	 * @param sentinelConfig
	 * @param poolConfig pool configuration. Defaulted to new instance if {@literal null}.
	 * @since 1.4
	 */
	public JedisConnectionFactory(RedisSentinelConfiguration sentinelConfig, JedisPoolConfig poolConfig) {

		this.sentinelConfig = sentinelConfig;
		this.clientConfiguration = MutableJedisClientConfiguration
				.create(poolConfig != null ? poolConfig : new JedisPoolConfig());
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link RedisClusterConfiguration} applied
	 * to create a {@link JedisCluster}.
	 * 
	 * @param clusterConfig
	 * @since 1.7
	 */
	public JedisConnectionFactory(RedisClusterConfiguration clusterConfig) {
		this(clusterConfig, new MutableJedisClientConfiguration());
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link RedisClusterConfiguration} applied
	 * to create a {@link JedisCluster}.
	 * 
	 * @param clusterConfig
	 * @since 1.7
	 */
	public JedisConnectionFactory(RedisClusterConfiguration clusterConfig, JedisPoolConfig poolConfig) {

		this.clusterConfig = clusterConfig;
		this.clientConfiguration = MutableJedisClientConfiguration.create(poolConfig);
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link RedisStandaloneConfiguration} and
	 * {@link JedisClientConfiguration}.
	 *
	 * @param standaloneConfig must not be {@literal null}.
	 * @param clientConfig must not be {@literal null}.
	 * @since 2.0
	 */
	public JedisConnectionFactory(RedisStandaloneConfiguration standaloneConfig, JedisClientConfiguration clientConfig) {

		this(clientConfig);

		Assert.notNull(standaloneConfig, "RedisStandaloneConfiguration must not be null!");

		this.standaloneConfig = standaloneConfig;
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link RedisSentinelConfiguration} and
	 * {@link JedisClientConfiguration}.
	 *
	 * @param sentinelConfig must not be {@literal null}.
	 * @param clientConfig must not be {@literal null}.
	 * @since 2.0
	 */
	public JedisConnectionFactory(RedisSentinelConfiguration sentinelConfig, JedisClientConfiguration clientConfig) {

		this(clientConfig);

		Assert.notNull(sentinelConfig, "RedisSentinelConfiguration must not be null!");

		this.sentinelConfig = sentinelConfig;
	}

	/**
	 * Constructs a new {@link JedisConnectionFactory} instance using the given {@link RedisClusterConfiguration} and
	 * {@link JedisClientConfiguration}.
	 *
	 * @param clusterConfig must not be {@literal null}.
	 * @param clientConfig must not be {@literal null}.
	 * @since 2.0
	 */
	public JedisConnectionFactory(RedisClusterConfiguration clusterConfig, JedisClientConfiguration clientConfig) {

		this(clientConfig);

		Assert.notNull(clusterConfig, "RedisClusterConfiguration must not be null!");

		this.clusterConfig = clusterConfig;

	}

	/**
	 * Returns a Jedis instance to be used as a Redis connection. The instance can be newly created or retrieved from a
	 * pool.
	 * 
	 * @return Jedis instance ready for wrapping into a {@link RedisConnection}.
	 */
	protected Jedis fetchJedisConnector() {
		try {

			if (getUsePool() && pool != null) {
				return pool.getResource();
			}

			Jedis jedis = new Jedis(getShardInfo());
			// force initialization (see Jedis issue #82)
			jedis.connect();

			potentiallySetClientName(jedis);
			return jedis;
		} catch (Exception ex) {
			throw new RedisConnectionFailureException("Cannot get Jedis connection", ex);
		}
	}

	/**
	 * Post process a newly retrieved connection. Useful for decorating or executing initialization commands on a new
	 * connection. This implementation simply returns the connection.
	 * 
	 * @param connection
	 * @return processed connection
	 */
	protected JedisConnection postProcessConnection(JedisConnection connection) {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {

		if (shardInfo == null) {

			shardInfo = new JedisShardInfo(standaloneConfig.getHostName(), standaloneConfig.getPort(), isUseSsl(), //
					clientConfiguration.getSslSocketFactory().orElse(null), //
					clientConfiguration.getSslParameters().orElse(null), //
					clientConfiguration.getHostnameVerifier().orElse(null));

			if (StringUtils.hasLength(password)) {
				shardInfo.setPassword(password);
			}

			int readTimeout = getReadTimeout();

			if (readTimeout > 0) {
				setTimeoutOn(shardInfo, readTimeout);
			}

			setShardInfo(shardInfo);
		}

		if (getUsePool() && clusterConfig == null) {
			this.pool = createPool();
		}

		if (isRedisClusterAware()) {
			this.cluster = createCluster();
		}
	}

	private Pool<Jedis> createPool() {

		if (isRedisSentinelAware()) {
			return createRedisSentinelPool(this.sentinelConfig);
		}
		return createRedisPool();
	}

	/**
	 * Creates {@link JedisSentinelPool}.
	 * 
	 * @param config
	 * @return
	 * @since 1.4
	 */
	protected Pool<Jedis> createRedisSentinelPool(RedisSentinelConfiguration config) {
		return new JedisSentinelPool(config.getMaster().getName(), convertToJedisSentinelSet(config.getSentinels()),
				getPoolConfig() != null ? getPoolConfig() : new JedisPoolConfig(), getTimeoutFrom(getShardInfo()),
				getShardInfo().getPassword(), Protocol.DEFAULT_DATABASE, getClientName());
	}

	/**
	 * Creates {@link JedisPool}.
	 * 
	 * @return
	 * @since 1.4
	 */
	protected Pool<Jedis> createRedisPool() {

		return new JedisPool(getPoolConfig(), getShardInfo().getHost(), getShardInfo().getPort(),
				getTimeoutFrom(getShardInfo()), getShardInfo().getPassword(), Protocol.DEFAULT_DATABASE, getClientName(),
				isUseSsl());
	}

	private JedisCluster createCluster() {

		JedisCluster cluster = createCluster(this.clusterConfig, getPoolConfig());
		this.clusterCommandExecutor = new ClusterCommandExecutor(
				new JedisClusterConnection.JedisClusterTopologyProvider(cluster),
				new JedisClusterConnection.JedisClusterNodeResourceProvider(cluster), EXCEPTION_TRANSLATION);
		return cluster;
	}

	/**
	 * Creates {@link JedisCluster} for given {@link RedisClusterConfiguration} and {@link GenericObjectPoolConfig}.
	 * 
	 * @param clusterConfig must not be {@literal null}.
	 * @param poolConfig can be {@literal null}.
	 * @return
	 * @since 1.7
	 */
	protected JedisCluster createCluster(RedisClusterConfiguration clusterConfig, GenericObjectPoolConfig poolConfig) {

		Assert.notNull(clusterConfig, "Cluster configuration must not be null!");

		Set<HostAndPort> hostAndPort = new HashSet<HostAndPort>();
		for (RedisNode node : clusterConfig.getClusterNodes()) {
			hostAndPort.add(new HostAndPort(node.getHost(), node.getPort()));
		}

		int redirects = clusterConfig.getMaxRedirects() != null ? clusterConfig.getMaxRedirects().intValue() : 5;

		int connectTimeout = getConnectTimeout();
		int readTimeout = getReadTimeout();

		return StringUtils.hasText(getPassword())
				? new JedisCluster(hostAndPort, connectTimeout, readTimeout, redirects, password, poolConfig)
				: new JedisCluster(hostAndPort, connectTimeout, readTimeout, redirects, poolConfig);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() {
		if (getUsePool() && pool != null) {
			try {
				pool.destroy();
			} catch (Exception ex) {
				log.warn("Cannot properly close Jedis pool", ex);
			}
			pool = null;
		}
		if (cluster != null) {
			try {
				cluster.close();
			} catch (Exception ex) {
				log.warn("Cannot properly close Jedis cluster", ex);
			}
			try {
				clusterCommandExecutor.destroy();
			} catch (Exception ex) {
				log.warn("Cannot properly close cluster command executor", ex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisConnectionFactory#getConnection()
	 */
	public RedisConnection getConnection() {

		if (cluster != null) {
			return getClusterConnection();
		}

		Jedis jedis = fetchJedisConnector();
		String clientName = clientConfiguration.getClientName().orElse(null);
		JedisConnection connection = (clientConfiguration.usePooling()
				? new JedisConnection(jedis, pool, getDatabase(), clientName)
				: new JedisConnection(jedis, null, getDatabase(), clientName));
		connection.setConvertPipelineAndTxResults(convertPipelineAndTxResults);
		return postProcessConnection(connection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisConnectionFactory#getClusterConnection()
	 */
	@Override
	public RedisClusterConnection getClusterConnection() {

		if (cluster == null) {
			throw new InvalidDataAccessApiUsageException("Cluster is not configured!");
		}
		return new JedisClusterConnection(cluster, clusterCommandExecutor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisConnectionFactory#getReactiveConnection()
	 */
	@Override
	public ReactiveRedisConnection getReactiveConnection() {
		throw new UnsupportedOperationException("Jedis does not support racative connections");
	}

	@Override
	public ReactiveRedisClusterConnection getReactiveClusterConnection() {
		throw new UnsupportedOperationException("Jedis does not support racative connections");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.dao.support.PersistenceExceptionTranslator#translateExceptionIfPossible(java.lang.RuntimeException)
	 */
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return EXCEPTION_TRANSLATION.translate(ex);
	}

	/**
	 * Returns the Redis hostName.
	 * 
	 * @return the hostName.
	 */
	public String getHostName() {
		return standaloneConfig.getHostName();
	}

	/**
	 * Sets the Redis hostName.
	 * 
	 * @param hostName the hostName to set.
	 * @deprecated since 2.0, set the port using {@link RedisStandaloneConfiguration}.
	 */
	@Deprecated
	public void setHostName(String hostName) {
		standaloneConfig.setHostName(hostName);
	}

	/**
	 * Sets whether to use SSL.
	 *
	 * @param useSsl {@literal true} to use SSL.
	 * @since 1.8
	 * @deprecated since 2.0, set the SSL usage with {@link JedisClientConfiguration}.
	 */
	@Deprecated
	public void setUseSsl(boolean useSsl) {
		getMutableConfiguration().setUseSsl(useSsl);
	}

	/**
	 * Returns whether to use SSL.
	 *
	 * @return use of SSL.
	 * @since 1.8
	 */
	public boolean isUseSsl() {
		return clientConfiguration.useSsl();
	}

	/**
	 * Returns the password used for authenticating with the Redis server.
	 * 
	 * @return password for authentication.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password used for authenticating with the Redis server.
	 * 
	 * @param password the password to set.
	 * @deprecated since 2.0, set the password using {@link RedisStandaloneConfiguration},
	 *             {@link RedisSentinelConfiguration} or {@link RedisClusterConfiguration}.
	 */
	@Deprecated
	public void setPassword(String password) {

		if (isRedisSentinelAware()) {
			sentinelConfig.setPassword(password);
			return;
		}

		if (isRedisClusterAware()) {
			clusterConfig.setPassword(password);
			return;
		}

		standaloneConfig.setPassword(password);
	}

	/**
	 * Returns the port used to connect to the Redis instance.
	 * 
	 * @return the Redis port.
	 */
	public int getPort() {
		return standaloneConfig.getPort();
	}

	/**
	 * Sets the port used to connect to the Redis instance.
	 * 
	 * @param port the Redis port.
	 * @deprecated since 2.0, set the port using {@link RedisStandaloneConfiguration}.
	 */
	public void setPort(int port) {
		standaloneConfig.setPort(port);
	}

	/**
	 * Returns the shardInfo.
	 * 
	 * @return the shardInfo.
	 * @deprecated since 2.0.
	 */
	@Deprecated
	public JedisShardInfo getShardInfo() {
		return shardInfo;
	}

	/**
	 * Sets the shard info for this factory.
	 * 
	 * @param shardInfo the shardInfo to set.
	 * @deprecated since 2.0, set the shard ingo using {@link JedisClientConfiguration}.
	 */
	@Deprecated
	public void setShardInfo(JedisShardInfo shardInfo) {

		this.shardInfo = shardInfo;
		getMutableConfiguration().setShardInfo(shardInfo);
	}

	/**
	 * Returns the timeout.
	 * 
	 * @return the timeout.
	 */
	public int getTimeout() {
		return getReadTimeout();
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout to set.
	 * @deprecated since 2.0, set the timeout using {@link JedisClientConfiguration}.
	 */
	@Deprecated
	public void setTimeout(int timeout) {

		getMutableConfiguration().setReadTimeout(Timeout.create(timeout, TimeUnit.MILLISECONDS));
		getMutableConfiguration().setConnectTimeout(Timeout.create(timeout, TimeUnit.MILLISECONDS));
	}

	/**
	 * Indicates the use of a connection pool.
	 * 
	 * @return the use of connection pooling.
	 */
	public boolean getUsePool() {
		return clientConfiguration.usePooling();
	}

	/**
	 * Turns on or off the use of connection pooling.
	 * 
	 * @param usePool the usePool to set.
	 * @deprecated since 2.0, set the pooling usage with {@link JedisClientConfiguration}.
	 */
	@Deprecated
	public void setUsePool(boolean usePool) {
		getMutableConfiguration().setUsePooling(usePool);
	}

	/**
	 * Returns the poolConfig.
	 * 
	 * @return the poolConfig
	 */
	public JedisPoolConfig getPoolConfig() {
		return clientConfiguration.getPoolConfig().orElse(null);
	}

	/**
	 * Sets the pool configuration for this factory.
	 * 
	 * @param poolConfig the poolConfig to set.
	 * @deprecated since 2.0, set the {@link JedisPoolConfig} using {@link JedisClientConfiguration}.
	 */
	@Deprecated
	public void setPoolConfig(JedisPoolConfig poolConfig) {
		getMutableConfiguration().setPoolConfig(poolConfig);
	}

	/**
	 * Returns the index of the database.
	 * 
	 * @return the database index.
	 */
	public int getDatabase() {

		if (isRedisSentinelAware()) {
			return sentinelConfig.getDatabase();
		}

		return standaloneConfig.getDatabase();
	}

	/**
	 * Sets the index of the database used by this connection factory. Default is 0.
	 * 
	 * @param index database index.
	 * @deprecated since 2.0, set the client name using {@link RedisSentinelConfiguration} or
	 *             {@link RedisStandaloneConfiguration}.
	 */
	@Deprecated
	public void setDatabase(int index) {

		Assert.isTrue(index >= 0, "invalid DB index (a positive index required)");

		if (isRedisSentinelAware()) {
			sentinelConfig.setDatabase(index);
			return;
		}

		standaloneConfig.setDatabase(index);
	}

	/**
	 * Returns the client name.
	 *
	 * @return the client name.
	 * @since 1.8
	 */
	public String getClientName() {
		return clientConfiguration.getClientName().orElse(null);
	}

	/**
	 * Sets the client name used by this connection factory. Defaults to none which does not set a client name.
	 *
	 * @param clientName the client name.
	 * @since 1.8
	 * @deprecated Set the client name using {@link JedisClientConfiguration}.
	 */
	@Deprecated
	public void setClientName(String clientName) {
		this.getMutableConfiguration().setClientName(clientName);
	}

	/**
	 * @return the {@link JedisClientConfiguration}.
	 * @since 2.0
	 */
	public JedisClientConfiguration getClientConfiguration() {
		return clientConfiguration;
	}

	/**
	 * @return the {@link RedisStandaloneConfiguration}.
	 * @since 2.0
	 */
	public RedisStandaloneConfiguration getStandaloneConfig() {
		return standaloneConfig;
	}

	/**
	 * @return the {@link RedisStandaloneConfiguration}, may be {@literal null}.
	 * @since 2.0
	 */
	public RedisSentinelConfiguration getSentinelConfig() {
		return sentinelConfig;
	}

	/**
	 * @return the {@link RedisClusterConfiguration}, may be {@literal null}.
	 * @since 2.0
	 */
	public RedisClusterConfiguration getClusterConfig() {
		return clusterConfig;
	}

	/**
	 * Specifies if pipelined results should be converted to the expected data type. If false, results of
	 * {@link JedisConnection#closePipeline()} and {@link JedisConnection#exec()} will be of the type returned by the
	 * Jedis driver.
	 * 
	 * @return Whether or not to convert pipeline and tx results.
	 */
	public boolean getConvertPipelineAndTxResults() {
		return convertPipelineAndTxResults;
	}

	/**
	 * Specifies if pipelined results should be converted to the expected data type. If false, results of
	 * {@link JedisConnection#closePipeline()} and {@link JedisConnection#exec()} will be of the type returned by the
	 * Jedis driver.
	 * 
	 * @param convertPipelineAndTxResults Whether or not to convert pipeline and tx results.
	 */
	public void setConvertPipelineAndTxResults(boolean convertPipelineAndTxResults) {
		this.convertPipelineAndTxResults = convertPipelineAndTxResults;
	}

	/**
	 * @return true when {@link RedisSentinelConfiguration} is present.
	 * @since 1.4
	 */
	public boolean isRedisSentinelAware() {
		return sentinelConfig != null;
	}

	/**
	 * @return true when {@link RedisSentinelConfiguration} is present.
	 * @since 1.4
	 */
	public boolean isRedisClusterAware() {
		return clusterConfig != null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisConnectionFactory#getSentinelConnection()
	 */
	@Override
	public RedisSentinelConnection getSentinelConnection() {

		if (!isRedisSentinelAware()) {
			throw new InvalidDataAccessResourceUsageException("No Sentinels configured");
		}

		return new JedisSentinelConnection(getActiveSentinel());
	}

	private Jedis getActiveSentinel() {

		Assert.notNull(this.sentinelConfig, "SentinelConfig must not be null!");

		for (RedisNode node : this.sentinelConfig.getSentinels()) {

			Jedis jedis = new Jedis(node.getHost(), node.getPort());

			if (jedis.ping().equalsIgnoreCase("pong")) {

				potentiallySetClientName(jedis);
				return jedis;
			}
		}

		throw new InvalidDataAccessResourceUsageException("No Sentinel found");
	}

	private Set<String> convertToJedisSentinelSet(Collection<RedisNode> nodes) {

		if (CollectionUtils.isEmpty(nodes)) {
			return Collections.emptySet();
		}

		Set<String> convertedNodes = new LinkedHashSet<String>(nodes.size());
		for (RedisNode node : nodes) {
			if (node != null) {
				convertedNodes.add(node.asString());
			}
		}
		return convertedNodes;
	}

	private void potentiallySetClientName(Jedis jedis) {
		clientConfiguration.getClientName().ifPresent(jedis::clientSetname);
	}

	private void setTimeoutOn(JedisShardInfo shardInfo, int timeout) {
		ReflectionUtils.invokeMethod(SET_TIMEOUT_METHOD, shardInfo, timeout);
	}

	private int getTimeoutFrom(JedisShardInfo shardInfo) {
		return (Integer) ReflectionUtils.invokeMethod(GET_TIMEOUT_METHOD, shardInfo);
	}

	private int getReadTimeout() {
		return Math.toIntExact(clientConfiguration.getReadTimeout().getTimeout(TimeUnit.MILLISECONDS));
	}

	private int getConnectTimeout() {
		return Math.toIntExact(clientConfiguration.getConnectTimeout().getTimeout(TimeUnit.MILLISECONDS));
	}

	private MutableJedisClientConfiguration getMutableConfiguration() {

		Assert.state(clientConfiguration instanceof MutableJedisClientConfiguration,
				() -> String.format("Client configuration must be instance of MutableJedisClientConfiguration but is %s",
						ClassUtils.getShortName(clientConfiguration.getClass())));

		return (MutableJedisClientConfiguration) clientConfiguration;
	}

	static class MutableJedisClientConfiguration implements JedisClientConfiguration {

		private boolean useSsl;
		private SSLSocketFactory sslSocketFactory;
		private SSLParameters sslParameters;
		private HostnameVerifier hostnameVerifier;
		private boolean usePooling = true;
		private JedisPoolConfig poolConfig = new JedisPoolConfig();
		private String clientName;
		private Timeout readTimeout = Timeout.create(Protocol.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
		private Timeout connectTimeout = Timeout.create(Protocol.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#useSsl()
		 */
		@Override
		public boolean useSsl() {
			return useSsl;
		}

		public void setUseSsl(boolean useSsl) {
			this.useSsl = useSsl;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#getSslSocketFactory()
		 */
		@Override
		public Optional<SSLSocketFactory> getSslSocketFactory() {
			return Optional.ofNullable(sslSocketFactory);
		}

		public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
			this.sslSocketFactory = sslSocketFactory;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#getSslParameters()
		 */
		@Override
		public Optional<SSLParameters> getSslParameters() {
			return Optional.ofNullable(sslParameters);
		}

		public void setSslParameters(SSLParameters sslParameters) {
			this.sslParameters = sslParameters;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#getHostnameVerifier()
		 */
		@Override
		public Optional<HostnameVerifier> getHostnameVerifier() {
			return Optional.ofNullable(hostnameVerifier);
		}

		public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
			this.hostnameVerifier = hostnameVerifier;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#usePooling()
		 */
		@Override
		public boolean usePooling() {
			return usePooling;
		}

		public void setUsePooling(boolean usePooling) {
			this.usePooling = usePooling;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#getPoolConfig()
		 */
		@Override
		public Optional<JedisPoolConfig> getPoolConfig() {
			return Optional.ofNullable(poolConfig);
		}

		public void setPoolConfig(JedisPoolConfig poolConfig) {
			this.poolConfig = poolConfig;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#getClientName()
		 */
		@Override
		public Optional<String> getClientName() {
			return Optional.ofNullable(clientName);
		}

		public void setClientName(String clientName) {
			this.clientName = clientName;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#getReadTimeout()
		 */
		@Override
		public Timeout getReadTimeout() {
			return readTimeout;
		}

		public void setReadTimeout(Timeout readTimeout) {
			this.readTimeout = readTimeout;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.redis.connection.jedis.JedisClientConfiguration#getConnectTimeout()
		 */
		@Override
		public Timeout getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(Timeout connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public static JedisClientConfiguration create(JedisShardInfo shardInfo) {

			MutableJedisClientConfiguration configuration = new MutableJedisClientConfiguration();

			configuration.setShardInfo(shardInfo);

			return configuration;
		}

		public static JedisClientConfiguration create(JedisPoolConfig jedisPoolConfig) {

			MutableJedisClientConfiguration configuration = new MutableJedisClientConfiguration();

			configuration.setPoolConfig(jedisPoolConfig);

			return configuration;
		}

		public void setShardInfo(JedisShardInfo shardInfo) {

			setSslSocketFactory(shardInfo.getSslSocketFactory());
			setSslParameters(shardInfo.getSslParameters());
			setHostnameVerifier(shardInfo.getHostnameVerifier());
			setUseSsl(shardInfo.getSsl());
			setConnectTimeout(Timeout.create(shardInfo.getConnectionTimeout(), TimeUnit.MILLISECONDS));
			setReadTimeout(Timeout.create(shardInfo.getSoTimeout(), TimeUnit.MILLISECONDS));
		}
	}

}
