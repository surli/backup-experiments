package com.avojak.mojo.aws.p2.maven.plugin.util.file;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test class for {@link FileWriterFactory}.
 */
public class FileWriterFactoryTest {

	/**
	 * Tests that {@link FileWriterFactory#create(File)} throws an exception when the given {@link File} is
	 * \{@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreate_NullFile() {
		try {
			new FileWriterFactory().create(null);
		} catch (IOException e) {
			fail("Unexpected exception");
			e.printStackTrace();
		}
	}

	/**
	 * Testst {@link FileWriterFactory#create(File)}.
	 */
	@Test
	public void testCreate() {
		try {
			final File file = Files.createTempFile(null, null).toFile();
			file.deleteOnExit();
			assertNotNull(new FileWriterFactory().create(file));
		} catch (IOException e) {
			fail("Unexpected exception");
			e.printStackTrace();
		}
	}

}
