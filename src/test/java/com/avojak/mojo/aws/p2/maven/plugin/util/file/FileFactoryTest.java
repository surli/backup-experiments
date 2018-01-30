package com.avojak.mojo.aws.p2.maven.plugin.util.file;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test class for {@link FileFactory}
 */
public class FileFactoryTest {

	/**
	 * Tests {@link FileFactory#createEmptyFile(String, String)}.
	 */
	@Test
	public void testCreateEmptyFile() {
		try {
			assertNotNull(new FileFactory().createEmptyFile(null, null));
		} catch (IOException e) {
			fail("Unexpected exception");
			e.printStackTrace();
		}
	}

}
