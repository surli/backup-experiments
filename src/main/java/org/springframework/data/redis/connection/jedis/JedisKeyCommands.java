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

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.convert.Converters;
import org.springframework.data.redis.connection.jedis.JedisConnection.JedisResult;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.util.Assert;
import redis.clients.jedis.SortingParams;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public class JedisKeyCommands implements RedisKeyCommands {

	private final JedisConnection connection;

	public JedisKeyCommands(JedisConnection connection) {
		this.connection = connection;
	}

	private boolean isPipelined() {
		return connection.isPipelined();
	}

	private void pipeline(JedisResult result) {
		connection.pipeline(result);
	}

	private boolean isQueueing() {
		return connection.isQueueing();
	}

	private void transaction(JedisResult result) {
		connection.transaction(result);
	}

	@Override
	public Boolean exists(byte[] key) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().exists(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().exists(key)));
				return null;
			}
			return connection.getJedis().exists(key);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Long del(byte[]... keys) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().del(keys)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().del(keys)));
				return null;
			}
			return connection.getJedis().del(keys);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}

	}

	@Override
	public DataType type(byte[] key) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().type(key), JedisConverters.stringToDataType()));
				return null;
			}
			if (isQueueing()) {
				transaction(
						connection.newJedisResult(connection.getTransaction().type(key), JedisConverters.stringToDataType()));
				return null;
			}
			return JedisConverters.toDataType(connection.getJedis().type(key));
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Set<byte[]> keys(byte[] pattern) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().keys(pattern)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().keys(pattern)));
				return null;
			}
			return connection.getJedis().keys(pattern);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}

	}

	@Override
	public Cursor<byte[]> scan(ScanOptions options) {
		return null;
	}

	@Override
	public byte[] randomKey() {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().randomKeyBinary()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().randomKeyBinary()));
				return null;
			}
			return connection.getJedis().randomBinaryKey();
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public void rename(byte[] oldName, byte[] newName) {

		try {
			if (isPipelined()) {
				pipeline(connection.newStatusResult(connection.getPipeline().rename(oldName, newName)));
				return;
			}
			if (isQueueing()) {
				transaction(connection.newStatusResult(connection.getTransaction().rename(oldName, newName)));
				return;
			}
			connection.getJedis().rename(oldName, newName);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Boolean renameNX(byte[] oldName, byte[] newName) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().renamenx(oldName, newName), JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().renamenx(oldName, newName), JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(connection.getJedis().renamenx(oldName, newName));
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Boolean expire(byte[] key, long seconds) {
		return null;
	}

	@Override
	public Boolean pExpire(byte[] key, long millis) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().pexpire(key, millis), JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().pexpire(key, millis), JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(connection.getJedis().pexpire(key, millis));
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Boolean expireAt(byte[] key, long unixTime) {
		return null;
	}

	@Override
	public Boolean pExpireAt(byte[] key, long unixTimeInMillis) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().pexpireAt(key, unixTimeInMillis), JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().pexpireAt(key, unixTimeInMillis), JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(connection.getJedis().pexpireAt(key, unixTimeInMillis));
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Boolean persist(byte[] key) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().persist(key), JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().persist(key), JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(connection.getJedis().persist(key));
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}

	}

	@Override
	public Boolean move(byte[] key, int dbIndex) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().move(key, dbIndex), JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().move(key, dbIndex), JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(connection.getJedis().move(key, dbIndex));
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Long ttl(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().ttl(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().ttl(key)));
				return null;
			}

			return connection.getJedis().ttl(key);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Long ttl(byte[] key, TimeUnit timeUnit) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().ttl(key), Converters.secondsToTimeUnit(timeUnit)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().ttl(key), Converters.secondsToTimeUnit(timeUnit)));
				return null;
			}

			return Converters.secondsToTimeUnit(connection.getJedis().ttl(key), timeUnit);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Long pTtl(byte[] key) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().pttl(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().pttl(key)));
				return null;
			}

			return connection.getJedis().pttl(key);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}

	}

	@Override
	public Long pTtl(byte[] key, TimeUnit timeUnit) {

		Assert.notNull(key, "Key must not be null!");

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().pttl(key), Converters.millisecondsToTimeUnit(timeUnit)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().pttl(key), Converters.millisecondsToTimeUnit(timeUnit)));
				return null;
			}

			return Converters.millisecondsToTimeUnit(connection.getJedis().pttl(key), timeUnit);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public List<byte[]> sort(byte[] key, SortParameters params) {

		SortingParams sortParams = JedisConverters.toSortingParams(params);

		try {
			if (isPipelined()) {
				if (sortParams != null) {
					pipeline(connection.newJedisResult(connection.getPipeline().sort(key, sortParams)));
				} else {
					pipeline(connection.newJedisResult(connection.getPipeline().sort(key)));
				}

				return null;
			}
			if (isQueueing()) {
				if (sortParams != null) {
					transaction(connection.newJedisResult(connection.getTransaction().sort(key, sortParams)));
				} else {
					transaction(connection.newJedisResult(connection.getTransaction().sort(key)));
				}

				return null;
			}
			return (sortParams != null ? connection.getJedis().sort(key, sortParams) : connection.getJedis().sort(key));
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public Long sort(byte[] key, SortParameters params, byte[] storeKey) {

		SortingParams sortParams = JedisConverters.toSortingParams(params);

		try {
			if (isPipelined()) {
				if (sortParams != null) {
					pipeline(connection.newJedisResult(connection.getPipeline().sort(key, sortParams, storeKey)));
				} else {
					pipeline(connection.newJedisResult(connection.getPipeline().sort(key, storeKey)));
				}

				return null;
			}
			if (isQueueing()) {
				if (sortParams != null) {
					transaction(connection.newJedisResult(connection.getTransaction().sort(key, sortParams, storeKey)));
				} else {
					transaction(connection.newJedisResult(connection.getTransaction().sort(key, storeKey)));
				}

				return null;
			}
			return (sortParams != null ? connection.getJedis().sort(key, sortParams, storeKey) : connection.getJedis().sort(key, storeKey));
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public byte[] dump(byte[] key) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().dump(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().dump(key)));
				return null;
			}
			return connection.getJedis().dump(key);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}
	}

	@Override
	public void restore(byte[] key, long ttlInMillis, byte[] serializedValue) {

		if (ttlInMillis > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("TtlInMillis must be less than Integer.MAX_VALUE for restore in Jedis.");
		}
		try {
			if (isPipelined()) {
				pipeline(connection.newStatusResult(connection.getPipeline().restore(key, (int) ttlInMillis, serializedValue)));
				return;
			}
			if (isQueueing()) {
				transaction(connection.newStatusResult(connection.getTransaction().restore(key, (int) ttlInMillis, serializedValue)));
				return;
			}
			connection.getJedis().restore(key, (int) ttlInMillis, serializedValue);
		} catch (Exception ex) {
			throw connection.convertJedisAccessException(ex);
		}

	}
}
