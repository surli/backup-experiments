package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.TriePrinter;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link TriePrinter} for {@link Logger#debug(String)}.
 */
public class DebugLogTriePrinter implements TriePrinter {

	private final Logger logger;

	/**
	 * Constructor.
	 *
	 * @param logger The {@link Logger}. Cannot be {@code null}.
	 */
	public DebugLogTriePrinter(final Logger logger) {
		this.logger = checkNotNull(logger, "logger cannot be null");
	}

	@Override
	public void print(final String line) {
		logger.debug(line);
	}

}
