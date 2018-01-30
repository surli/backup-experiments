package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie;

import com.google.common.base.Optional;

import java.util.Map;

/**
 * Models the node of a {@link Trie}.
 *
 * @param <V> The type of the value contained in the node.
 */
public interface TrieNode<V> {

	/**
	 * Returns the value contained in the node, if present.
	 *
	 * @return An {@link Optional} containing the value in the node, if present. Otherwise, returns
	 * {@link Optional#absent()}.
	 */
	Optional<V> getValue();

	/**
	 * Returns the children of this node.
	 *
	 * @return The non-{@code null}, possibly empty {@link Map} of child nodes.
	 */
	Map<String, TrieNode<V>> getChildren();

}
