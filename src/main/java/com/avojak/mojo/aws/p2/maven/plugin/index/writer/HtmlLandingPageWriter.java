package com.avojak.mojo.aws.p2.maven.plugin.index.writer;

import com.avojak.mojo.aws.p2.maven.plugin.util.file.FileFactory;
import com.avojak.mojo.aws.p2.maven.plugin.util.file.FileWriterFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link LandingPageWriter} to write HTML files.
 */
public class HtmlLandingPageWriter implements LandingPageWriter {

	private static final String FILE_EXTENSION = ".html";

	private final FileFactory fileFactory;
	private final FileWriterFactory fileWriterFactory;

	public HtmlLandingPageWriter(final FileFactory fileFactory, final FileWriterFactory fileWriterFactory) {
		this.fileFactory = checkNotNull(fileFactory, "fileFactory cannot be null");
		this.fileWriterFactory = checkNotNull(fileWriterFactory, "fileWriterFactory cannot be null");
	}

	@Override
	public File write(final String content, final String filename) throws IOException {
		checkNotNull(content, "content cannot be null");
		checkArgument(!content.trim().isEmpty(), "content cannot be empty");
		checkNotNull(filename, "filename cannot be null");
		checkArgument(!filename.trim().isEmpty(), "filename cannot be empty");

		final File file = fileFactory.createEmptyFile(filename, FILE_EXTENSION);
		return writeContentsToFile(file, content);
	}

	/**
	 * Writes the String content to the {@link File}.
	 *
	 * @param file     The file to write content to.
	 * @param contents The String contents.
	 *
	 * @return The non-{@code null} {@link File}.
	 *
	 * @throws IOException if an {@link IOException} occurs.
	 */
	private File writeContentsToFile(final File file, final String contents) throws IOException {
		FileWriter fileWriter = null;
		try {
			fileWriter = fileWriterFactory.create(file);
			fileWriter.write(contents);
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
		return file;
	}

}