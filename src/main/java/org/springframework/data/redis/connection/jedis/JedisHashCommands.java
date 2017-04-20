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

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.jedis.JedisConnection.JedisResult;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.KeyBoundCursor;
import org.springframework.data.redis.core.ScanIteration;
import org.springframework.data.redis.core.ScanOptions;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public class JedisHashCommands implements RedisHashCommands {

	private final JedisConnection connection;

	public JedisHashCommands(JedisConnection connection) {
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

	private RuntimeException convertJedisAccessException(Exception ex) {
		return connection.convertJedisAccessException(ex);
	}

	public Boolean hSet(byte[] key, byte[] field, byte[] value) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hset(key, field, value),
						JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hset(key, field, value),
						JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(connection.getJedis().hset(key, field, value));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Boolean hSetNX(byte[] key, byte[] field, byte[] value) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hsetnx(key, field, value),
						JedisConverters.longToBoolean()));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hsetnx(key, field, value),
						JedisConverters.longToBoolean()));
				return null;
			}
			return JedisConverters.toBoolean(connection.getJedis().hsetnx(key, field, value));
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Long hDel(byte[] key, byte[]... fields) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hdel(key, fields)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hdel(key, fields)));
				return null;
			}
			return connection.getJedis().hdel(key, fields);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Boolean hExists(byte[] key, byte[] field) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hexists(key, field)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hexists(key, field)));
				return null;
			}
			return connection.getJedis().hexists(key, field);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public byte[] hGet(byte[] key, byte[] field) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hget(key, field)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hget(key, field)));
				return null;
			}
			return connection.getJedis().hget(key, field);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Map<byte[], byte[]> hGetAll(byte[] key) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hgetAll(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hgetAll(key)));
				return null;
			}
			return connection.getJedis().hgetAll(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Long hIncrBy(byte[] key, byte[] field, long delta) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hincrBy(key, field, delta)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hincrBy(key, field, delta)));
				return null;
			}
			return connection.getJedis().hincrBy(key, field, delta);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Double hIncrBy(byte[] key, byte[] field, double delta) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hincrByFloat(key, field, delta)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hincrByFloat(key, field, delta)));
				return null;
			}
			return connection.getJedis().hincrByFloat(key, field, delta);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Set<byte[]> hKeys(byte[] key) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hkeys(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hkeys(key)));
				return null;
			}
			return connection.getJedis().hkeys(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public Long hLen(byte[] key) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hlen(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hlen(key)));
				return null;
			}
			return connection.getJedis().hlen(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public List<byte[]> hMGet(byte[] key, byte[]... fields) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hmget(key, fields)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hmget(key, fields)));
				return null;
			}
			return connection.getJedis().hmget(key, fields);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public void hMSet(byte[] key, Map<byte[], byte[]> tuple) {

		try {
			if (isPipelined()) {
				pipeline(connection.newStatusResult(connection.getPipeline().hmset(key, tuple)));
				return;
			}
			if (isQueueing()) {
				transaction(connection.newStatusResult(connection.getTransaction().hmset(key, tuple)));
				return;
			}
			connection.getJedis().hmset(key, tuple);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
	}

	public List<byte[]> hVals(byte[] key) {

		try {
			if (isPipelined()) {
				pipeline(connection.newJedisResult(connection.getPipeline().hvals(key)));
				return null;
			}
			if (isQueueing()) {
				transaction(connection.newJedisResult(connection.getTransaction().hvals(key)));
				return null;
			}
			return connection.getJedis().hvals(key);
		} catch (Exception ex) {
			throw convertJedisAccessException(ex);
		}
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

		return new KeyBoundCursor<Entry<byte[], byte[]>>(key, cursorId, options) {

			@Override
			protected ScanIteration<Entry<byte[], byte[]>> doScan(byte[] key, long cursorId, ScanOptions options) {

				if (isQueueing() || isPipelined()) {
					throw new UnsupportedOperationException("'HSCAN' cannot be called in pipeline / transaction mode.");
				}

				ScanParams params = JedisConverters.toScanParams(options);

				ScanResult<Entry<byte[], byte[]>> result = connection.getJedis().hscan(key, JedisConverters.toBytes(cursorId),
						params);
				return new ScanIteration<>(Long.valueOf(result.getStringCursor()), result.getResult());
			}

			protected void doClose() {
				JedisHashCommands.this.connection.close();
			};

		}.open();
	}
}
