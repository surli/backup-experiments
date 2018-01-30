package com.avojak.mojo.aws.p2.maven.plugin.util.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Factory class to create instances of {@link File}.
 */
public class FileFactory {

	/**
	 * Creates an empty file.
	 *
	 * @param name      The name of the file. May be {@code null}.
	 * @param extension The extension of the file. May be {@code null}, in which case <pre>.tmp</pre> is used.
	 *
	 * @return The new, non-{@code null} {@link File}.
	 *
	 * @throws IOException If an I/O exception occurs.
	 */
	public File createEmptyFile(final String name, final String extension) throws IOException {
		return Files.createTempFile(name, extension).toFile();
	}

}
