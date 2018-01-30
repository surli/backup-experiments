package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.TrieNode;
import com.google.common.base.Optional;

import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of {@link TrieNode} for directory objects.
 */
public class DirectoryTrieNode implements TrieNode<String> {

	private final Map<String, TrieNode<String>> children = new TreeMap<String, TrieNode<String>>();

	@Override
	public Optional<String> getValue() {
		return Optional.absent();
	}

	@Override
	public Map<String, TrieNode<String>> getChildren() {
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DirectoryTrieNode that = (DirectoryTrieNode) o;

		return children.equals(that.children);
	}

	@Override
	public int hashCode() {
		return children.hashCode();
	}

	@Override
	public String toString() {
		return "DirectoryTrieNode{" +
				"children=" + children +
				'}';
	}

}
