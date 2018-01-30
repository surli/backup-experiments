package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.TrieNode;
import com.google.common.base.Optional;
import org.junit.Test;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.org.lidalia.slf4jtest.LoggingEvent.debug;

/**
 * Test class for {@link BucketTrie}.
 */
public class BucketTrieTest {

	private final TestLogger logger = TestLoggerFactory.getTestLogger(BucketTrie.class);

	/**
	 * Tests that the constructor throws an exception when the given prefix is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullPrefix() {
		new BucketTrie(null);
	}

	/**
	 * Tests that the constructor throws an exception when the given prefix is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyPrefix() {
		new BucketTrie(" ");
	}

	/**
	 * Tests that {@link BucketTrie#insert(String, String)} throws an exception when the given key is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testInsert_NullKey() {
		new BucketTrie().insert(null, "");
	}

	/**
	 * Tests that {@link BucketTrie#insert(String, String)} throws an exception when the given key is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInsert_EmptyKey() {
		new BucketTrie().insert(" ", "");
	}

	/**
	 * Tests {@link BucketTrie#insert(String, String)} when the given key does not match the prefix.
	 */
	@Test
	public void testInsert_KeyDoesNotMatchPrefix() {
		final String prefix = "prefix";
		final String key = "key";
		final Trie<String, String> trie = new BucketTrie(prefix);
		trie.insert(key, "value");

		assertTrue(trie.isEmpty());
		assertThat(logger.getLoggingEvents(), is(singletonList(debug("Given key [{}] does not begin with prefix [{}]", key, prefix))));
	}

	/**
	 * Tests {@link BucketTrie#insert(String, String)} when the given key matches the prefix.
	 */
	@Test
	public void testInsert_KeyMatchesPrefix() {
		final String prefix = "prefix";
		final String shortKey = "key";
		final String longKey = prefix + "/" + shortKey;
		final String value = "value";
		final TrieNode<String> expected = new FileTrieNode(value);

		final Trie<String, String> trie = new BucketTrie(prefix);
		trie.insert(longKey, value);

		assertFalse(trie.isEmpty());
		assertEquals(1, trie.getRoot().getChildren().values().size());
		assertEquals(expected, trie.getRoot().getChildren().get(shortKey));
	}

	/**
	 * Tests {@link BucketTrie#insert(String, String)} when there is no prefix.
	 */
	@Test
	public void testInsert_NoPrefix() {
		final String key = "key";
		final String value = "value";
		final TrieNode<String> expected = new FileTrieNode(value);

		final Trie<String, String> trie = new BucketTrie();
		trie.insert(key, value);

		assertFalse(trie.isEmpty());
		assertEquals(1, trie.getRoot().getChildren().values().size());
		assertEquals(expected, trie.getRoot().getChildren().get(key));
	}

	/**
	 * Tests {@link BucketTrie#insert(String, String)} with a folder and a file.
	 */
	@Test
	public void testInsert_FolderAndFile() {
		final String filename = "file.tmp";
		final String key = "folder/" + filename;
		final String value = "http://www.example.com/file.tmp";
		final TrieNode<String> expectedFileNode = new FileTrieNode(value);
		final TrieNode<String> expectedDirectoryNode = new DirectoryTrieNode();
		expectedDirectoryNode.getChildren().put(filename, expectedFileNode);

		final Trie<String, String> trie = new BucketTrie();
		trie.insert(key, value);

		assertFalse(trie.isEmpty());
		assertEquals(1, trie.getRoot().getChildren().values().size());
		final TrieNode<String> actualDirectoryNode = trie.getRoot().getChildren().values().iterator().next();
		assertEquals(expectedDirectoryNode, actualDirectoryNode);
		assertEquals(1, actualDirectoryNode.getChildren().values().size());
		final TrieNode<String> actualFileNode = actualDirectoryNode.getChildren().values().iterator().next();
		assertEquals(expectedFileNode, actualFileNode);
		assertTrue(actualFileNode.getChildren().isEmpty());

	}

	/**
	 * Tests {@link BucketTrie#getRoot()}.
	 */
	@Test
	public void testGetRoot() {
		final TrieNode<String> expectedRoot = new DirectoryTrieNode();
		assertEquals(expectedRoot, new BucketTrie().getRoot());
	}

	/**
	 * Tests {@link BucketTrie#getPrefix()} when there is no prefix.
	 */
	@Test
	public void testGetPrefix_NullPrefix() {
		assertEquals(Optional.absent(), new BucketTrie().getPrefix());
	}

	/**
	 * Tests {@link BucketTrie#getPrefix()}.
	 */
	@Test
	public void testGetPrefix() {
		final String prefix = "prefix";
		assertEquals(Optional.of(prefix), new BucketTrie(prefix).getPrefix());
	}

	/**
	 * Tests {@link BucketTrie#isEmpty()}.
	 */
	@Test
	public void testIsEmpty() {
		final Trie<String, String> trie = new BucketTrie();
		assertTrue(trie.isEmpty());
		trie.insert("key", "value");
		assertFalse(trie.isEmpty());
	}

}
