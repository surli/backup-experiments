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

import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.ClusterSlotHashUtil;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection.JedisClusterCommandCallback;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection.JedisMultiKeyClusterCommandCallback;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public class JedisClusterStringCommands implements RedisStringCommands {

	private final JedisClusterConnection connection;

	public JedisClusterStringCommands(JedisClusterConnection jedisClusterConnection) {
		this.connection = jedisClusterConnection;
	}

	protected DataAccessException convertJedisAccessException(Exception ex) {
		return connection.convertJedisAccessException(ex);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#get(byte[])
	 */
	@Override
	public byte[] get(byte[] key) {

		try {
			return connection.getCluster().get(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#getSet(byte[], byte[])
	 */
	@Override
	public byte[] getSet(byte[] key, byte[] value) {

		try {
			return connection.getCluster().getSet(key, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#mGet(byte[][])
	 */
	@Override
	public List<byte[]> mGet(byte[]... keys) {

		Assert.noNullElements(keys, "Keys must not contain null elements!");

		if (ClusterSlotHashUtil.isSameSlotForAllKeys(keys)) {
			return connection.getCluster().mget(keys);
		}

		return connection.getClusterCommandExecutor()
				.executeMuliKeyCommand(new JedisMultiKeyClusterCommandCallback<byte[]>() {

					@Override
					public byte[] doInCluster(Jedis client, byte[] key) {
						return client.get(key);
					}
				}, Arrays.asList(keys)).resultsAsListSortBy(keys);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#set(byte[], byte[])
	 */
	@Override
	public void set(byte[] key, byte[] value) {

		try {
			connection.getCluster().set(key, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#set(byte[], byte[], org.springframework.data.redis.core.types.Expiration, org.springframework.data.redis.connection.RedisStringCommands.SetOptions)
	 */
	@Override
	public void set(byte[] key, byte[] value, Expiration expiration, SetOption option) {

		if (expiration == null || expiration.isPersistent()) {

			if (option == null || ObjectUtils.nullSafeEquals(SetOption.UPSERT, option)) {
				set(key, value);
			} else {

				// BinaryCluster does not support set with nxxx and binary key/value pairs.
				if (ObjectUtils.nullSafeEquals(SetOption.SET_IF_PRESENT, option)) {
					throw new UnsupportedOperationException("Jedis does not support SET XX without PX or EX on BinaryCluster.");
				}

				setNX(key, value);
			}
		} else {

			if (option == null || ObjectUtils.nullSafeEquals(SetOption.UPSERT, option)) {

				if (ObjectUtils.nullSafeEquals(TimeUnit.MILLISECONDS, expiration.getTimeUnit())) {
					pSetEx(key, expiration.getExpirationTime(), value);
				} else {
					setEx(key, expiration.getExpirationTime(), value);
				}
			} else {

				byte[] nxxx = JedisConverters.toSetCommandNxXxArgument(option);
				byte[] expx = JedisConverters.toSetCommandExPxArgument(expiration);

				try {
					connection.getCluster().set(key, value, nxxx, expx, expiration.getExpirationTime());
				} catch (Exception ex) {
					throw convertJedisAccessException(ex);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#setNX(byte[], byte[])
	 */
	@Override
	public Boolean setNX(byte[] key, byte[] value) {

		try {
			return JedisConverters.toBoolean(connection.getCluster().setnx(key, value));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#setEx(byte[], long, byte[])
	 */
	@Override
	public void setEx(byte[] key, long seconds, byte[] value) {

		if (seconds > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Seconds have cannot exceed Integer.MAX_VALUE!");
		}

		try {
			connection.getCluster().setex(key, Long.valueOf(seconds).intValue(), value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#pSetEx(byte[], long, byte[])
	 */
	@Override
	public void pSetEx(final byte[] key, final long milliseconds, final byte[] value) {

		if (milliseconds > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Milliseconds have cannot exceed Integer.MAX_VALUE!");
		}

		connection.getClusterCommandExecutor().executeCommandOnSingleNode(new JedisClusterCommandCallback<String>() {

			@Override
			public String doInCluster(Jedis client) {
				return client.psetex(key, milliseconds, value);
			}
		}, connection.getTopologyProvider().getTopology().getKeyServingMasterNode(key));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#mSet(java.util.Map)
	 */
	@Override
	public void mSet(Map<byte[], byte[]> tuples) {

		Assert.notNull(tuples, "Tuples must not be null!");

		if (ClusterSlotHashUtil.isSameSlotForAllKeys(tuples.keySet().toArray(new byte[tuples.keySet().size()][]))) {
			try {
				connection.getCluster().mset(JedisConverters.toByteArrays(tuples));
				return;
			} catch (Exception ex) {
				throw convertJedisAccessException(ex);
			}
		}

		for (Map.Entry<byte[], byte[]> entry : tuples.entrySet()) {
			set(entry.getKey(), entry.getValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#mSetNX(java.util.Map)
	 */
	@Override
	public Boolean mSetNX(Map<byte[], byte[]> tuples) {

		Assert.notNull(tuples, "Tuple must not be null!");

		if (ClusterSlotHashUtil.isSameSlotForAllKeys(tuples.keySet().toArray(new byte[tuples.keySet().size()][]))) {
			try {
				return JedisConverters.toBoolean(connection.getCluster().msetnx(JedisConverters.toByteArrays(tuples)));
			} catch (Exception ex) {
				throw convertJedisAccessException(ex);
			}
		}

		boolean result = true;
		for (Map.Entry<byte[], byte[]> entry : tuples.entrySet()) {
			if (!setNX(entry.getKey(), entry.getValue()) && result) {
				result = false;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#incr(byte[])
	 */
	@Override
	public Long incr(byte[] key) {

		try {
			return connection.getCluster().incr(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#incrBy(byte[], long)
	 */
	@Override
	public Long incrBy(byte[] key, long value) {

		try {
			return connection.getCluster().incrBy(key, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#incrBy(byte[], double)
	 */
	@Override
	public Double incrBy(byte[] key, double value) {

		try {
			return connection.getCluster().incrByFloat(key, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#decr(byte[])
	 */
	@Override
	public Long decr(byte[] key) {

		try {
			return connection.getCluster().decr(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#decrBy(byte[], long)
	 */
	@Override
	public Long decrBy(byte[] key, long value) {

		try {
			return connection.getCluster().decrBy(key, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#append(byte[], byte[])
	 */
	@Override
	public Long append(byte[] key, byte[] value) {

		try {
			return connection.getCluster().append(key, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#getRange(byte[], long, long)
	 */
	@Override
	public byte[] getRange(byte[] key, long begin, long end) {

		try {
			return connection.getCluster().getrange(key, begin, end);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#setRange(byte[], byte[], long)
	 */
	@Override
	public void setRange(byte[] key, byte[] value, long offset) {

		try {
			connection.getCluster().setrange(key, offset, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	@Override
	public Boolean getBit(byte[] key, long offset) {

		try {
			return connection.getCluster().getbit(key, offset);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	@Override
	public Boolean setBit(byte[] key, long offset, boolean value) {

		try {
			return connection.getCluster().setbit(key, offset, value);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	@Override
	public Long bitCount(byte[] key) {

		try {
			return connection.getCluster().bitcount(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	@Override
	public Long bitCount(byte[] key, long begin, long end) {

		try {
			return connection.getCluster().bitcount(key, begin, end);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	@Override
	public Long bitOp(BitOperation op, byte[] destination, byte[]... keys) {

		byte[][] allKeys = ByteUtils.mergeArrays(destination, keys);

		if (ClusterSlotHashUtil.isSameSlotForAllKeys(allKeys)) {
			try {
				return connection.getCluster().bitop(JedisConverters.toBitOp(op), destination, keys);
			} catch (Exception ex) {
				throw convertJedisAccessException(ex);
			}
		}

		throw new InvalidDataAccessApiUsageException("BITOP is only supported for same slot keys in cluster mode.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.redis.connection.RedisStringCommands#strLen(byte[])
	 */
	@Override
	public Long strLen(byte[] key) {

		try {
			return connection.getCluster().strlen(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}
}
