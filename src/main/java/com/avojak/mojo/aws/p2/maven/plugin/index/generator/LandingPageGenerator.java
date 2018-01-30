package com.avojak.mojo.aws.p2.maven.plugin.index.generator;

import com.avojak.mojo.aws.p2.maven.plugin.index.formatter.LandingPageFormatter;
import com.avojak.mojo.aws.p2.maven.plugin.index.writer.LandingPageWriter;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to generate the landing page for the repository.
 */
public class LandingPageGenerator {

	private static final String PAGE_NAME = "index";

	private final LandingPageFormatter formatter;
	private final LandingPageWriter writer;

	/**
	 * Constructor.
	 *
	 * @param formatter The {@link LandingPageFormatter}. Cannot be {@code null}.
	 * @param writer    The {@link LandingPageWriter}. Cannot be {@code null}.
	 */
	public LandingPageGenerator(final LandingPageFormatter formatter, final LandingPageWriter writer) {
		this.formatter = checkNotNull(formatter, "formatter cannot be null");
		this.writer = checkNotNull(writer, "writer cannot be null");
	}

	/**
	 * Generates the landing page {@link File}.
	 *
	 * @param bucketName  The name of the bucket. Cannot be {@code null} or empty.
	 * @param projectName The name of the project. Cannot be {@code null} or empty.
	 * @param content     The {@link Trie} content of the bucket. Cannot be {@code null}.
	 * @param date        The current {@link Date}. Cannot be {@code null}.
	 *
	 * @return The non-{@code null} landing page {@link File}.
	 *
	 * @throws IOException If an {@link IOException} occurs.
	 */
	public File generate(final String bucketName, final String projectName, final Trie<String, String> content,
	                     final Date date) throws IOException {
		checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
		checkNotNull(projectName, "projectName cannot be null");
		checkArgument(!projectName.trim().isEmpty(), "projectName cannot be empty");
		checkNotNull(content, "content cannot be null");
		checkNotNull(date, "date cannot be null");
		final String html = formatter.format(bucketName, projectName, content, date);
		return writer.write(html, PAGE_NAME);
	}

}
