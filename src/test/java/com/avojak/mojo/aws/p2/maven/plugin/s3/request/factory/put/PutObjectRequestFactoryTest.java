package com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.put;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.org.lidalia.slf4jtest.LoggingEvent.debug;

/**
 * Test class for {@link PutObjectRequestFactory}.
 */
public class PutObjectRequestFactoryTest {

	private final TestLogger logger = TestLoggerFactory.getTestLogger(PutObjectRequestFactory.class);

	private final String bucketName = "mock";
	private final String destination = "repository";

	private PutObjectRequestFactory factory;

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		factory = new PutObjectRequestFactory(bucketName);
	}

	/**
	 * Clear loggers.
	 */
	@After
	public void clearLoggers() {
		TestLoggerFactory.clear();
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullBucketName() {
		new PutObjectRequestFactory(null);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyBucketName() {
		new PutObjectRequestFactory(" ");
	}

	/**
	 * Tests that {@link PutObjectRequestFactory#create(File, String)} throws an exception when the given file is {@code
	 * null}.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreate_NullFile() throws ObjectRequestCreationException {
		factory.create(null, destination);
	}

	/**
	 * Tests that {@link PutObjectRequestFactory#create(File, String)} throws an exception when the given destination is
	 * {@code null}.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 * @throws IOException                    Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreate_NullDestination() throws ObjectRequestCreationException, IOException {
		factory.create(createTemporaryFile(), null);
	}

	/**
	 * Tests that {@link PutObjectRequestFactory#create(File, String)} throws an exception when the given destination is
	 * empty.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 * @throws IOException                    Unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testCreate_EmptyDestination() throws ObjectRequestCreationException, IOException {
		factory.create(createTemporaryFile(), " ");
	}

	/**
	 * Tests that {@link PutObjectRequestFactory#create(File, String)} throws an exception when the given file cannot be
	 * found.
	 *
	 * @throws ObjectRequestCreationException Expected.
	 */
	@Test(expected = ObjectRequestCreationException.class)
	public void testCreate_FileNotFound() throws ObjectRequestCreationException {
		factory.create(new File(""), destination);
	}

	/**
	 * Tests {@link PutObjectRequestFactory#create(File, String)}.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 * @throws IOException                    Unexpected.
	 */
	@Test
	public void testCreate() throws ObjectRequestCreationException, IOException {
		final File file = createTemporaryFile();
		final PutObjectRequest request = factory.create(file, destination);

		assertThat(logger.getLoggingEvents(), is(EMPTY_LIST));

		assertEquals(bucketName, request.getBucketName());
		assertEquals(destination, request.getKey());
		assertEquals(file.length(), request.getMetadata().getContentLength());
		assertEquals(CannedAccessControlList.PublicRead, request.getCannedAcl());
	}

	/**
	 * Tests {@link PutObjectRequestFactory#create(File, String)} when the file is an HTML file. The metadata should be
	 * updated to set the Content-Type to text/html to support static website hosting.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 * @throws IOException                    Unexpected.
	 */
	@Test
	public void testCreate_HtmlContentType() throws ObjectRequestCreationException, IOException {
		final File file = createTemporaryFile(".html");
		final PutObjectRequest request = factory.create(file, destination);

		assertThat(logger.getLoggingEvents(), is(singletonList(debug("Setting Content-Type on metadata to text/html for file: {}", file.getName()))));

		assertEquals(bucketName, request.getBucketName());
		assertEquals(destination, request.getKey());
		assertEquals(file.length(), request.getMetadata().getContentLength());
		assertEquals("text/html", request.getMetadata().getContentType());
		assertEquals(CannedAccessControlList.PublicRead, request.getCannedAcl());
	}

	/**
	 * Creates a temporary file with no file suffix.
	 */
	private File createTemporaryFile() throws IOException {
		return createTemporaryFile(null);
	}

	/**
	 * Creates a temporary file with the given suffix.
	 */
	private File createTemporaryFile(final String suffix) throws IOException {
		return Files.createTempFile("mock", suffix).toFile();
	}

}
