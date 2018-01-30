package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.TrieNode;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link FileTrieNode}.
 */
public class FileTrieNodeTest {

	/**
	 * Tests that the constructor throws an exception when the given value is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullValue() {
		new FileTrieNode(null);
	}

	/**
	 * Tests that the constructor throws an exception when the given value is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyValue() {
		new FileTrieNode(" ");
	}

	/**
	 * Tests {@link FileTrieNode#getValue()}.
	 */
	@Test
	public void testGetValue() {
		final String value = "value";
		final TrieNode<String> node = new FileTrieNode(value);
		assertTrue(node.getValue().isPresent());
		assertEquals(value, node.getValue().get());
	}

	/**
	 * Tests {@link FileTrieNode#getChildren()}.
	 */
	@Test
	public void testGetChildren() {
		assertEquals(Collections.emptyMap(), new FileTrieNode("value").getChildren());
	}

}
