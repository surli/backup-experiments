package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;

/**
 * Provides methods to print contents of a {@link Trie} for debugging purposes.
 */
public interface TriePrinter {

	/**
	 * Prints the given line.
	 *
	 * @param line The line to print.
	 */
	void print(final String line);

}
