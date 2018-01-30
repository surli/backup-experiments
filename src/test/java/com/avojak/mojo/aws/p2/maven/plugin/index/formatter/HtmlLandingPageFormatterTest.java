package com.avojak.mojo.aws.p2.maven.plugin.index.formatter;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl.BucketTrie;
import com.google.common.base.Charsets;
import com.google.common.escape.Escaper;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link HtmlLandingPageFormatter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HtmlLandingPageFormatterTest {

	@Mock
	private Escaper escaper;

	// Careful modifying these values - the HTML test resources depend on them
	private final String bucketName = "p2.example.com";
	private final String projectName = "example-project";
	private final Trie<String, String> content = new BucketTrie();
	private final Date date = new GregorianCalendar(2018, 0, 26, 14, 14, 17).getTime();

	private HtmlLandingPageFormatter htmlLandingPageFormatter;

	/**
	 * Setup mocks.
	 *
	 * @throws IOException Unexpected.
	 */
	@Before
	public void setup() throws IOException {
		htmlLandingPageFormatter = new HtmlLandingPageFormatter(escaper);
		when(escaper.escape(bucketName)).thenReturn(bucketName);
		when(escaper.escape(projectName)).thenReturn(projectName);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link Escaper} is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullEscaper() throws IOException {
		new HtmlLandingPageFormatter(null);
	}

	/**
	 * Tests that {@link HtmlLandingPageFormatter#format(String, String, Trie, Date)} throws an exception when the given
	 * bucket name is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testFormat_NullBucketName() {
		htmlLandingPageFormatter.format(null, projectName, content, date);
	}

	/**
	 * Tests that {@link HtmlLandingPageFormatter#format(String, String, Trie, Date)} throws an exception when the given
	 * bucket name is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFormat_EmptyBucketName() {
		htmlLandingPageFormatter.format(" ", projectName, content, date);
	}

	/**
	 * Tests that {@link HtmlLandingPageFormatter#format(String, String, Trie, Date)} throws an exception when the given
	 * project name is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testFormat_NullProjectName() {
		htmlLandingPageFormatter.format(bucketName, null, content, date);
	}

	/**
	 * Tests that {@link HtmlLandingPageFormatter#format(String, String, Trie, Date)} throws an exception when the given
	 * project name is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFormat_EmptyProjectName() {
		htmlLandingPageFormatter.format(bucketName, " ", content, date);
	}

	/**
	 * Tests that {@link HtmlLandingPageFormatter#format(String, String, Trie, Date)} throws an exception when the given
	 * {@link Trie} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testFormat_NullContent() {
		htmlLandingPageFormatter.format(bucketName, projectName, null, date);
	}

	/**
	 * Tests that {@link HtmlLandingPageFormatter#format(String, String, Trie, Date)} throws an exception when the given
	 * {@link Date} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testFormat_NullDate() {
		htmlLandingPageFormatter.format(bucketName, projectName, content, null);
	}

	/**
	 * Tests {@link HtmlLandingPageFormatter#format(String, String, Trie, Date)} when there is no repository content.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testFormat_NoRepositoryContent() throws IOException {
		final String expected = getExpectedLandingPage("LandingPage_NoRepositoryContent.html");
		final String actual = htmlLandingPageFormatter.format(bucketName, projectName, content, date);
		assertEquals(expected, actual);
	}

	/**
	 * Tests {@link HtmlLandingPageFormatter#format(String, String, Trie, Date)}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testFormat() throws IOException {
		content.insert("folder", null);
		content.insert("folder/file", "http://www.example.com/file");
		final String expected = getExpectedLandingPage("LandingPage_WithRepositoryContent.html");
		final String actual = htmlLandingPageFormatter.format(bucketName, projectName, content, date);
		assertEquals(expected, actual);
	}

	private static String getExpectedLandingPage(final String name) throws IOException {
		final String filename = "src/test/resources/html/" + name;
		return Resources.toString(new File(filename).toURI().toURL(), Charsets.UTF_8);
	}

}
