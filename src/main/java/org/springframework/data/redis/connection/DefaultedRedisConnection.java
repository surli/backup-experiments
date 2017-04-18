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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.Expiration;

/**
 * @author Christoph Strobl
 * @since 2.0
 */
public interface DefaultedRedisConnection extends RedisConnection {

	// KEY COMMANDS

	default Boolean exists(byte[] key) {
		return keyCommands().exists(key);
	}

	default Long del(byte[]... keys) {
		return keyCommands().del(keys);
	}

	default DataType type(byte[] pattern) {
		return keyCommands().type(pattern);
	}

	default Set<byte[]> keys(byte[] pattern) {
		return keyCommands().keys(pattern);
	}

	default Cursor<byte[]> scan(ScanOptions options) {
		return keyCommands().scan(options);
	}

	default byte[] randomKey() {
		return keyCommands().randomKey();
	}

	default void rename(byte[] oldName, byte[] newName) {
		keyCommands().rename(oldName, newName);
	}

	default Boolean renameNX(byte[] oldName, byte[] newName) {
		return keyCommands().renameNX(oldName, newName);
	}

	default Boolean expire(byte[] key, long seconds) {
		return keyCommands().expire(key, seconds);
	}

	default Boolean persist(byte[] key) {
		return keyCommands().persist(key);
	}

	default Boolean move(byte[] key, int dbIndex) {
		return keyCommands().move(key, dbIndex);
	}

	default void restore(byte[] key, long ttlInMillis, byte[] serializedValue) {
		keyCommands().restore(key, ttlInMillis, serializedValue);
	}

	default Long pTtl(byte[] key) {
		return keyCommands().pTtl(key);
	}

	@Override
	default Long pTtl(byte[] key, TimeUnit timeUnit) {
		return keyCommands().pTtl(key, timeUnit);
	}

	@Override
	default Boolean pExpire(byte[] key, long millis) {
		return keyCommands().pExpire(key, millis);
	}

	@Override
	default Boolean pExpireAt(byte[] key, long unixTimeInMillis) {
		return keyCommands().pExpireAt(key, unixTimeInMillis);
	}

	@Override
	default Boolean expireAt(byte[] key, long unixTime) {
		return keyCommands().expireAt(key, unixTime);
	}

	@Override
	default Long ttl(byte[] key) {
		return keyCommands().ttl(key);
	}

	@Override
	default Long ttl(byte[] key, TimeUnit timeUnit) {
		return keyCommands().ttl(key, timeUnit);
	}

	@Override
	default byte[] dump(byte[] key) {
		return keyCommands().dump(key);
	}

	// STRING COMMANDS

	@Override
	default byte[] get(byte[] key) {
		return stringCommands().get(key);
	}

	@Override
	default byte[] getSet(byte[] key, byte[] value) {
		return stringCommands().getSet(key, value);
	}

	@Override
	default List<byte[]> mGet(byte[]... keys) {
		return stringCommands().mGet(keys);
	}

	@Override
	default void set(byte[] key, byte[] value) {
		stringCommands().set(key, value);
	}

	@Override
	default void set(byte[] key, byte[] value, Expiration expiration, SetOption option) {
		stringCommands().set(key, value, expiration, option);
	}

	@Override
	default Boolean setNX(byte[] key, byte[] value) {
		return stringCommands().setNX(key, value);
	}

	@Override
	default void setEx(byte[] key, long seconds, byte[] value) {
		stringCommands().setEx(key, seconds, value);
	}

	@Override
	default void pSetEx(byte[] key, long milliseconds, byte[] value) {
		stringCommands().pSetEx(key, milliseconds, value);
	}

	@Override
	default void mSet(Map<byte[], byte[]> tuple) {
		stringCommands().mSet(tuple);
	}

	@Override
	default Boolean mSetNX(Map<byte[], byte[]> tuple) {
		return stringCommands().mSetNX(tuple);
	}

	@Override
	default Long incr(byte[] key) {
		return stringCommands().incr(key);
	}

	@Override
	default Double incrBy(byte[] key, double value) {
		return stringCommands().incrBy(key, value);
	}

	@Override
	default Long incrBy(byte[] key, long value) {
		return stringCommands().incrBy(key, value);
	}

	@Override
	default Long decr(byte[] key) {
		return stringCommands().decr(key);
	}

	@Override
	default Long decrBy(byte[] key, long value) {
		return stringCommands().decrBy(key, value);
	}

	@Override
	default Long append(byte[] key, byte[] value) {
		return stringCommands().append(key, value);
	}

	@Override
	default byte[] getRange(byte[] key, long begin, long end) {
		return stringCommands().getRange(key, begin, end);
	}

	@Override
	default void setRange(byte[] key, byte[] value, long offset) {
		stringCommands().setRange(key, value, offset);
	}

	@Override
	default Boolean getBit(byte[] key, long offset) {
		return stringCommands().getBit(key, offset);
	}

	@Override
	default Boolean setBit(byte[] key, long offset, boolean value) {
		return stringCommands().setBit(key, offset, value);
	}

	@Override
	default Long bitCount(byte[] key) {
		return stringCommands().bitCount(key);
	}

	@Override
	default Long bitCount(byte[] key, long begin, long end) {
		return stringCommands().bitCount(key, begin, end);
	}

	@Override
	default Long bitOp(BitOperation op, byte[] destination, byte[]... keys) {
		return stringCommands().bitOp(op, destination, keys);
	}

	@Override
	default Long strLen(byte[] key) {
		return stringCommands().strLen(key);
	}

	// LIST COMMANDS

	@Override
	default Long rPush(byte[] key, byte[]... values) {
		return listCommands().rPush(key, values);
	}

	@Override
	default Long lPush(byte[] key, byte[]... values) {
		return listCommands().lPush(key, values);
	}

	@Override
	default Long rPushX(byte[] key, byte[] value) {
		return listCommands().rPushX(key, value);
	}

	@Override
	default Long lPushX(byte[] key, byte[] value) {
		return listCommands().lPushX(key, value);
	}

	@Override
	default Long lLen(byte[] key) {
		return listCommands().lLen(key);
	}

	@Override
	default List<byte[]> lRange(byte[] key, long start, long end) {
		return listCommands().lRange(key, start, end);
	}

	@Override
	default void lTrim(byte[] key, long start, long end) {
		listCommands().lTrim(key, start, end);
	}

	@Override
	default byte[] lIndex(byte[] key, long index) {
		return listCommands().lIndex(key, index);
	}

	@Override
	default Long lInsert(byte[] key, Position where, byte[] pivot, byte[] value) {
		return listCommands().lInsert(key, where, pivot, value);
	}

	@Override
	default void lSet(byte[] key, long index, byte[] value) {
		listCommands().lSet(key, index, value);
	}

	@Override
	default Long lRem(byte[] key, long count, byte[] value) {
		return listCommands().lRem(key, count, value);
	}

	@Override
	default byte[] lPop(byte[] key) {
		return listCommands().lPop(key);
	}

	@Override
	default byte[] rPop(byte[] key) {
		return listCommands().rPop(key);
	}

	@Override
	default List<byte[]> bLPop(int timeout, byte[]... keys) {
		return listCommands().bLPop(timeout, keys);
	}

	@Override
	default List<byte[]> bRPop(int timeout, byte[]... keys) {
		return listCommands().bRPop(timeout, keys);
	}

	@Override
	default byte[] rPopLPush(byte[] srcKey, byte[] dstKey) {
		return listCommands().rPopLPush(srcKey, dstKey);
	}

	@Override
	default byte[] bRPopLPush(int timeout, byte[] srcKey, byte[] dstKey) {
		return listCommands().bRPopLPush(timeout, srcKey, dstKey);
	}

	// SET COMMANDS

	@Override
	default Long sAdd(byte[] key, byte[]... values) {
		return setCommands().sAdd(key, values);
	}

	@Override
	default Long sCard(byte[] key) {
		return setCommands().sCard(key);
	}

	@Override
	default Set<byte[]> sDiff(byte[]... keys) {
		return setCommands().sDiff(keys);
	}

	@Override
	default Long sDiffStore(byte[] destKey, byte[]... keys) {
		return setCommands().sDiffStore(destKey, keys);
	}

	@Override
	default Set<byte[]> sInter(byte[]... keys) {
		return setCommands().sInter(keys);
	}

	@Override
	default Long sInterStore(byte[] destKey, byte[]... keys) {
		return setCommands().sInterStore(destKey, keys);
	}

	@Override
	default Boolean sIsMember(byte[] key, byte[] value) {
		return setCommands().sIsMember(key, value);
	}

	@Override
	default Set<byte[]> sMembers(byte[] key) {
		return setCommands().sMembers(key);
	}

	@Override
	default Boolean sMove(byte[] srcKey, byte[] destKey, byte[] value) {
		return setCommands().sMove(srcKey, destKey, value);
	}

	@Override
	default byte[] sPop(byte[] key) {
		return setCommands().sPop(key);
	}

	@Override
	default byte[] sRandMember(byte[] key) {
		return setCommands().sRandMember(key);
	}

	@Override
	default List<byte[]> sRandMember(byte[] key, long count) {
		return setCommands().sRandMember(key, count);
	}

	@Override
	default Long sRem(byte[] key, byte[]... values) {
		return setCommands().sRem(key, values);
	}

	@Override
	default Set<byte[]> sUnion(byte[]... keys) {
		return setCommands().sUnion(keys);
	}

	@Override
	default Long sUnionStore(byte[] destKey, byte[]... keys) {
		return setCommands().sUnionStore(destKey, keys);
	}

	@Override
	default Cursor<byte[]> sScan(byte[] key, ScanOptions options) {
		return setCommands().sScan(key, options);
	}

	// ZSET COMMANDS

	@Override
	default Boolean zAdd(byte[] key, double score, byte[] value) {
		return zSetCommands().zAdd(key, score, value);
	}

	@Override
	default Long zAdd(byte[] key, Set<Tuple> tuples) {
		return zSetCommands().zAdd(key, tuples);
	}

	@Override
	default Long zCard(byte[] key) {
		return zSetCommands().zCard(key);
	}

	@Override
	default Long zCount(byte[] key, double min, double max) {
		return zSetCommands().zCount(key, min, max);
	}

	@Override
	default Long zCount(byte[] key, Range range) {
		return zSetCommands().zCount(key, range);
	}

	@Override
	default Double zIncrBy(byte[] key, double increment, byte[] value) {
		return zSetCommands().zIncrBy(key, increment, value);
	}

	@Override
	default Long zInterStore(byte[] destKey, Aggregate aggregate, int[] weights, byte[]... sets) {
		return zSetCommands().zInterStore(destKey, aggregate, weights, sets);
	}

	@Override
	default Long zInterStore(byte[] destKey, byte[]... sets) {
		return zSetCommands().zInterStore(destKey, sets);
	}

	default Set<byte[]> zRange(byte[] key, long start, long end) {
		return zSetCommands().zRange(key, start, end);
	}

	@Override
	default Set<Tuple> zRangeWithScores(byte[] key, long start, long end) {
		return zSetCommands().zRangeWithScores(key, start, end);
	}

	default Set<byte[]> zRangeByLex(byte[] key, Range range, Limit limit) {
		return zSetCommands().zRangeByLex(key, range, limit);
	}

	@Override
	default Set<byte[]> zRangeByScore(byte[] key, Range range, Limit limit) {
		return zSetCommands().zRangeByScore(key, range, limit);
	}

	@Override
	default Set<Tuple> zRangeByScoreWithScores(byte[] key, Range range, Limit limit) {
		return zSetCommands().zRangeByScoreWithScores(key, range, limit);
	}

	@Override
	default Set<Tuple> zRevRangeWithScores(byte[] key, long start, long end) {
		return zSetCommands().zRevRangeWithScores(key, start, end);
	}

	@Override
	default Set<byte[]> zRevRangeByScore(byte[] key, Range range, Limit limit) {
		return zSetCommands().zRevRangeByScore(key, range, limit);
	}

	@Override
	default Set<Tuple> zRevRangeByScoreWithScores(byte[] key, Range range, Limit limit) {
		return zSetCommands().zRevRangeByScoreWithScores(key, range, limit);
	}

	@Override
	default Long zRank(byte[] key, byte[] value) {

		return zSetCommands().zRank(key, value);
	}

	@Override
	default Long zRem(byte[] key, byte[]... values) {
		return zSetCommands().zRem(key, values);
	}

	@Override
	default Long zRemRange(byte[] key, long start, long end) {
		return zSetCommands().zRemRange(key, start, end);
	}

	@Override
	default Long zRemRangeByScore(byte[] key, Range range) {
		return zSetCommands().zRemRangeByScore(key, range);
	}

	@Override
	default Long zRemRangeByScore(byte[] key, double min, double max) {
		return zSetCommands().zRemRangeByScore(key, min, max);
	}

	@Override
	default Set<byte[]> zRevRange(byte[] key, long start, long end) {
		return zSetCommands().zRevRange(key, start, end);
	}

	@Override
	default Long zRevRank(byte[] key, byte[] value) {
		return zSetCommands().zRevRank(key, value);
	}

	@Override
	default Double zScore(byte[] key, byte[] value) {
		return zSetCommands().zScore(key, value);
	}

	@Override
	default Long zUnionStore(byte[] destKey, Aggregate aggregate, int[] weights, byte[]... sets) {
		return zSetCommands().zUnionStore(destKey, aggregate, weights, sets);
	}

	@Override
	default Long zUnionStore(byte[] destKey, byte[]... sets) {
		return zSetCommands().zUnionStore(destKey, sets);
	}

	@Override
	default Cursor<Tuple> zScan(byte[] key, ScanOptions options) {
		return zSetCommands().zScan(key, options);
	}

	@Override
	default Set<byte[]> zRangeByScore(byte[] key, String min, String max) {
		return zSetCommands().zRangeByScore(key, min, max);
	}

	@Override
	default Set<byte[]> zRangeByScore(byte[] key, String min, String max, long offset, long count) {
		return zSetCommands().zRangeByScore(key, min, max, offset, count);
	}

	// HASH COMMANDS

	@Override
	default Boolean hSet(byte[] key, byte[] field, byte[] value) {
		return hashCommands().hSet(key, field, value);
	}

	@Override
	default Boolean hSetNX(byte[] key, byte[] field, byte[] value) {
		return hashCommands().hSetNX(key, field, value);
	}

	@Override
	default Long hDel(byte[] key, byte[]... fields) {
		return hashCommands().hDel(key, fields);
	}

	@Override
	default Boolean hExists(byte[] key, byte[] field) {
		return hashCommands().hExists(key, field);
	}

	@Override
	default byte[] hGet(byte[] key, byte[] field) {
		return hashCommands().hGet(key, field);
	}

	@Override
	default Map<byte[], byte[]> hGetAll(byte[] key) {
		return hashCommands().hGetAll(key);
	}

	@Override
	default Double hIncrBy(byte[] key, byte[] field, double delta) {
		return hashCommands().hIncrBy(key, field, delta);
	}

	@Override
	default Long hIncrBy(byte[] key, byte[] field, long delta) {
		return hashCommands().hIncrBy(key, field, delta);
	}

	@Override
	default Set<byte[]> hKeys(byte[] key) {
		return hashCommands().hKeys(key);
	}

	@Override
	default Long hLen(byte[] key) {
		return hashCommands().hLen(key);
	}

	@Override
	default List<byte[]> hMGet(byte[] key, byte[]... fields) {
		return hashCommands().hMGet(key, fields);
	}

	@Override
	default void hMSet(byte[] key, Map<byte[], byte[]> hashes) {
		hashCommands().hMSet(key, hashes);
	}

	@Override
	default List<byte[]> hVals(byte[] key) {
		return hashCommands().hVals(key);
	}

	@Override
	default Cursor<Entry<byte[], byte[]>> hScan(byte[] key, ScanOptions options) {
		return hashCommands().hScan(key, options);
	}

	// GEO COMMANDS

	default Long geoAdd(byte[] key, Point point, byte[] member) {
		return geoCommands().geoAdd(key, point, member);
	}

	default Long geoAdd(byte[] key, Map<byte[], Point> memberCoordinateMap) {
		return geoCommands().geoAdd(key, memberCoordinateMap);
	}

	default Long geoAdd(byte[] key, Iterable<GeoLocation<byte[]>> locations) {
		return geoCommands().geoAdd(key, locations);
	}

	default Distance geoDist(byte[] key, byte[] member1, byte[] member2) {
		return geoCommands().geoDist(key, member1, member2);
	}

	default Distance geoDist(byte[] key, byte[] member1, byte[] member2, Metric metric) {
		return geoCommands().geoDist(key, member1, member2, metric);
	}

	default List<String> geoHash(byte[] key, byte[]... members) {
		return geoCommands().geoHash(key, members);
	}

	default List<Point> geoPos(byte[] key, byte[]... members) {
		return geoCommands().geoPos(key, members);
	}

	default GeoResults<GeoLocation<byte[]>> geoRadius(byte[] key, Circle within) {
		return geoCommands().geoRadius(key, within);
	}

	default GeoResults<GeoLocation<byte[]>> geoRadius(byte[] key, Circle within, GeoRadiusCommandArgs args) {
		return geoCommands().geoRadius(key, within, args);
	}

	default GeoResults<GeoLocation<byte[]>> geoRadiusByMember(byte[] key, byte[] member, Distance radius) {
		return geoCommands().geoRadiusByMember(key, member, radius);
	}

	default GeoResults<GeoLocation<byte[]>> geoRadiusByMember(byte[] key, byte[] member, Distance radius,
			GeoRadiusCommandArgs args) {
		return geoCommands().geoRadiusByMember(key, member, radius, args);
	}

	default Long geoRemove(byte[] key, byte[]... members) {
		return geoCommands().geoRemove(key, members);
	}

	// HLL COMMANDS

	@Override
	default Long pfAdd(byte[] key, byte[]... values) {
		return hyperLogLogCommands().pfAdd(key, values);
	}

	@Override
	default Long pfCount(byte[]... keys) {
		return hyperLogLogCommands().pfCount(keys);
	}

	@Override
	default void pfMerge(byte[] destinationKey, byte[]... sourceKeys) {
		hyperLogLogCommands().pfMerge(destinationKey, sourceKeys);
	}
}
