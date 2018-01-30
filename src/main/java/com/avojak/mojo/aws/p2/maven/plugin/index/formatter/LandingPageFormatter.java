package com.avojak.mojo.aws.p2.maven.plugin.index.formatter;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;

import java.util.Date;

/**
 * Provides methods to convert repository content into a formatted {@code String}.
 */
public interface LandingPageFormatter {

	/**
	 * Formats the repository content.
	 *
	 * @param bucketName  The bucket name. Cannot be {@code null} or empty.
	 * @param projectName The project name. Cannot be {@code null} or empty.
	 * @param content     The repository content. Cannot be {@code null}.
	 * @param date        The current {@link Date}. Cannot be {@code null}.
	 *
	 * @return The repository content as a formatted {@code String}.
	 */
	String format(final String bucketName, final String projectName, final Trie<String, String> content,
	              final Date date);

}
