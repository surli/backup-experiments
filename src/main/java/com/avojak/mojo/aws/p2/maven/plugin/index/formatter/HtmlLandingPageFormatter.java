package com.avojak.mojo.aws.p2.maven.plugin.index.formatter;

import com.avojak.mojo.aws.p2.maven.plugin.util.resource.ResourceUtil;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.TrieNode;
import com.google.common.base.Optional;
import com.google.common.escape.Escaper;
import com.google.common.io.Resources;
import org.apache.commons.codec.Charsets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link LandingPageFormatter} for HTML formatting.
 */
public class HtmlLandingPageFormatter implements LandingPageFormatter {

	private static final String TITLE_PLACEHOLDER = "{{title}}";
	private static final String BANNER_PLACEHOLDER = "{{banner}}";
	private static final String MESSAGE_PLACEHOLDER = "{{message}}";
	private static final String SHOW_PLACEHOLDER = "{{show}}";
	private static final String HIDE_PLACEHOLDER = "{{hide}}";
	private static final String TIMESTAMP_PLACEHOLDER = "{{timestamp}}";
	private static final String REPOSITORY_CONTENT_PLACEHOLDER = "{{repositoryContent}}";

	private final Escaper escaper;

	private final String indexTemplate;
	private final String fileTemplate;
	private final String folderTemplate;
	private final String seeHowTemplate;

	private final String landingPageMessageFormat;
	private final String bannerFormat;
	private final DateFormat dateFormat;

	private final String noContent;
	private final String showContent;
	private final String hideContent;

	public HtmlLandingPageFormatter(final Escaper escaper) throws IOException {
		this.escaper = checkNotNull(escaper, "escaper cannot be null");

		indexTemplate = readTemplateFileAsString("indexTemplate");
		fileTemplate = readTemplateFileAsString("fileTemplate");
		folderTemplate = readTemplateFileAsString("folderTemplate");
		seeHowTemplate = readTemplateFileAsString("seeHowTemplate");

		landingPageMessageFormat = ResourceUtil.getString(getClass(), "landingPageMessage");
		bannerFormat = ResourceUtil.getString(getClass(), "bannerFormat");
		dateFormat = DateFormat.getDateTimeInstance();

		noContent = ResourceUtil.getString(getClass(), "noContent");
		showContent = ResourceUtil.getString(getClass(), "showContent");
		hideContent = ResourceUtil.getString(getClass(), "hideContent");
	}

	/**
	 * Reads the HTML template file as a String.
	 *
	 * @param resourceKey The resource key for the template file to read.
	 *
	 * @return The HTML template file as a String.
	 *
	 * @throws IOException if the template file cannot be found, or if an {@link IOException} occurs.
	 */
	private String readTemplateFileAsString(final String resourceKey) throws IOException {
		final String templateFilename = ResourceUtil.getString(getClass(), resourceKey);
		return Resources.toString(Resources.getResource(templateFilename), Charsets.UTF_8);
	}

	@Override
	public String format(final String bucketName, final String projectName, final Trie<String, String> content,
	                     final Date date) {
		checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
		checkNotNull(projectName, "projectName cannot be null");
		checkArgument(!projectName.trim().isEmpty(), "projectName cannot be empty");
		checkNotNull(content, "content cannot be null");
		checkNotNull(date, "date cannot be null");

		final String escapedBucketName = escaper.escape(bucketName);
		final String escapedProjectName = escaper.escape(projectName);

		final String banner = formatBanner(escapedBucketName);
		final String message = formatMessage(escapedProjectName);
		final String repositoryContent = formatRepositoryContent(content);
		final String timestamp = dateFormat.format(date);

		return indexTemplate.replace(TITLE_PLACEHOLDER, escapedBucketName)
				.replace(BANNER_PLACEHOLDER, banner)
				.replace(MESSAGE_PLACEHOLDER, message)
				.replace(TIMESTAMP_PLACEHOLDER, timestamp)
				.replace(REPOSITORY_CONTENT_PLACEHOLDER, repositoryContent)
				.replace(SHOW_PLACEHOLDER, showContent)
				.replace(HIDE_PLACEHOLDER, hideContent);
	}

	private String formatRepositoryContent(final Trie<String, String> content) {
		if (content.isEmpty()) {
			return noContent;
		}
		return formatRepositoryContentHtml(content.getRoot(), content.getPrefix().or(""), fileTemplate, folderTemplate);
	}

	/**
	 * Recursive helper method for formatting the repository content as HTML.
	 */
	private String formatRepositoryContentHtml(final TrieNode<String> node, final String prefix,
	                                           final String fileTemplate, final String folderTemplate) {
		final StringBuilder stringBuilder = new StringBuilder();
		final Map<String, TrieNode<String>> children = node.getChildren();
		for (final Map.Entry<String, TrieNode<String>> entry : children.entrySet()) {
			final String key = entry.getKey().replaceFirst(prefix, "");
			final TrieNode<String> currentNode = entry.getValue();
			final Optional<String> nodeValue = currentNode.getValue();
			if (nodeValue.isPresent()) {
				// File
				stringBuilder.append(MessageFormat.format(fileTemplate, nodeValue.get(), key));
			} else {
				// Folder
				final String nodeContent =
						formatRepositoryContentHtml(currentNode, prefix, fileTemplate, folderTemplate);
				stringBuilder.append(MessageFormat.format(folderTemplate, key, nodeContent));
			}
		}
		return stringBuilder.toString();
	}

	private String formatMessage(final String projectName) {
		final String howToURL = ResourceUtil.getString(getClass(), "howToURL");
		final String seeHow = ResourceUtil.getString(getClass(), "seeHow");
		final String seeHowHTML = MessageFormat.format(seeHowTemplate, howToURL, seeHow);
		return MessageFormat.format(landingPageMessageFormat, projectName, seeHowHTML);
	}

	private String formatBanner(final String bucketName) {
		return MessageFormat.format(bannerFormat, bucketName);
	}

}
