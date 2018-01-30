package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.google.common.base.Optional;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link DirectoryTrieNode}.
 */
public class DirectoryTrieNodeTest {

	/**
	 * Tests {@link DirectoryTrieNode#getValue()}.
	 */
	@Test
	public void testGetValue() {
		assertEquals(Optional.absent(), new DirectoryTrieNode().getValue());
	}

	/**
	 * Tests {@link DirectoryTrieNode#getChildren()}.
	 */
	@Test
	public void testGetChildren() {
		final DirectoryTrieNode node = new DirectoryTrieNode();
		assertEquals(Collections.emptyMap(), node.getChildren());
	}

}
