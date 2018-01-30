package com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.head;

import com.amazonaws.services.s3.model.HeadBucketRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link HeadBucketRequestFactory}.
 */
public class HeadBucketRequestFactoryTest {

	/**
	 * Tests that the constructor throws an exception when the given bucket name is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullBucketName() {
		new HeadBucketRequestFactory(null);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyBucketName() {
		new HeadBucketRequestFactory(" ");
	}

	/**
	 * Tests {@link HeadBucketRequestFactory#create()}.
	 */
	@Test
	public void testCreate() {
		final String bucketName = "mockBucket";
		final HeadBucketRequest request = new HeadBucketRequestFactory(bucketName).create();
		assertEquals(bucketName, request.getBucketName());
	}

}
