package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.TriePrinter;

/**
 * Implementation of {@link TriePrinter} for {@link System#out}.
 */
public class SystemOutTriePrinter implements TriePrinter {

	@Override
	public void print(final String line) {
		System.out.println(line);
	}

}
