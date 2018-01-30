package com.avojak.mojo.aws.p2.maven.plugin.index.writer;

import java.io.File;
import java.io.IOException;

/**
 * Provides methods to write formatted content to file.
 */
public interface LandingPageWriter {

	/**
	 * Writes the given formatted content to file.
	 *
	 * @param formattedContent The formatted content {@code String}. Cannot be {@code null} or empty.
	 * @param filename         The name of the file to write (without the file extension). Cannot be {@code null} or
	 *                         empty.
	 *
	 * @return The non-{@code null} {@link File} containing the formatted content.
	 *
	 * @throws IOException If an {@link IOException} occurs.
	 */
	File write(final String formattedContent, final String filename) throws IOException;

}
