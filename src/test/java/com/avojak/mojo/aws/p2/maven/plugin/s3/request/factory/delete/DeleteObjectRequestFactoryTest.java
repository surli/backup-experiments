package com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.delete;

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for {@link DeleteObjectRequestFactory}.
 */
public class DeleteObjectRequestFactoryTest {

	private final String bucketName = "mock";

	private DeleteObjectRequestFactory factory;

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		factory = new DeleteObjectRequestFactory(bucketName);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullBucketName() {
		new DeleteObjectRequestFactory(null);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyBucketName() {
		new DeleteObjectRequestFactory(" ");
	}

	/**
	 * Tests that {@link DeleteObjectRequestFactory#create(String)} throws an exception when the given key is {@code
	 * null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreate_NullKey() {
		factory.create(null);
	}

	/**
	 * Tests that {@link DeleteObjectRequestFactory#create(String)} throws an exception when the given key is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testCreate_EmptyKey() {
		factory.create(" ");
	}

	/**
	 * Tests {@link DeleteObjectRequestFactory#create(String)}.
	 */
	@Test
	public void testCreate() {
		final String key = "key";
		final DeleteObjectRequest request = factory.create(key);

		assertNotNull(request);
		assertEquals(bucketName, request.getBucketName());
		assertEquals(key, request.getKey());
	}

}
