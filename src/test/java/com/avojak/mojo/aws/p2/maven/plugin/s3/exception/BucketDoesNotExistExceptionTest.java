package com.avojak.mojo.aws.p2.maven.plugin.s3.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test class for {@link BucketDoesNotExistException}.
 */
public class BucketDoesNotExistExceptionTest {

	/**
	 * Tests {@link BucketDoesNotExistException#BucketDoesNotExistException()}.
	 */
	@Test
	public void testNoArgsConstructor() {
		final BucketDoesNotExistException exception = new BucketDoesNotExistException();
		assertNull(exception.getMessage());
	}

	/**
	 * Tests {@link BucketDoesNotExistException#BucketDoesNotExistException(String)}.
	 */
	@Test
	public void testBucketNameConstructor() {
		final BucketDoesNotExistException exception = new BucketDoesNotExistException("mock");
		assertEquals("Bucket [mock] does not exist", exception.getMessage());
	}

}
