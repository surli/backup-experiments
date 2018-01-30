package com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.list;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link ListObjectsRequestFactory}.
 */
public class ListObjectsRequestFactoryTest {

	/**
	 * Tests that the constructor throws an exception when the given bucket name is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullBucketName() {
		new ListObjectsRequestFactory(null);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyBucketName() {
		new ListObjectsRequestFactory(" ");
	}

	/**
	 * Tests {@link ListObjectsRequestFactory#create(String)}.
	 */
	@Test
	public void testCreate() {
		final String bucketName = "mockBucket";
		final String prefix = "prefix";
		final ListObjectsRequest request = new ListObjectsRequestFactory(bucketName).create(prefix);
		assertEquals(bucketName, request.getBucketName());
		assertEquals(prefix, request.getPrefix());
	}

}
