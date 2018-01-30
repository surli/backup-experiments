package com.avojak.mojo.aws.p2.maven.plugin.index.generator;

import com.avojak.mojo.aws.p2.maven.plugin.index.formatter.LandingPageFormatter;
import com.avojak.mojo.aws.p2.maven.plugin.index.writer.LandingPageWriter;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl.BucketTrie;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link LandingPageGenerator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LandingPageGeneratorTest {

	@Mock
	private LandingPageFormatter landingPageFormatter;

	@Mock
	private LandingPageWriter landingPageWriter;

	private final String bucketName = "p2.example.com";
	private final String projectName = "example-project";
	private final Trie<String, String> content = new BucketTrie();
	private final Date date = new Date();

	private LandingPageGenerator landingPageGenerator;

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		landingPageGenerator = new LandingPageGenerator(landingPageFormatter, landingPageWriter);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link LandingPageFormatter} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullFormatter() {
		new LandingPageGenerator(null, landingPageWriter);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link LandingPageWriter} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullWriter() {
		new LandingPageGenerator(landingPageFormatter, null);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Trie, Date)} throws an exception when the given
	 * bucket name is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testGenerate_NullBucketName() throws IOException {
		landingPageGenerator.generate(null, projectName, content, date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Trie, Date)} throws an exception when the given
	 * bucket name is empty.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGenerate_EmptyBucketName() throws IOException {
		landingPageGenerator.generate(" ", projectName, content, date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Trie, Date)} throws an exception when the given
	 * project name is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testGenerate_NullProjectName() throws IOException {
		landingPageGenerator.generate(bucketName, null, content, date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Trie, Date)} throws an exception when the given
	 * project name is empty.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGenerate_EmptyProjectName() throws IOException {
		landingPageGenerator.generate(bucketName, " ", content, date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Trie, Date)} throws an exception when the given
	 * repository content {@link Trie} is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testGenerate_NullContent() throws IOException {
		landingPageGenerator.generate(bucketName, projectName, null, date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Trie, Date)} throws an exception when the given
	 * {@link Date} is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testGenerate_NullDate() throws IOException {
		landingPageGenerator.generate(bucketName, projectName, content, null);
	}

	/**
	 * Tests {@link LandingPageGenerator#generate(String, String, Trie, Date)}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testGenerate() throws IOException {
		final String expectedHtml = "<html></html>";
		final File expectedFile = mock(File.class);
		when(landingPageFormatter.format(bucketName, projectName, content, date)).thenReturn(expectedHtml);
		when(landingPageWriter.write(expectedHtml, "index")).thenReturn(expectedFile);

		final File file = landingPageGenerator.generate(bucketName, projectName, content, date);

		assertEquals(expectedFile, file);
	}

}
