package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie;

import com.google.common.base.Optional;

/**
 * Provides methods to model a Trie structure.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 */
public interface Trie<K extends String, V> {

	/**
	 * Inserts a key-value pair into the trie.
	 * <p>
	 * Inserts should be performed using the <em>full</em> key, which includes the prefix. This allows proper filtering
	 * of content.
	 *
	 * @param key   The key. Cannot be {@code null} or empty.
	 * @param value The value.
	 */
	void insert(final K key, final V value);

	/**
	 * Returns the root {@link TrieNode} of the trie.
	 *
	 * @return The non-{@code null} root {@link TrieNode}.
	 */
	TrieNode<V> getRoot();

	/**
	 * Returns the prefix for filtering content inserted into the trie.
	 *
	 * @return The non-{@code null} {@link Optional} prefix, if present. Otherwise {@link Optional#absent()}.
	 */
	Optional<String> getPrefix();

	/**
	 * Returns whether or not the trie is empty.
	 *
	 * @return {@code true} if the trie is empty, otherwise {@code false}.
	 */
	boolean isEmpty();

	/**
	 * Formats and prints the trie to the console for debugging.
	 */
	void print();

	/**
	 * Formats and prints the trie to the debug log.
	 */
	void log();

}
