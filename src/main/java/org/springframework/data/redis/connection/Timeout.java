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

import java.util.concurrent.TimeUnit;

import org.springframework.util.Assert;

/**
 * Timeout object representing an immutable timeout value with its {@link TimeUnit}.
 * <p>
 * {@link Timeout} is created with the {@link #create(long, TimeUnit)} factory method.
 *
 * @author Mark Paluch
 * @since 2.0
 */
public class Timeout {

	private final long timeout;
	private final TimeUnit timeUnit;

	private Timeout(long timeout, TimeUnit timeUnit) {

		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}

	/**
	 * Create a {@link Timeout} given {@code timeout} and {@link TimeUnit}.
	 *
	 * @param timeout must be greater or equal to zero.
	 * @param timeUnit must not be {@literal null}.
	 * @return a {@link Timeout} object containing {@code timeout} and {@link TimeUnit}.
	 */
	public static Timeout create(long timeout, TimeUnit timeUnit) {

		Assert.isTrue(timeout >= 0, "Timeout must be greater or equal to zero!");
		Assert.notNull(timeUnit, "TimeUnit must not be null!");

		return new Timeout(timeout, timeUnit);
	}

	/**
	 * @return the timeout value.
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * @return the {@link TimeUnit}.
	 */
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Retrieve the timeout value as {@link TimeUnit converted} value.
	 *
	 * @param destinationTimeUnit the target time unit, must not be {@literal null}.
	 * @return the timeout value as {@link TimeUnit converted} value.
	 */
	public long getTimeout(TimeUnit destinationTimeUnit) {

		Assert.notNull(destinationTimeUnit, "Destination TimeUnit must not be null!");

		return destinationTimeUnit.convert(timeout, timeUnit);
	}
}
