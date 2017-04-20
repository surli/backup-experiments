/*
 * Copyright 2011-2014 the original author or authors.
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis operations for simple (or in Redis terminology 'string') values.
 * 
 * @author Costin Leau
 * @author Christoph Strobl
 */
public interface ValueOperations<K, V> {

	void set(K key, V value);

	/**
	 * Set {@code key} to hold the string {@code value} until {@code timeout}.
	 * 
	 * @param key
	 * @param value
	 * @param timeout
	 * @param units
	 * @see http://redis.io/commands/set
	 */
	void set(K key, V value, long timeout, TimeUnit unit);

	Boolean setIfAbsent(K key, V value);

	void multiSet(Map<? extends K, ? extends V> m);

	Boolean multiSetIfAbsent(Map<? extends K, ? extends V> m);

	V get(Object key);

	V getAndSet(K key, V value);

	List<V> multiGet(Collection<K> keys);

	Long increment(K key, long delta);

	Double increment(K key, double delta);

	Integer append(K key, String value);

	String get(K key, long start, long end);

	void set(K key, V value, long offset);

	Long size(K key);

	RedisOperations<K, V> getOperations();
	
	/**
	 * @since 1.5
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 */
	Boolean setBit(K key, long offset, boolean value);
	
	/**
	 * @since 1.5
	 * @param key
	 * @param offset
	 * @return
	 */
	Boolean getBit(K key, long offset);
	
}
