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

/**
 * @author Mark Paluch
 */
public interface RedisClientConfigurator<T extends RedisClientConfigurator<T>> {

	T password(String password);

	T withSsl();

	interface RedisSentinelClientConfigurator<T extends RedisSentinelClientConfigurator<T>>
			extends RedisClientConfigurator<T>, RedisClientNameConfigurer<T> {

		T withMaster(String master);

		T withMaster(NamedNode master);

		T withSentinel(String host, int port);

		T withSentinel(RedisNode redisNode);

		T dbIndex(int dbIndex);
	}

	interface RedisStandaloneClientConfigurator<T extends RedisStandaloneClientConfigurator<T>>
			extends RedisClientConfigurator<T>, RedisClientNameConfigurer<T> {

		T dbIndex(int dbIndex);
	}

	interface RedisClientNameConfigurer<T extends RedisClientNameConfigurer<T>> {

		T clientName(String clientName);
	}
}
