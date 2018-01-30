package com.avojak.mojo.aws.p2.maven.plugin.s3.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link BucketPath}.
 */
public class BucketPathTest {

	/**
	 * Tests {@link BucketPath#BucketPath()}.
	 */
	@Test
	public void testNoArgsConstructor() {
		assertEquals("", new BucketPath().asString());
	}

	/**
	 * Tests that {@link BucketPath#BucketPath(BucketPath)} throws an exception when the given {@link BucketPath} is
	 * {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testDeepCopyConstructor_NullPath() {
		new BucketPath(null);
	}

	/**
	 * Tests {@link BucketPath#BucketPath(BucketPath)}.
	 */
	@Test
	public void testDeepCopyConstructor() {
		final BucketPath path = new BucketPath().append("mock");
		assertEquals(path, new BucketPath(path));
	}

	/**
	 * Tests that {@link BucketPath#append(String)} throws an exception when the given path is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testAppend_NullPath() {
		new BucketPath().append(null);
	}

	/**
	 * Tests that {@link BucketPath#append(String)} throws an exception when the given path is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testAppend_EmptyPath() {
		new BucketPath().append(" ");
	}

	/**
	 * Tests that {@link BucketPath#append(String)} replaces '\' with '/'.
	 */
	@Test
	public void testAppend_ReplaceDeliminators() {
		final String path = "directory\\file";
		final String expectedPath = "directory/file";
		assertEquals(expectedPath, new BucketPath().append(path).asString());
	}

	/**
	 * Tests that {@link BucketPath#append(String)} trims deliminators at the start and end of the path.
	 */
	@Test
	public void testAppend_TrimPrefixAndSuffix() {
		final String path = "\\directory\\file\\";
		final String expectedPath = "directory/file";
		assertEquals(expectedPath, new BucketPath().append(path).asString());
	}

	/**
	 * Tests that {@link BucketPath#append(String)} adds a deliminator when one doesn't already exist.
	 */
	@Test
	public void test_AddDeliminator() {
		final String directory = "directory";
		final String file = "file";
		final String expectedPath = "directory/file";
		assertEquals(expectedPath, new BucketPath().append(directory).append(file).asString());
	}

	/**
	 * Tests {@link BucketPath#asString()}.
	 */
	@Test
	public void testAsString() {
		assertEquals("mock", new BucketPath().append("mock").asString());
	}

}
