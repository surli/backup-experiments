package com.avojak.mojo.aws.p2.maven.plugin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides utility methods for testing with file systems.
 */
public class FileSystemTestUtil {

	/**
	 * Creates an accessible temporary file.
	 */
	public static File createAccessibleFile() throws IOException {
		return createAccessibleFile(null);
	}

	/**
	 * Creates an accessible temporary file in the given directory.
	 */
	public static File createAccessibleFile(final Path directory) throws IOException {
		return createTemporaryFile(directory, true);
	}

	/**
	 * Creates an inaccessible temporary file.
	 */
	public static File createInaccessibleFile() throws IOException {
		return createTemporaryFile(null, false);
	}

	/**
	 * Creates a temporary file with the given permissions.
	 */
	private static File createTemporaryFile(final Path directory, final boolean isAccessible)
			throws IOException {
		File file;
		if (directory != null) {
			file = Files.createTempFile(directory, "mock", null).toFile();
		} else {
			file = Files.createTempFile("mock", null).toFile();
		}
		if (!isAccessible) {
			file.delete(); // Delete here so that doesExist() returns false
		}
		return file;
	}

	/**
	 * Creates an accessible temporary directory.
	 */
	public static File createAccessibleDirectory() throws IOException {
		return createAccessibleDirectory(null);
	}

	/**
	 * Creates an accessible temporary directory in the given directory.
	 */
	public static File createAccessibleDirectory(final Path directory) throws IOException {
		return createTemporaryDirectory(directory, true);
	}

	/**
	 * Creates an inaccessible temporary directory.
	 */
	public static File createInaccessibleDirectory() throws IOException {
		return createTemporaryDirectory(null, false);
	}

	/**
	 * Creates a temporary directory with the given permissions.
	 */
	private static File createTemporaryDirectory(final Path directory, final boolean isAccessible)
			throws IOException {
		File file;
		if (directory != null) {
			file = Files.createTempDirectory(directory, "mock").toFile();
		} else {
			file = Files.createTempDirectory("mock").toFile();
		}
		if (!isAccessible) {
			file.delete(); // Delete here so that doesExist() returns false
		}
		return file;
	}

}
