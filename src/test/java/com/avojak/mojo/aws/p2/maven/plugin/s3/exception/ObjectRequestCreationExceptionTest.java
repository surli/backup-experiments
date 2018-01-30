package com.avojak.mojo.aws.p2.maven.plugin.s3.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test class for {@link ObjectRequestCreationException}.
 */
public class ObjectRequestCreationExceptionTest {

	/**
	 * Tests {@link ObjectRequestCreationException#ObjectRequestCreationException()}
	 */
	@Test
	public void testNoArgsConstructor() {
		final ObjectRequestCreationException exception = new ObjectRequestCreationException();
		assertNull(exception.getMessage());
	}

	/**
	 * Tests {@link ObjectRequestCreationException#ObjectRequestCreationException(Throwable)}.
	 */
	@Test
	public void testThrowableConstructor() {
		final Throwable throwable = new Throwable();
		final ObjectRequestCreationException exception = new ObjectRequestCreationException(throwable);
		assertEquals(throwable, exception.getCause());
	}

}
