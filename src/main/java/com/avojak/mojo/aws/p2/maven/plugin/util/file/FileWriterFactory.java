package com.avojak.mojo.aws.p2.maven.plugin.util.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class to create instances of {@link FileWriter}.
 */
public class FileWriterFactory {

	/**
	 * Creates and returns an instance of {@link FileWriter}.
	 *
	 * @param file The {@link File} for which to create the writer. Cannot be {@code null}.
	 *
	 * @return The new, non-{@code null} {@link FileWriter}.
	 *
	 * @throws IOException If the file exists but is a directory rather than a regular file, does not exist but cannot
	 *                     be created, or cannot be opened for any other reason
	 */
	public FileWriter create(final File file) throws IOException {
		checkNotNull(file, "file cannot be null");
		return new FileWriter(file);
	}

}
