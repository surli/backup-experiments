package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.TrieNode;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link TrieNode} for file objects.
 */
public class FileTrieNode implements TrieNode<String> {

	private final String value;

	public FileTrieNode(final String value) {
		this.value = checkNotNull(value, "value cannot be null");
		checkArgument(!value.trim().isEmpty(), "value cannot be empty");
	}

	@Override
	public Optional<String> getValue() {
		return Optional.of(value);
	}

	@Override
	public Map<String, TrieNode<String>> getChildren() {
		return Collections.emptyMap();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		FileTrieNode that = (FileTrieNode) o;

		return value != null ? value.equals(that.value) : that.value == null;
	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "FileTrieNode{" +
				"value='" + value + '\'' +
				'}';
	}

}
