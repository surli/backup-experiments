package com.avojak.mojo.aws.p2.maven.plugin.s3.repository.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.HeadBucketResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl.BucketTrie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.delete.DeleteObjectRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.head.HeadBucketRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.list.ListObjectsRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.put.PutObjectRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.util.FileSystemTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jtest.LoggingEvent.debug;
import static uk.org.lidalia.slf4jtest.LoggingEvent.error;
import static uk.org.lidalia.slf4jtest.LoggingEvent.warn;

/**
 * Test class for {@link S3BucketRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class S3BucketRepositoryImplTest {

	@Mock
	private AmazonS3Client client;

	@Mock
	private PutObjectRequestFactory putObjectRequestFactory;

	@Mock
	private PutObjectRequest putObjectRequest;

	@Mock
	private DeleteObjectRequestFactory deleteObjectRequestFactory;

	@Mock
	private DeleteObjectRequest deleteObjectRequest;

	@Mock
	private ListObjectsRequestFactory listObjectsRequestFactory;

	@Mock
	private ListObjectsRequest listObjectsRequest;

	@Mock
	private ObjectListing objectListing;

	@Mock
	private S3ObjectSummary objectSummary;

	@Mock
	private HeadBucketRequestFactory headBucketRequestFactory;

	@Mock
	private HeadBucketRequest headBucketRequest;

	@Mock
	private HeadBucketResult headBucketResult;

	private final TestLogger logger = TestLoggerFactory.getTestLogger(S3BucketRepositoryImpl.class);

	private final String bucketName = "mock";
	private final String bucketLocation = "us-east-1";

	private S3BucketRepositoryImpl repository;

	/**
	 * Setup mocks.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Before
	public void setup() throws BucketDoesNotExistException {
		when(client.doesBucketExist(bucketName)).thenReturn(true);
		repository = new S3BucketRepositoryImpl(client, bucketName, putObjectRequestFactory, deleteObjectRequestFactory,
				listObjectsRequestFactory, headBucketRequestFactory);
	}

	/**
	 * Clear loggers.
	 */
	@After
	public void clearLoggers() {
		TestLoggerFactory.clear();
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link AmazonS3} client is {@code null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullClient() throws BucketDoesNotExistException {
		new S3BucketRepositoryImpl(null, bucketName, putObjectRequestFactory, deleteObjectRequestFactory,
				listObjectsRequestFactory, headBucketRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is {@code null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullBucketName() throws BucketDoesNotExistException {
		new S3BucketRepositoryImpl(client, null, putObjectRequestFactory, deleteObjectRequestFactory,
				listObjectsRequestFactory, headBucketRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is empty.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyBucketName() throws BucketDoesNotExistException {
		new S3BucketRepositoryImpl(client, " ", putObjectRequestFactory, deleteObjectRequestFactory,
				listObjectsRequestFactory, headBucketRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link PutObjectRequestFactory} is {@code null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullPutObjectRequestFactory() throws BucketDoesNotExistException {
		new S3BucketRepositoryImpl(client, bucketName, null, deleteObjectRequestFactory,
				listObjectsRequestFactory, headBucketRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link DeleteObjectRequestFactory} is {@code
	 * null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullDeleteObjectRequestFactory() throws BucketDoesNotExistException {
		new S3BucketRepositoryImpl(client, bucketName, putObjectRequestFactory, null,
				listObjectsRequestFactory, headBucketRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link ListObjectsRequestFactory} is {@code
	 * null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullListObjectsRequestFactory() throws BucketDoesNotExistException {
		new S3BucketRepositoryImpl(client, bucketName, putObjectRequestFactory, deleteObjectRequestFactory, null,
				headBucketRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link HeadBucketRequestFactory} is {@code
	 * null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullHeadBucketRequestFactory() throws BucketDoesNotExistException {
		new S3BucketRepositoryImpl(client, bucketName, putObjectRequestFactory, deleteObjectRequestFactory,
				listObjectsRequestFactory, null);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket does not exist.
	 *
	 * @throws BucketDoesNotExistException Expected.
	 */
	@Test(expected = BucketDoesNotExistException.class)
	public void testConstructor_BucketDoesNotExist() throws BucketDoesNotExistException {
		when(client.doesBucketExist(bucketName)).thenReturn(false);
		new S3BucketRepositoryImpl(client, bucketName, putObjectRequestFactory, deleteObjectRequestFactory,
				listObjectsRequestFactory, headBucketRequestFactory);
	}

	/**
	 * Tests that {@link S3BucketRepositoryImpl#uploadFile(File, BucketPath)} throws an exception when the given {@link
	 * File} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testUploadFile_NullFile() {
		repository.uploadFile(null, new BucketPath());
	}

	/**
	 * Tests that {@link S3BucketRepositoryImpl#uploadFile(File, BucketPath)} throws an exception when the given destination
	 * {@link BucketPath} is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testUploadFile_NullDestination() throws IOException {
		repository.uploadFile(FileSystemTestUtil.createAccessibleFile(), null);
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadFile(File, BucketPath)} when the given {@link File} is not accessible.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadFile_FileNotAccessible() throws IOException {
		final File file = FileSystemTestUtil.createInaccessibleFile();
		final String key = repository.uploadFile(file, new BucketPath());

		assertNull(key);
		assertThat(logger.getLoggingEvents(), is(singletonList(warn("File is not accessible: {}", file.getName()))));
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadFile(File, BucketPath)} when the given {@link File} is not a file.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadFile_FileNotAFile() throws IOException {
		final File file = FileSystemTestUtil.createAccessibleDirectory();
		final String key = repository.uploadFile(file, new BucketPath());

		assertNull(key);
		assertThat(logger.getLoggingEvents(), is(singletonList(warn("File is not accessible: {}", file.getName()))));
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadFile(File, BucketPath)} when the creation of the upload request fails.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Expected to be caught and handled.
	 */
	@Test
	public void testUploadFile_UploadRequestCreationFailed() throws IOException, ObjectRequestCreationException {
		final File file = FileSystemTestUtil.createAccessibleFile();
		final BucketPath destination = new BucketPath().append("repository");
		final Throwable throwable = new ObjectRequestCreationException();
		when(putObjectRequestFactory.create(file, destination.asString())).thenThrow(throwable);

		final String key = repository.uploadFile(file, destination);

		assertNull(key);
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);

		final LoggingEvent uploadLoggingEvent = debug("Uploading file: {}", destination.asString());
		final LoggingEvent requestCreationFailedLoggingEvent = error(throwable, "Failed to create upload request");
		assertThat(logger.getLoggingEvents(), is(asList(uploadLoggingEvent, requestCreationFailedLoggingEvent)));
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadFile(File, BucketPath)} when the specified file already exists in the
	 * bucket and is first deleted.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadFile_DeleteExistingFile() throws IOException, ObjectRequestCreationException {
		final File file = FileSystemTestUtil.createAccessibleFile();
		final BucketPath destination = new BucketPath().append("repository");
		when(putObjectRequestFactory.create(file, destination.asString())).thenReturn(putObjectRequest);
		final String expectedKey = destination.asString();
//		when(client.getUrl(bucketName, destination.asString())).thenReturn(expectedUrl);

		final String key = repository.uploadFile(file, destination);

		assertEquals(expectedKey, key);
		verify(client).putObject(putObjectRequest);
		assertThat(logger.getLoggingEvents(), is(singletonList(debug("Uploading file: {}", destination.asString()))));
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadFile(File, BucketPath)}.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadFile() throws IOException, ObjectRequestCreationException {
		final File file = FileSystemTestUtil.createAccessibleFile();
		final BucketPath destination = new BucketPath().append("repository");
		when(putObjectRequestFactory.create(file, destination.asString())).thenReturn(putObjectRequest);
		final String expectedKey = destination.asString();
//		when(client.getUrl(bucketName, destination.asString())).thenReturn(expectedUrl);

		final String key = repository.uploadFile(file, destination);

		assertEquals(expectedKey, key);
		verify(client).putObject(putObjectRequest);
		assertThat(logger.getLoggingEvents(), is(singletonList(debug("Uploading file: {}", destination.asString()))));
	}

	/**
	 * Tests that {@link S3BucketRepositoryImpl#uploadDirectory(File, BucketPath)} throws an exception when the given
	 * directory is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testUploadDirectory_NullDirectory() {
		repository.uploadDirectory(null, new BucketPath());
	}

	/**
	 * Tests that {@link S3BucketRepositoryImpl#uploadDirectory(File, BucketPath)} throws an exception when the given
	 * destination is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testUploadDirectory_NullDestination() throws IOException {
		repository.uploadDirectory(FileSystemTestUtil.createAccessibleDirectory(), null);
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadDirectory(File, BucketPath)} when the given directory is not accessible.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadDirectory_DirectoryNotAccessible() throws IOException {
		final File directory = FileSystemTestUtil.createInaccessibleDirectory();
		final Trie<String, String> content = repository.uploadDirectory(directory, new BucketPath());

		assertTrue(content.isEmpty());
		assertThat(logger.getLoggingEvents(),
				is(singletonList(warn("Directory is not accessible: {}", directory.getName()))));
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadDirectory(File, BucketPath)} when the given directory is not a directory.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadDirectory_DirectoryNotADirectory() throws IOException {
		final File directory = FileSystemTestUtil.createAccessibleFile();
		final Trie<String, String> content = repository.uploadDirectory(directory, new BucketPath());

		assertTrue(content.isEmpty());
		assertThat(logger.getLoggingEvents(),
				is(singletonList(warn("Directory is not accessible: {}", directory.getName()))));
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadDirectory(File, BucketPath)} when the given directory already exists.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadDirectory_DeleteExistingDirectory() throws IOException {
		final File directory = FileSystemTestUtil.createAccessibleDirectory();
		final BucketPath destination = new BucketPath().append("repository");

		final Trie<String, String> content = repository.uploadDirectory(directory, destination);

		assertTrue(content.isEmpty());
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);

		assertThat(logger.getLoggingEvents(),
				is(singletonList(debug("Skipping upload of empty directory: {}", directory.getName()))));
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadDirectory(File, BucketPath)} when the given directory is empty.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadDirectory_EmptyDirectory() throws IOException {
		final File directory = FileSystemTestUtil.createAccessibleDirectory();
		final BucketPath destination = new BucketPath().append("repository");

		final Trie<String, String> content = repository.uploadDirectory(directory, destination);

		assertTrue(content.isEmpty());
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);

		assertThat(logger.getLoggingEvents(),
				is(singletonList(debug("Skipping upload of empty directory: {}", directory.getName()))));
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadDirectory(File, BucketPath)} when the given directory has a child
	 * directory.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadDirectory_ChildDirectory() throws IOException {
		final File parentDirectory = FileSystemTestUtil.createAccessibleDirectory();
		FileSystemTestUtil.createAccessibleDirectory(parentDirectory.toPath());

		final BucketPath parentDirectoryDestination = new BucketPath().append("repository");

		final Trie<String, String> expectedContent = new BucketTrie();
//		final URL expectedUrl = new URL("http", "example", "mock");
//		when(client.getUrl(bucketName, parentDirectoryDestination.asString())).thenReturn(expectedUrl);

		final Trie<String, String> content = repository.uploadDirectory(parentDirectory, parentDirectoryDestination);

		assertEquals(expectedContent, content);
		verify(client).doesBucketExist(bucketName);
//		verify(client).getUrl(bucketName, parentDirectoryDestination.asString());
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#uploadDirectory(File, BucketPath)} when the given directory has a child file.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadDirectory_ChildFile() throws IOException, ObjectRequestCreationException {
		final File directory = FileSystemTestUtil.createAccessibleDirectory();
		final File file = FileSystemTestUtil.createAccessibleFile(directory.toPath());

		final BucketPath directoryDestination = new BucketPath().append("repository");
		final BucketPath fileDestination = new BucketPath(directoryDestination).append(file.getName());
		when(putObjectRequestFactory.create(file, fileDestination.asString())).thenReturn(putObjectRequest);
		when(headBucketRequestFactory.create()).thenReturn(headBucketRequest);
		when(client.headBucket(headBucketRequest)).thenReturn(headBucketResult);
		when(headBucketResult.getBucketRegion()).thenReturn(bucketLocation);
		final Trie<String, String> expectedContent = new BucketTrie();
		final String expectedUrl =
				"http://" + bucketName + ".s3-website-" + bucketLocation + ".amazonaws.com/repository/" + file.getName();
		expectedContent.insert(fileDestination.asString(), expectedUrl);
//		final URL expectedUrl = new URL("http", "example", "mock");
//		when(client.getUrl(bucketName, directoryDestination.asString())).thenReturn(expectedUrl);

		final Trie<String, String> content = repository.uploadDirectory(directory, directoryDestination);

		assertEquals(expectedContent, content);
		verify(client).doesBucketExist(bucketName);
		verify(client).putObject(putObjectRequest);
		verify(client).headBucket(headBucketRequest);
//		verify(client).getUrl(bucketName, directoryDestination.asString());
//		verify(client).getUrl(bucketName, fileDestination.asString());
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests that {@link S3BucketRepositoryImpl#deleteDirectory(String)} throws an exception when the given prefix is
	 * {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testDeleteDirectory_NullPrefix() {
		repository.deleteDirectory(null);
	}

	/**
	 * Tests that {@link S3BucketRepositoryImpl#deleteDirectory(String)} throws an exception when the given prefix is
	 * empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testDeleteDirectory_EmptyPrefix() {
		repository.deleteDirectory(" ");
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#deleteDirectory(String)}.
	 */
	@Test
	public void testDeleteDirectory() {
		final String prefix = "prefix";
		when(listObjectsRequestFactory.create(prefix)).thenReturn(listObjectsRequest);
		when(client.listObjects(listObjectsRequest)).thenReturn(objectListing);
		when(objectListing.getObjectSummaries()).thenReturn(singletonList(objectSummary));
		when(objectListing.isTruncated()).thenReturn(false);
		when(objectSummary.getKey()).thenReturn(prefix);
		when(deleteObjectRequestFactory.create(prefix)).thenReturn(deleteObjectRequest);

		repository.deleteDirectory(prefix);

		verify(client).deleteObject(deleteObjectRequest);
		assertThat(logger.getLoggingEvents(), is(singletonList(debug("Deleting existing object: {}", prefix))));
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#deleteDirectory(String)} when the returned collections of objects is
	 * truncated.
	 */
	@Test
	public void testDeleteDirectory_ResultsTruncated() {
		final String prefix = "prefix";
		when(listObjectsRequestFactory.create(prefix)).thenReturn(listObjectsRequest);

		final ObjectListing objectListing1 = mock(ObjectListing.class);
		final ObjectListing objectListing2 = mock(ObjectListing.class);

		when(client.listObjects(listObjectsRequest)).thenReturn(objectListing1);
		when(client.listNextBatchOfObjects(objectListing1)).thenReturn(objectListing2);

		final S3ObjectSummary objectSummary1 = mock(S3ObjectSummary.class);
		final S3ObjectSummary objectSummary2 = mock(S3ObjectSummary.class);

		final String key1 = "key1";
		final String key2 = "key2";
		when(objectSummary1.getKey()).thenReturn(key1);
		when(objectSummary2.getKey()).thenReturn(key2);

		when(objectListing1.getObjectSummaries()).thenReturn(singletonList(objectSummary1));
		when(objectListing2.getObjectSummaries()).thenReturn(singletonList(objectSummary2));

		when(objectListing1.isTruncated()).thenReturn(true);
		when(objectListing2.isTruncated()).thenReturn(false);

		final DeleteObjectRequest deleteObjectRequest1 = mock(DeleteObjectRequest.class);
		final DeleteObjectRequest deleteObjectRequest2 = mock(DeleteObjectRequest.class);
		when(deleteObjectRequestFactory.create(key1)).thenReturn(deleteObjectRequest1);
		when(deleteObjectRequestFactory.create(key2)).thenReturn(deleteObjectRequest2);

		repository.deleteDirectory(prefix);

		verify(client).deleteObject(deleteObjectRequest1);
		verify(client).deleteObject(deleteObjectRequest2);
		final LoggingEvent event1 = debug("Deleting existing object: {}", key1);
		final LoggingEvent event2 = debug("Deleting existing object: {}", key2);
		assertThat(logger.getLoggingEvents(), is(asList(event1, event2)));
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#getHostingUrl(String)} when the given key is null.
	 */
	@Test
	public void testGetHostingUrl_NullKey() {
		when(headBucketRequestFactory.create()).thenReturn(headBucketRequest);
		when(client.headBucket(headBucketRequest)).thenReturn(headBucketResult);
		when(headBucketResult.getBucketRegion()).thenReturn(bucketLocation);
		final String key = null;
		final String expectedUrl = "http://" + bucketName + ".s3-website-" + bucketLocation + ".amazonaws.com/";

		final String url = repository.getHostingUrl(key);

		assertEquals(expectedUrl, url);
	}

	/**
	 * Tests {@link S3BucketRepositoryImpl#getHostingUrl(String)}.
	 */
	@Test
	public void testGetHostingUrl() {
		when(headBucketRequestFactory.create()).thenReturn(headBucketRequest);
		when(client.headBucket(headBucketRequest)).thenReturn(headBucketResult);
		when(headBucketResult.getBucketRegion()).thenReturn(bucketLocation);
		final String key = "key";
		final String expectedUrl = "http://" + bucketName + ".s3-website-" + bucketLocation + ".amazonaws.com/" + key;

		final String url = repository.getHostingUrl(key);

		assertEquals(expectedUrl, url);
	}

}
