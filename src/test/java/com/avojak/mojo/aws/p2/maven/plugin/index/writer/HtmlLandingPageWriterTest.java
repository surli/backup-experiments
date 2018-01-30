package com.avojak.mojo.aws.p2.maven.plugin.index.writer;

import com.avojak.mojo.aws.p2.maven.plugin.util.file.FileFactory;
import com.avojak.mojo.aws.p2.maven.plugin.util.file.FileWriterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link HtmlLandingPageWriter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HtmlLandingPageWriterTest {

	@Mock
	private FileFactory fileFactory;

	@Mock
	private FileWriterFactory fileWriterFactory;

	@Mock
	private File file;

	@Mock
	private FileWriter fileWriter;

	private final String content = "<html>...</html>";
	private final String filename = "index";

	private LandingPageWriter writer;

	/**
	 * Setup mocks.
	 *
	 * @throws IOException Unexpected.
	 */
	@Before
	public void setup() throws IOException {
		writer = new HtmlLandingPageWriter(fileFactory, fileWriterFactory);
		when(fileFactory.createEmptyFile(filename, ".html")).thenReturn(file);
		when(fileWriterFactory.create(file)).thenReturn(fileWriter);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link FileFactory} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullFileFactory() {
		new HtmlLandingPageWriter(null, fileWriterFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link FileWriterFactory} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullFileWriterFactory() {
		new HtmlLandingPageWriter(fileFactory, null);
	}

	/**
	 * Tests that {@link HtmlLandingPageWriter#write(String, String)} throws an exception when the given content is
	 * {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testWrite_NullContent() throws IOException {
		writer.write(null, filename);
	}

	/**
	 * Tests that {@link HtmlLandingPageWriter#write(String, String)} throws an exception when the given content is
	 * empty.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWrite_EmptyContent() throws IOException {
		writer.write(" ", filename);
	}

	/**
	 * Tests that {@link HtmlLandingPageWriter#write(String, String)} throws an exception when the given file name is
	 * {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testWrite_NullFilename() throws IOException {
		writer.write(content, null);
	}

	/**
	 * Tests that {@link HtmlLandingPageWriter#write(String, String)} throws an exception when the given file name is
	 * empty.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWrite_EmptyFilename() throws IOException {
		writer.write(content, " ");
	}

	/**
	 * Tests {@link HtmlLandingPageWriter#write(String, String)}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testWrite() throws IOException {
		final File actual = writer.write(content, filename);
		assertEquals(file, actual);
		verify(fileWriterFactory).create(file);
	}

}
