//package com.avojak.mojo.aws.p2.maven.plugin;
//
//import com.avojak.mojo.aws.p2.maven.plugin.generator.HtmlLandingPageWriter;
//import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
//import com.avojak.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
//import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepositoryFactory;
//import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.impl.S3BucketRepositoryImpl;
//import org.apache.maven.plugin.MojoFailureException;
//import org.apache.maven.project.MavenProject;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//import uk.org.lidalia.slf4jtest.TestLogger;
//import uk.org.lidalia.slf4jtest.TestLoggerFactory;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.Date;
//
//import static java.util.Collections.singletonList;
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThat;
//import static org.junit.Assert.fail;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static uk.org.lidalia.slf4jtest.LoggingEvent.info;
//
///**
// * Test class for {@link AWSP2Mojo}.
// */
//@RunWith(MockitoJUnitRunner.class)
//public class AWSP2MojoTest {
//
//	private static final String REPOSITORY_DIR = "repository";
//	private static final String SNAPSHOT_DIR = "snapshot";
//	private static final String RELEASE_DIR = "release";
//	private static final String SNAPSHOT_VERSION = "1.0.0-SNAPSHOT";
//	private static final String RELEASE_VERSION = "1.0.0";
//
//	@Mock
//	private MavenProject project;
//
//	@Mock
//	private S3BucketRepositoryFactory repositoryFactory;
//
//	@Mock
//	private HtmlLandingPageWriter landingPageGenerator;
//
//	@Mock
//	private S3BucketRepositoryImpl repository;
//
//	private String artifactId;
//	private String outputDirectory;
//	private String bucketName;
//	private String targetSiteDirectory;
//
//	private final TestLogger logger = TestLoggerFactory.getTestLogger(AWSP2Mojo.class);
//
//	private AWSP2Mojo mojo;
//
//	/**
//	 * Setup mocks.
//	 *
//	 * @throws BucketDoesNotExistException unexpected.
//	 */
//	@Before
//	public void setup() throws BucketDoesNotExistException {
//		final String projectVersion = SNAPSHOT_VERSION;
//		artifactId = "mock-project";
//		outputDirectory = "target";
//		bucketName = "mock";
//		targetSiteDirectory = artifactId + "/" + projectVersion;
//
//		when(project.getVersion()).thenReturn(projectVersion);
//		when(project.getArtifactId()).thenReturn(artifactId);
//		when(repositoryFactory.create(bucketName)).thenReturn(repository);
//
//		mojo = new AWSP2Mojo(repositoryFactory, landingPageGenerator);
//		mojo.setProject(project);
//		mojo.setBucket(bucketName);
//		mojo.setDeploySnapshots(true);
//		mojo.setTargetSiteDirectory(targetSiteDirectory);
//		mojo.setSkip(false);
//		mojo.setDedicatedBuckets(true);
//		mojo.setGenerateLandingPage(false);
//		mojo.setOutputDirectory(new File(outputDirectory));
//	}
//
//	/**
//	 * Clear loggers.
//	 */
//	@After
//	public void clearLoggers() {
//		TestLoggerFactory.clear();
//	}
//
//	/**
//	 * Tests that {@link AWSP2Mojo#execute()} skips execution when the skip property is set to {@code true}.
//	 *
//	 * @throws MojoFailureException        Unexpected.
//	 * @throws BucketDoesNotExistException Unexpected.
//	 */
//	@Test
//	public void testExecute_SkipExecution()
//			throws MojoFailureException, BucketDoesNotExistException {
//		mojo.setSkip(true);
//		mojo.execute();
//
//		verify(repositoryFactory, never()).create(any(String.class));
//		assertThat(logger.getLoggingEvents(), is(singletonList(info("Skipping execution"))));
//	}
//
//	/**
//	 * Tests that {@link AWSP2Mojo#execute()} skips execution when the skip snapshot property is set to {@code true} and
//	 * the current project version is a snapshot version.
//	 *
//	 * @throws MojoFailureException        Unexpected.
//	 * @throws BucketDoesNotExistException Unexpected.
//	 */
//	@Test
//	public void testExecute_SkipSnapshot()
//			throws MojoFailureException, BucketDoesNotExistException {
//		mojo.setDeploySnapshots(false);
//		mojo.execute();
//
//		verify(repositoryFactory, never()).create(any(String.class));
//		assertThat(logger.getLoggingEvents(), is(singletonList(info("Skipping deployment of SNAPSHOT version"))));
//	}
//
//	/**
//	 * Tests that {@link AWSP2Mojo#execute()} throws an exception when the specified bucket does not exist.
//	 *
//	 * @throws BucketDoesNotExistException Expected to be caught and wrapped by {@link MojoFailureException}.
//	 */
//	@Test
//	public void testExecute_BucketDoesNotExist()
//			throws BucketDoesNotExistException {
//		when(repositoryFactory.create(bucketName)).thenThrow(BucketDoesNotExistException.class);
//
//		try {
//			mojo.execute();
//			fail("Expected exception not thrown");
//		} catch (final MojoFailureException e) {
//			assertEquals("The specified bucket does not exist", e.getMessage());
//		}
//	}
//
//	/**
//	 * Tests {@link AWSP2Mojo#execute()} when not using dedicated buckets, and the current version is a snapshot
//	 * version.
//	 *
//	 * @throws MojoFailureException  Unexpected.
//	 * @throws MalformedURLException Unexpected.
//	 */
//	@Test
//	public void testExecute_NotUsingDedicatedBucket_SnapshotDeployment()
//			throws MojoFailureException, MalformedURLException {
//		when(project.getVersion()).thenReturn(SNAPSHOT_VERSION);
//		mojo.setDedicatedBuckets(false);
//		final File expectedRepositoryDirectory = new File(outputDirectory, REPOSITORY_DIR);
//		final BucketPath expectedDestination = new BucketPath().append(SNAPSHOT_DIR).append(targetSiteDirectory);
//		final URL expectedUrl = new URL("http", "example.com", "mock");
//		when(repository.uploadDirectory(expectedRepositoryDirectory, expectedDestination)).thenReturn(expectedUrl);
//		when(repository.getHostingUrl(expectedDestination.asString())).thenReturn(expectedUrl.toString());
//
//		mojo.execute();
//
//		assertThat(logger.getLoggingEvents(), is(singletonList(info("Upload complete: {}", expectedUrl.toString()))));
//		verify(repository).deleteDirectory(expectedDestination.asString());
//		verify(repository).uploadDirectory(expectedRepositoryDirectory, expectedDestination);
//	}
//
//	/**
//	 * Tests {@link AWSP2Mojo#execute()} when not using dedicated buckets, and the current version is a release
//	 * version.
//	 *
//	 * @throws MojoFailureException  Unexpected.
//	 * @throws MalformedURLException Unexpected.
//	 */
//	@Test
//	public void testExecute_NotUsingDedicatedBucket_ReleaseDeployment()
//			throws MojoFailureException, MalformedURLException {
//		when(project.getVersion()).thenReturn(RELEASE_VERSION);
//		mojo.setDedicatedBuckets(false);
//		final File expectedRepositoryDirectory = new File(outputDirectory, REPOSITORY_DIR);
//		final BucketPath expectedDestination = new BucketPath().append(RELEASE_DIR).append(targetSiteDirectory);
//		final URL expectedUrl = new URL("http", "example.com", "mock");
//		when(repository.uploadDirectory(expectedRepositoryDirectory, expectedDestination)).thenReturn(expectedUrl);
//		when(repository.getHostingUrl(expectedDestination.asString())).thenReturn(expectedUrl.toString());
//
//		mojo.execute();
//
//		assertThat(logger.getLoggingEvents(), is(singletonList(info("Upload complete: {}", expectedUrl.toString()))));
//		verify(repository).deleteDirectory(expectedDestination.asString());
//		verify(repository).uploadDirectory(expectedRepositoryDirectory, expectedDestination);
//	}
//
//	/**
//	 * Tests {@link AWSP2Mojo#execute()} on a snapshot deployment with dedicated buckets.
//	 *
//	 * @throws MojoFailureException  Unexpected.
//	 * @throws MalformedURLException Unexpected.
//	 */
//	@Test
//	public void testExecute_DedicatedBucket()
//			throws MojoFailureException, MalformedURLException {
//		final File expectedRepositoryDirectory = new File(outputDirectory, REPOSITORY_DIR);
//		final BucketPath expectedDestination = new BucketPath().append(targetSiteDirectory);
//		final URL expectedUrl = new URL("http", "example.com", "mock");
//		when(repository.uploadDirectory(expectedRepositoryDirectory, expectedDestination)).thenReturn(expectedUrl);
//		when(repository.getHostingUrl(expectedDestination.asString())).thenReturn(expectedUrl.toString());
//
//		mojo.execute();
//
//		assertThat(logger.getLoggingEvents(), is(singletonList(info("Upload complete: {}", expectedUrl.toString()))));
//		verify(repository).deleteDirectory(expectedDestination.asString());
//		verify(repository).uploadDirectory(expectedRepositoryDirectory, expectedDestination);
//	}
//
//	/**
//	 * Tests that {@link AWSP2Mojo#execute()} does not write a landing page when the landing page flag is set to
//	 * {@code false}.
//	 *
//	 * @throws MojoFailureException Unexpected.
//	 * @throws IOException          Unexpected.
//	 */
//	@Test
//	public void testExecute_DoNotGenerateLandingPage()
//			throws MojoFailureException, IOException {
//		final File expectedRepositoryDirectory = new File(outputDirectory, REPOSITORY_DIR);
//		final BucketPath expectedDestination = new BucketPath().append(targetSiteDirectory);
//		final URL expectedUrl = new URL("http", "example.com", "mock");
//		when(repository.uploadDirectory(expectedRepositoryDirectory, expectedDestination)).thenReturn(expectedUrl);
//		when(repository.getHostingUrl(expectedDestination.asString())).thenReturn(expectedUrl.toString());
//
//		mojo.execute();
//
//		assertThat(logger.getLoggingEvents(), is(singletonList(info("Upload complete: {}", expectedUrl.toString()))));
//		verify(landingPageGenerator, never()).write(eq(bucketName), eq(artifactId), any(Date.class));
//	}
//
//	/**
//	 * Tests that {@link AWSP2Mojo#execute()} generates a landing page when the landing page flag is set to {@code
//	 * true}.
//	 *
//	 * @throws MojoFailureException Unexpected.
//	 * @throws IOException          Unexpected.
//	 */
//	@Test
//	public void testExecute_GenerateLandingPage()
//			throws MojoFailureException, IOException {
//		mojo.setGenerateLandingPage(true);
//		final File expectedRepositoryDirectory = new File(outputDirectory, REPOSITORY_DIR);
//		final BucketPath expectedDestination = new BucketPath().append(targetSiteDirectory);
//		final URL expectedUrl = new URL("http", "example.com", "mock");
//		when(repository.uploadDirectory(expectedRepositoryDirectory, expectedDestination)).thenReturn(expectedUrl);
//		when(repository.getHostingUrl(expectedDestination.asString())).thenReturn(expectedUrl.toString());
//
//		mojo.execute();
//
//		assertThat(logger.getLoggingEvents(), is(singletonList(info("Upload complete: {}", expectedUrl.toString()))));
//		verify(landingPageGenerator).write(eq(bucketName), eq(artifactId), any(Date.class));
//	}
//
//}
