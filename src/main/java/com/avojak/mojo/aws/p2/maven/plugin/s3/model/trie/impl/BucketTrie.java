package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.TrieNode;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.TriePrinter;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.impl.DebugLogTriePrinter;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.impl.SystemOutTriePrinter;
import com.avojak.mojo.aws.p2.maven.plugin.util.resource.ResourceUtil;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of a {@link Trie} for bucket content. Values stored within the structure are the hosting URLs for the
 * objects keyed by the object key. Consumers may optionally specify a prefix for content in the trie. This allows for
 * restricting the content inserted into the trie.
 */
public class BucketTrie implements Trie<String, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BucketTrie.class);

	private final TrieNode<String> root;
	private final String prefix;

	/**
	 * Constructor.
	 */
	public BucketTrie() {
		this.root = new DirectoryTrieNode();
		this.prefix = null;
	}

	/**
	 * Constructor.
	 *
	 * @param prefix The prefix for the trie content. Cannot be null or empty.
	 */
	public BucketTrie(final String prefix) {
		checkNotNull(prefix, "prefix cannot be null");
		checkArgument(!prefix.trim().isEmpty(), "prefix cannot be empty");
		this.root = new DirectoryTrieNode();
		this.prefix = prefix;
	}

	@Override
	public void insert(final String key, final String value) {
		checkNotNull(key, "key cannot be null");
		checkArgument(!key.trim().isEmpty(), "key cannot be empty");

		if (prefix != null && !key.startsWith(prefix)) {
			LOGGER.debug(ResourceUtil.getString(getClass(), "nonMatchingPrefix"), key, prefix);
			return;
		}

		// If there is a prefix defined, trim the prefix from the given key. We've already verified that if there is a
		// prefix, that the given key begins with the prefix.
		final String insertKey = prefix != null ? key.replaceFirst(prefix, "") : key;

		Map<String, TrieNode<String>> children = root.getChildren();
		final Iterator<Path> pathIterator = Paths.get(insertKey).iterator();
		while (pathIterator.hasNext()) {
			final String path = pathIterator.next().toString();
			TrieNode<String> node;
			if (children.containsKey(path)) {
				node = children.get(path);
			} else {
				// Make sure we don't set a value on all intermediate nodes
				if (pathIterator.hasNext()) {
					node = new DirectoryTrieNode();
				} else {
					if (value == null) {
						node = new DirectoryTrieNode();
					} else {
						node = new FileTrieNode(value);
					}
				}
				children.put(path, node);
			}
			children = node.getChildren();
		}
	}

	@Override
	public TrieNode<String> getRoot() {
		return root;
	}

	@Override
	public Optional<String> getPrefix() {
		return Optional.fromNullable(prefix);
	}

	@Override
	public boolean isEmpty() {
		return root.getChildren().isEmpty();
	}

	@Override
	public void print() {
		outputChildren("", root, new SystemOutTriePrinter());
	}

	@Override
	public void log() {
		outputChildren("", root, new DebugLogTriePrinter(LOGGER));
	}

	/**
	 * Recursive helper method for {@link BucketTrie#print()}.
	 */
	private void outputChildren(final String indent, final TrieNode<String> node, final TriePrinter printer) {
		final Iterator<Map.Entry<String, TrieNode<String>>> iterator = node.getChildren().entrySet().iterator();
		while (iterator.hasNext()) {
			final Map.Entry<String, TrieNode<String>> entry = iterator.next();
			final TrieNode<String> currentNode = entry.getValue();
			final String key = entry.getKey() + (currentNode.getValue().isPresent() ? "" : "/");
			printer.print(indent + "+--" + key);
			final String nextIndent = indent + (iterator.hasNext() ? "|  " : "   ");
			outputChildren(nextIndent, currentNode, printer);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BucketTrie that = (BucketTrie) o;

		if (!root.equals(that.root)) {
			return false;
		}
		return prefix != null ? prefix.equals(that.prefix) : that.prefix == null;
	}

	@Override
	public int hashCode() {
		int result = root.hashCode();
		result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "BucketTrie{" +
				"root=" + root +
				", prefix='" + prefix + '\'' +
				'}';
	}

}
