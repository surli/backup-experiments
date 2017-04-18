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

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Builder;
import redis.clients.jedis.Client;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol.Command;
import redis.clients.jedis.Queable;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.Pool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.ExceptionTranslationStrategy;
import org.springframework.data.redis.FallbackExceptionTranslationStrategy;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.AbstractRedisConnection;
import org.springframework.data.redis.connection.FutureResult;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.RedisHyperLogLogCommands;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPipelineException;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisSubscribedConnectionException;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.Subscription;
import org.springframework.data.redis.connection.convert.TransactionResultConverter;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.KeyBoundCursor;
import org.springframework.data.redis.core.ScanCursor;
import org.springframework.data.redis.core.ScanIteration;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.RedisClientInfo;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@code RedisConnection} implementation on top of <a href="http://github.com/xetorthio/jedis">Jedis</a> library.
 *
 * @author Costin Leau
 * @author Jennifer Hickey
 * @author Christoph Strobl
 * @author Thomas Darimont
 * @author Jungtaek Lim
 * @author Konstantin Shchepanovskyi
 * @author David Liu
 * @author Milan Agatonovic
 * @author Mark Paluch
 * @author Ninad Divadkar
 */
public class JedisConnection extends AbstractRedisConnection {

	private static final Field CLIENT_FIELD;
	private static final Method SEND_COMMAND;
	private static final Method GET_RESPONSE;

	private static final String SHUTDOWN_SCRIPT = "return redis.call('SHUTDOWN','%s')";

	private static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION = new FallbackExceptionTranslationStrategy(
			JedisConverters.exceptionConverter());

	static {

		CLIENT_FIELD = ReflectionUtils.findField(BinaryJedis.class, "client", Client.class);
		ReflectionUtils.makeAccessible(CLIENT_FIELD);

		try {
			Class<?> commandType = ClassUtils.isPresent("redis.clients.jedis.ProtocolCommand", null)
					? ClassUtils.forName("redis.clients.jedis.ProtocolCommand", null)
					: ClassUtils.forName("redis.clients.jedis.Protocol$Command", null);

			SEND_COMMAND = ReflectionUtils.findMethod(Connection.class, "sendCommand",
					new Class[] { commandType, byte[][].class });
		} catch (Exception e) {
			throw new NoClassDefFoundError(
					"Could not find required flavor of command required by 'redis.clients.jedis.Connection#sendCommand'.");
		}

		ReflectionUtils.makeAccessible(SEND_COMMAND);

		GET_RESPONSE = ReflectionUtils.findMethod(Queable.class, "getResponse", Builder.class);
		ReflectionUtils.makeAccessible(GET_RESPONSE);
	}

	private final Jedis jedis;
	private final Client client;
	private Transaction transaction;
	private final Pool<Jedis> pool;
	/**
	 * flag indicating whether the connection needs to be dropped or not
	 */
	private boolean broken = false;
	private volatile JedisSubscription subscription;
	private volatile Pipeline pipeline;
	private final int dbIndex;
	private final String clientName;
	private boolean convertPipelineAndTxResults = true;
	private List<FutureResult<Response<?>>> pipelinedResults = new ArrayList<FutureResult<Response<?>>>();
	private Queue<FutureResult<Response<?>>> txResults = new LinkedList<FutureResult<Response<?>>>();

	class JedisResult extends FutureResult<Response<?>> {
		public <T> JedisResult(Response<T> resultHolder, Converter<T, ?> converter) {
			super(resultHolder, converter);
		}

		public <T> JedisResult(Response<T> resultHolder) {
			super(resultHolder);
		}

		@SuppressWarnings("unchecked")
		public Object get() {
			if (convertPipelineAndTxResults && converter != null) {
				return converter.convert(resultHolder.get());
			}
			return resultHolder.get();
		}
	}

	private class JedisStatusResult extends JedisResult {
		public JedisStatusResult(Response<?> resultHolder) {
			super(resultHolder);
			setStatus(true);
		}
	}

	/**
	 * Constructs a new <code>JedisConnection</code> instance.
	 *
	 * @param jedis Jedis entity
	 */
	public JedisConnection(Jedis jedis) {
		this(jedis, null, 0);
	}

	/**
	 * Constructs a new <code>JedisConnection</code> instance backed by a jedis pool.
	 *
	 * @param jedis
	 * @param pool can be null, if no pool is used
	 * @param dbIndex
	 */
	public JedisConnection(Jedis jedis, Pool<Jedis> pool, int dbIndex) {
		this(jedis, pool, dbIndex, null);
	}

	/**
	 * Constructs a new <code>JedisConnection</code> instance backed by a jedis pool.
	 *
	 * @param jedis
	 * @param pool can be null, if no pool is used
	 * @param dbIndex
	 * @param clientName the client name, can be {@literal null}.
	 * @since 1.8
	 */
	protected JedisConnection(Jedis jedis, Pool<Jedis> pool, int dbIndex, String clientName) {

		// extract underlying connection for batch operations
		client = (Client) ReflectionUtils.getField(CLIENT_FIELD, jedis);

		this.jedis = jedis;
		this.pool = pool;
		this.dbIndex = dbIndex;
		this.clientName = clientName;

		// select the db
		// if this fail, do manual clean-up before propagating the exception
		// as we're inside the constructor
		if (dbIndex > 0) {
			try {
				select(dbIndex);
			} catch (DataAccessException ex) {
				close();
				throw ex;
			}
		}
	}

	protected DataAccessException convertJedisAccessException(Exception ex) {

		if (ex instanceof NullPointerException) {
			// An NPE before flush will leave data in the OutputStream of a pooled connection
			broken = true;
		}

		DataAccessException exception = EXCEPTION_TRANSLATION.translate(ex);
		if (exception instanceof RedisConnectionFailureException) {
			broken = true;
		}

		return exception;
	}

	public Object execute(String command, byte[]... args) {
		Assert.hasText(command, "a valid command needs to be specified");
		try {
			List<byte[]> mArgs = new ArrayList<byte[]>();
			if (!ObjectUtils.isEmpty(args)) {
				Collections.addAll(mArgs, args);
			}

			ReflectionUtils.invokeMethod(SEND_COMMAND, client, Command.valueOf(command.trim().toUpperCase()),
					mArgs.toArray(new byte[mArgs.size()][]));
			if (isQueueing() || isPipelined()) {
				Object target = (isPipelined() ? pipeline : transaction);
				@SuppressWarnings("unchecked")
				Response<Object> result = (Response<Object>) ReflectionUtils.invokeMethod(GET_RESPONSE, target,
						new Builder<Object>() {
							public Object build(Object data) {
								return data;
							}

							public String toString() {
								return "Object";
							}
						});
				if (isPipelined()) {
					pipeline(new JedisResult(result));
				} else {
					transaction(new JedisResult(result));
				}
				return null;
			}
			return client.getOne();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void close() throws DataAccessException {
		super.close();
		// return the connection to the pool
		if (pool != null) {
			if (!broken) {
				// reset the connection
				try {
					if (dbIndex > 0) {
						jedis.select(0);
					}
					pool.returnResource(jedis);
					return;
				} catch (Exception ex) {
					DataAccessException dae = convertJedisAccessException(ex);
					if (broken) {
						pool.returnBrokenResource(jedis);
					} else {
						pool.returnResource(jedis);
					}
					throw dae;
				}
			} else {
				pool.returnBrokenResource(jedis);
				return;
			}
		}
		// else close the connection normally (doing the try/catch dance)
		Exception exc = null;
		if (isQueueing()) {
			try {
				client.quit();
			} catch (Exception ex) {
				exc = ex;
			}
			try {
				client.disconnect();
			} catch (Exception ex) {
				exc = ex;
			}
			return;
		}
		try {
			jedis.quit();
		} catch (Exception ex) {
			exc = ex;
		}
		try {
			jedis.disconnect();
		} catch (Exception ex) {
			exc = ex;
		}
		if (exc != null)
			throw convertJedisAccessException(exc);
	}

	public Jedis getNativeConnection() {
		return jedis;
	}

	public boolean isClosed() {
		try {
			return !jedis.isConnected();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public boolean isQueueing() {
		return client.isInMulti();
	}

	public boolean isPipelined() {
		return (pipeline != null);
	}

	public void openPipeline() {
		if (pipeline == null) {
			pipeline = jedis.pipelined();
		}
	}

	public List<Object> closePipeline() {
		if (pipeline != null) {
			try {
				return convertPipelineResults();
			} finally {
				pipeline = null;
				pipelinedResults.clear();
			}
		}
		return Collections.emptyList();
	}

	private List<Object> convertPipelineResults() {
		List<Object> results = new ArrayList<Object>();
		pipeline.sync();
		Exception cause = null;
		for (FutureResult<Response<?>> result : pipelinedResults) {
			try {
				Object data = result.get();
				if (!convertPipelineAndTxResults || !(result.isStatus())) {
					results.add(data);
				}
			} catch (JedisDataException e) {
				DataAccessException dataAccessException = convertJedisAccessException(e);
				if (cause == null) {
					cause = dataAccessException;
				}
				results.add(dataAccessException);
			} catch (DataAccessException e) {
				if (cause == null) {
					cause = e;
				}
				results.add(e);
			}
		}
		if (cause != null) {
			throw new RedisPipelineException(cause, results);
		}
		return results;
	}

	private void doPipelined(Response<?> response) {
		pipeline(new JedisStatusResult(response));
	}

	void pipeline(FutureResult<Response<?>> result) {
		if (isQueueing()) {
			transaction(result);
		} else {
			pipelinedResults.add(result);
		}
	}

	private void doQueued(Response<?> response) {
		transaction(new JedisStatusResult(response));
	}

	void transaction(FutureResult<Response<?>> result) {
		txResults.add(result);
	}

	public List<byte[]> sort(byte[] key, SortParameters params) {

		SortingParams sortParams = JedisConverters.toSortingParams(params);

		try {
			if (isPipelined()) {
				if (sortParams != null) {
					pipeline(new JedisResult(pipeline.sort(key, sortParams)));
				} else {
					pipeline(new JedisResult(pipeline.sort(key)));
				}

				return null;
			}
			if (isQueueing()) {
				if (sortParams != null) {
					transaction(new JedisResult(transaction.sort(key, sortParams)));
				} else {
					transaction(new JedisResult(transaction.sort(key)));
				}

				return null;
			}
			return (sortParams != null ? jedis.sort(key, sortParams) : jedis.sort(key));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Long sort(byte[] key, SortParameters params, byte[] storeKey) {

		SortingParams sortParams = JedisConverters.toSortingParams(params);

		try {
			if (isPipelined()) {
				if (sortParams != null) {
					pipeline(new JedisResult(pipeline.sort(key, sortParams, storeKey)));
				} else {
					pipeline(new JedisResult(pipeline.sort(key, storeKey)));
				}

				return null;
			}
			if (isQueueing()) {
				if (sortParams != null) {
					transaction(new JedisResult(transaction.sort(key, sortParams, storeKey)));
				} else {
					transaction(new JedisResult(transaction.sort(key, storeKey)));
				}

				return null;
			}
			return (sortParams != null ? jedis.sort(key, sortParams, storeKey) : jedis.sort(key, storeKey));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Long dbSize() {
		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.dbSize()));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.dbSize()));
				return null;
			}
			return jedis.dbSize();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void flushDb() {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.flushDB()));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisStatusResult(transaction.flushDB()));
				return;
			}
			jedis.flushDB();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void flushAll() {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.flushAll()));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.flushAll()));
				return;
			}
			jedis.flushAll();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void bgSave() {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.bgsave()));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisStatusResult(transaction.bgsave()));
				return;
			}
			jedis.bgsave();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	@Override
	public void bgReWriteAof() {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.bgrewriteaof()));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisStatusResult(transaction.bgrewriteaof()));
				return;
			}
			jedis.bgrewriteaof();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/**
	 * @deprecated As of 1.3, use {@link #bgReWriteAof}.
	 */
	@Deprecated
	public void bgWriteAof() {
		bgReWriteAof();
	}

	public void save() {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.save()));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisStatusResult(transaction.save()));
				return;
			}
			jedis.save();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public List<String> getConfig(String param) {
		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.configGet(param)));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.configGet(param)));
				return null;
			}
			return jedis.configGet(param);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Properties info() {
		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.info(), JedisConverters.stringToProps()));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.info(), JedisConverters.stringToProps()));
				return null;
			}
			return JedisConverters.toProperties(jedis.info());
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Properties info(String section) {
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}
		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		try {
			return JedisConverters.toProperties(jedis.info(section));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Long lastSave() {
		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.lastsave()));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.lastsave()));
				return null;
			}
			return jedis.lastsave();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void setConfig(String param, String value) {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.configSet(param, value)));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisStatusResult(transaction.configSet(param, value)));
				return;
			}
			jedis.configSet(param, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void resetConfigStats() {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.configResetStat()));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisStatusResult(transaction.configResetStat()));
				return;
			}
			jedis.configResetStat();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void shutdown() {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.shutdown()));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisStatusResult(transaction.shutdown()));
				return;
			}
			jedis.shutdown();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	* (non-Javadoc)
	* @see org.springframework.data.redis.connection.RedisServerCommands#shutdown(org.springframework.data.redis.connection.RedisServerCommands.ShutdownOption)
	*/
	@Override
	public void shutdown(ShutdownOption option) {

		if (option == null) {
			shutdown();
			return;
		}

		eval(String.format(SHUTDOWN_SCRIPT, option.name()).getBytes(), ReturnType.STATUS, 0);
	}

	public byte[] echo(byte[] message) {
		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.echo(message)));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.echo(message)));
				return null;
			}
			return jedis.echo(message);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public String ping() {
		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.ping()));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.ping()));
				return null;
			}
			return jedis.ping();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void discard() {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.discard()));
				return;
			}
			transaction.discard();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		} finally {
			txResults.clear();
			transaction = null;
		}
	}

	public List<Object> exec() {
		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.exec(), new TransactionResultConverter<Response<?>>(
						new LinkedList<FutureResult<Response<?>>>(txResults), JedisConverters.exceptionConverter())));
				return null;
			}

			if (transaction == null) {
				throw new InvalidDataAccessApiUsageException("No ongoing transaction. Did you forget to call multi?");
			}
			List<Object> results = transaction.exec();
			return convertPipelineAndTxResults && !CollectionUtils.isEmpty(results)
					? new TransactionResultConverter<Response<?>>(txResults, JedisConverters.exceptionConverter())
							.convert(results)
					: results;
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		} finally {
			txResults.clear();
			transaction = null;
		}
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public Jedis getJedis() {
		return jedis;
	}

	@Override
	public RedisKeyCommands keyCommands() {
		return new JedisKeyCommands(this);
	}

	@Override
	public RedisStringCommands stringCommands() {
		return new JedisStringCommands(this);
	}

	@Override
	public RedisListCommands listCommands() {
		return new JedisListCommands(this);
	}

	@Override
	public RedisSetCommands setCommands() {
		return new JedisSetCommands(this);
	}

	@Override
	public RedisZSetCommands zSetCommands() {
		return new JedisZSetCommands(this);
	}

	@Override
	public RedisHashCommands hashCommands() {
		return new JedisHashCommands(this);
	}

	@Override
	public RedisGeoCommands geoCommands() {
		return new JedisGeoCommands(this);
	}

	@Override
	public RedisHyperLogLogCommands hyperLogLogCommands() {
		return new JedisHyperLogLogCommands(this);
	}

	JedisResult newJedisResult(Response<?> response) {
		return new JedisResult(response);
	}

	<T> JedisResult newJedisResult(Response<T> response, Converter<T, ?> converter) {
		return new JedisResult(response, converter);
	}

	JedisStatusResult newStatusResult(Response<?> response) {
		return new JedisStatusResult(response);
	}

	public Boolean expire(byte[] key, long seconds) {

		Assert.notNull(key, "Key must not be null!");

		if (seconds > Integer.MAX_VALUE) {
			return pExpire(key, TimeUnit.SECONDS.toMillis(seconds));
		}

		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.expire(key, (int) seconds), JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.expire(key, (int) seconds), JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(jedis.expire(key, (int) seconds));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Boolean expireAt(byte[] key, long unixTime) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.expireAt(key, unixTime), JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.expireAt(key, unixTime), JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(jedis.expireAt(key, unixTime));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void multi() {
		if (isQueueing()) {
			return;
		}
		try {
			if (isPipelined()) {
				pipeline.multi();
				return;
			}
			this.transaction = jedis.multi();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void select(int dbIndex) {
		try {
			if (isPipelined()) {
				pipeline(new JedisStatusResult(pipeline.select(dbIndex)));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisStatusResult(transaction.select(dbIndex)));
				return;
			}
			jedis.select(dbIndex);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void unwatch() {
		try {
			jedis.unwatch();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void watch(byte[]... keys) {
		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		try {
			for (byte[] key : keys) {
				if (isPipelined()) {
					pipeline(new JedisStatusResult(pipeline.watch(key)));
				} else {
					jedis.watch(key);
				}
			}
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	//
	// Pub/Sub functionality
	//

	public Long publish(byte[] channel, byte[] message) {
		try {
			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.publish(channel, message)));
				return null;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.publish(channel, message)));
				return null;
			}
			return jedis.publish(channel, message);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public boolean isSubscribed() {
		return (subscription != null && subscription.isAlive());
	}

	public void pSubscribe(MessageListener listener, byte[]... patterns) {
		if (isSubscribed()) {
			throw new RedisSubscribedConnectionException(
					"Connection already subscribed; use the connection Subscription to cancel or add new channels");
		}
		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}

		try {
			BinaryJedisPubSub jedisPubSub = new JedisMessageListener(listener);

			subscription = new JedisSubscription(listener, jedisPubSub, null, patterns);
			jedis.psubscribe(jedisPubSub, patterns);

		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void subscribe(MessageListener listener, byte[]... channels) {
		if (isSubscribed()) {
			throw new RedisSubscribedConnectionException(
					"Connection already subscribed; use the connection Subscription to cancel or add new channels");
		}

		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}

		try {
			BinaryJedisPubSub jedisPubSub = new JedisMessageListener(listener);

			subscription = new JedisSubscription(listener, jedisPubSub, channels, null);
			jedis.subscribe(jedisPubSub, channels);

		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	//
	// Scripting commands
	//

	public void scriptFlush() {
		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}
		try {
			jedis.scriptFlush();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void scriptKill() {
		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}
		try {
			jedis.scriptKill();
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public String scriptLoad(byte[] script) {
		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}
		try {
			return JedisConverters.toString(jedis.scriptLoad(script));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public List<Boolean> scriptExists(String... scriptSha1) {
		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}
		try {
			return jedis.scriptExists(scriptSha1);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T eval(byte[] script, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}
		try {
			return (T) new JedisScriptReturnConverter(returnType)
					.convert(jedis.eval(script, JedisConverters.toBytes(numKeys), keysAndArgs));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public <T> T evalSha(String scriptSha1, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
		return evalSha(JedisConverters.toBytes(scriptSha1), returnType, numKeys, keysAndArgs);
	}

	@SuppressWarnings("unchecked")
	public <T> T evalSha(byte[] scriptSha1, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {

		if (isQueueing()) {
			throw new UnsupportedOperationException();
		}
		if (isPipelined()) {
			throw new UnsupportedOperationException();
		}
		try {
			return (T) new JedisScriptReturnConverter(returnType).convert(jedis.evalsha(scriptSha1, numKeys, keysAndArgs));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisServerCommands#time()
	 */
	@Override
	public Long time() {

		try {

			if (isPipelined()) {
				pipeline(new JedisResult(pipeline.time(), JedisConverters.toTimeConverter()));
				return null;
			}

			if (isQueueing()) {
				transaction(new JedisResult(transaction.time(), JedisConverters.toTimeConverter()));
				return null;
			}
			return JedisConverters.toTimeConverter().convert(jedis.time());
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisServerCommands#killClient(byte[])
	 */
	@Override
	public void killClient(String host, int port) {

		Assert.hasText(host, "Host for 'CLIENT KILL' must not be 'null' or 'empty'.");

		if (isQueueing() || isPipelined()) {
			throw new UnsupportedOperationException("'CLIENT KILL' is not supported in transaction / pipline mode.");
		}

		try {
			this.jedis.clientKill(String.format("%s:%s", host, port));
		} catch (Exception e) {
			throw convertJedisAccessException(e);
		}
	}

	/*
	 * @see org.springframework.data.redis.connection.RedisServerCommands#slaveOf(java.lang.String, int)
	 */
	@Override
	public void slaveOf(String host, int port) {

		Assert.hasText(host, "Host must not be null for 'SLAVEOF' command.");
		if (isQueueing() || isPipelined()) {
			throw new UnsupportedOperationException("'SLAVEOF' cannot be called in pipline / transaction mode.");
		}
		try {
			this.jedis.slaveof(host, port);
		} catch (Exception e) {
			throw convertJedisAccessException(e);
		}
	}

	/*
	* @see org.springframework.data.redis.connection.RedisServerCommands#setClientName(java.lang.String)
	*/
	@Override
	public void setClientName(byte[] name) {

		if (isPipelined() || isQueueing()) {
			throw new UnsupportedOperationException("'CLIENT SETNAME' is not suppored in transacton / pipeline mode.");
		}

		jedis.clientSetname(name);
	}

	/*
	 * @see org.springframework.data.redis.connection.RedisServerCommands#getClientName()
	 */
	@Override
	public String getClientName() {

		if (isPipelined() || isQueueing()) {
			throw new UnsupportedOperationException();
		}

		return jedis.clientGetname();
	}

	/*
	 * @see org.springframework.data.redis.connection.RedisServerCommands#getClientName()
	 */
	@Override
	public List<RedisClientInfo> getClientList() {

		if (isQueueing() || isPipelined()) {
			throw new UnsupportedOperationException("'CLIENT LIST' is not supported in in pipeline / multi mode.");
		}
		return JedisConverters.toListOfRedisClientInformation(this.jedis.clientList());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisServerCommands#slaveOfNoOne()
	 */
	@Override
	public void slaveOfNoOne() {

		if (isQueueing() || isPipelined()) {
			throw new UnsupportedOperationException("'SLAVEOF' cannot be called in pipline / transaction mode.");
		}
		try {
			this.jedis.slaveofNoOne();
		} catch (Exception e) {
			throw convertJedisAccessException(e);
		}
	}

	/**
	 * @since 1.4
	 * @return
	 */
	public Cursor<byte[]> scan() {
		return scan(ScanOptions.NONE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisKeyCommands#scan(org.springframework.data.redis.core.ScanOptions)
	 */
	public Cursor<byte[]> scan(ScanOptions options) {
		return scan(0, options != null ? options : ScanOptions.NONE);
	}

	/**
	 * @since 1.4
	 * @param cursorId
	 * @param options
	 * @return
	 */
	public Cursor<byte[]> scan(long cursorId, ScanOptions options) {

		return new ScanCursor<byte[]>(cursorId, options) {

			@Override
			protected ScanIteration<byte[]> doScan(long cursorId, ScanOptions options) {

				if (isQueueing() || isPipelined()) {
					throw new UnsupportedOperationException("'SCAN' cannot be called in pipeline / transaction mode.");
				}

				ScanParams params = JedisConverters.toScanParams(options);
				redis.clients.jedis.ScanResult<String> result = jedis.scan(Long.toString(cursorId), params);
				return new ScanIteration<byte[]>(Long.valueOf(result.getStringCursor()),
						JedisConverters.stringListToByteList().convert(result.getResult()));
			}

			protected void doClose() {
				JedisConnection.this.close();
			};

		}.open();

	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisHashCommands#hScan(byte[], org.springframework.data.redis.core.ScanOptions)
	 */
	@Override
	public Cursor<Entry<byte[], byte[]>> hScan(byte[] key, ScanOptions options) {
		return hScan(key, 0, options);
	}

	/**
	 * @since 1.4
	 * @param key
	 * @param cursorId
	 * @param options
	 * @return
	 */
	public Cursor<Entry<byte[], byte[]>> hScan(byte[] key, long cursorId, ScanOptions options) {

		return new KeyBoundCursor<Map.Entry<byte[], byte[]>>(key, cursorId, options) {

			@Override
			protected ScanIteration<Entry<byte[], byte[]>> doScan(byte[] key, long cursorId, ScanOptions options) {

				if (isQueueing() || isPipelined()) {
					throw new UnsupportedOperationException("'HSCAN' cannot be called in pipeline / transaction mode.");
				}

				ScanParams params = JedisConverters.toScanParams(options);

				ScanResult<Entry<byte[], byte[]>> result = jedis.hscan(key, JedisConverters.toBytes(cursorId), params);
				return new ScanIteration<Map.Entry<byte[], byte[]>>(Long.valueOf(result.getStringCursor()), result.getResult());
			}

			protected void doClose() {
				JedisConnection.this.close();
			};

		}.open();
	}

	/**
	 * Specifies if pipelined results should be converted to the expected data type. If false, results of
	 * {@link #closePipeline()} and {@link #exec()} will be of the type returned by the Jedis driver
	 *
	 * @param convertPipelineAndTxResults Whether or not to convert pipeline and tx results
	 */
	public void setConvertPipelineAndTxResults(boolean convertPipelineAndTxResults) {
		this.convertPipelineAndTxResults = convertPipelineAndTxResults;
	}

	@Override
	protected boolean isActive(RedisNode node) {

		if (node == null) {
			return false;
		}

		Jedis temp = null;
		try {
			temp = getJedis(node);
			temp.connect();
			return temp.ping().equalsIgnoreCase("pong");
		} catch (Exception e) {
			return false;
		} finally {
			if (temp != null) {
				temp.disconnect();
				temp.close();
			}
		}
	}

	@Override
	protected JedisSentinelConnection getSentinelConnection(RedisNode sentinel) {
		return new JedisSentinelConnection(getJedis(sentinel));
	}

	protected Jedis getJedis(RedisNode node) {

		Jedis jedis = new Jedis(node.getHost(), node.getPort());

		if (StringUtils.hasText(clientName)) {
			jedis.clientSetname(clientName);
		}

		return jedis;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisServerCommands#migrate(byte[], org.springframework.data.redis.connection.RedisNode, int, org.springframework.data.redis.connection.RedisServerCommands.MigrateOption)
	 */
	@Override
	public void migrate(byte[] key, RedisNode target, int dbIndex, MigrateOption option) {
		migrate(key, target, dbIndex, option, Long.MAX_VALUE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisServerCommands#migrate(byte[], org.springframework.data.redis.connection.RedisNode, int, org.springframework.data.redis.connection.RedisServerCommands.MigrateOption, long)
	 */
	@Override
	public void migrate(byte[] key, RedisNode target, int dbIndex, MigrateOption option, long timeout) {

		final int timeoutToUse = timeout <= Integer.MAX_VALUE ? (int) timeout : Integer.MAX_VALUE;

		try {
			if (isPipelined()) {

				pipeline(new JedisResult(
						pipeline.migrate(JedisConverters.toBytes(target.getHost()), target.getPort(), key, dbIndex, timeoutToUse)));
				return;
			}
			if (isQueueing()) {
				transaction(new JedisResult(transaction.migrate(JedisConverters.toBytes(target.getHost()), target.getPort(),
						key, dbIndex, timeoutToUse)));
				return;
			}
			jedis.migrate(JedisConverters.toBytes(target.getHost()), target.getPort(), key, dbIndex, timeoutToUse);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}

	}

}
