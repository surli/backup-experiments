/*
 * Copyright 2011-2013 the original author or authors.
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
package org.springframework.data.redis.core;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * List operations bound to a certain key.
 * 
 * @author Costin Leau
 */
public interface BoundListOperations<K, V> extends BoundKeyOperations<K> {

	RedisOperations<K, V> getOperations();

	List<V> range(long start, long end);

	void trim(long start, long end);

	Long size();

	Long leftPush(V value);

	Long leftPushAll(V... values);

	Long leftPushIfPresent(V value);

	Long leftPush(V pivot, V value);

	Long rightPush(V value);

	Long rightPushAll(V... values);

	Long rightPushIfPresent(V value);

	Long rightPush(V pivot, V value);

	V leftPop();

	V leftPop(long timeout, TimeUnit unit);

	V rightPop();

	V rightPop(long timeout, TimeUnit unit);

	Long remove(long i, Object value);

	V index(long index);

	void set(long index, V value);
}
