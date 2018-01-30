package com.avojak.mojo.aws.p2.maven.plugin;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.avojak.mojo.aws.p2.maven.plugin.index.formatter.HtmlLandingPageFormatter;
import com.avojak.mojo.aws.p2.maven.plugin.index.generator.LandingPageGenerator;
import com.avojak.mojo.aws.p2.maven.plugin.index.writer.HtmlLandingPageWriter;
import com.avojak.mojo.aws.p2.maven.plugin.util.resource.ResourceUtil;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepository;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepositoryFactory;
import com.avojak.mojo.aws.p2.maven.plugin.util.file.FileFactory;
import com.avojak.mojo.aws.p2.maven.plugin.util.file.FileWriterFactory;
import com.google.common.html.HtmlEscapers;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Deploys a p2 update site to an AWS S3 bucket.
 *
 * @author Andrew Vojak
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY, requiresOnline = true)
public class AWSP2Mojo extends AbstractMojo {

	private static final String REPOSITORY_DIR = "repository";
	private static final String SNAPSHOT_QUALIFIER = "-SNAPSHOT";
	private static final String SNAPSHOT_DIR = "snapshot";
	private static final String RELEASE_DIR = "release";

	// Attempt to use an arbitrary default region for creating the client, even if it's incorrect.
	// See: https://github.com/aws/aws-sdk-java/issues/1142#issuecomment-300308009
	private static final String DEFAULT_REGION = "us-east-1";

	private static final Logger LOGGER = LoggerFactory.getLogger(AWSP2Mojo.class);

	private final S3BucketRepositoryFactory repositoryFactory;
	private final LandingPageGenerator landingPageGenerator;

	/**
	 * The name of the S3 bucket to host the p2 site.
	 * <p>
	 * <em>This value is required.</em>
	 */
	@Parameter(name = "bucket", property = "aws-p2.bucket", required = true)
	private String bucket;

	/**
	 * The directory within the bucket where the site will be placed. The default location is:
	 * <pre>
	 *     ${project.artifactId}/${project.version}
	 * </pre>
	 */
	@Parameter(name = "targetSiteDirectory", property = "aws-p2.targetSiteDirectory", defaultValue = "${project.artifactId}/${project.version}")
	private String targetSiteDirectory;

	/**
	 * Whether or not to deploy snapshot sites. The default value is {@code true}.
	 */
	@Parameter(name = "deploySnapshots", property = "aws-p2.deploySnapshots", defaultValue = "true")
	private boolean deploySnapshots;

	/**
	 * Whether or not to skip execution. The default value is {@code false}.
	 */
	@Parameter(name = "skip", property = "aws-p2.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Whether or not dedicated buckets are used for snapshot and release sites. If {@code false}, the site will be
	 * placed in either a snapshot or release directory in the bucket. For example, if {@code false}, a snapshot site
	 * will be placed in:
	 * <pre>
	 *     snapshot/targetSiteDirectory
	 * </pre>
	 * And a release version site will be placed in:
	 * <pre>
	 *     release/targetSiteDirectory
	 * </pre>
	 * Consumers may instead choose host separate buckets for release and snapshot sites. The default value is {@code
	 * false}.
	 */
	@Parameter(name = "dedicatedBuckets", property = "aws-p2.dedicatedBuckets", defaultValue = "false")
	private boolean dedicatedBuckets;

	/**
	 * Whether or not to write a web-accessible landing page for the update site. If {@code true}, the HTML landing
	 * page will be created and uploaded into the root of the update site.
	 */
	@Parameter(name = "generateLandingPage", property = "aws-p2.generateLandingPage", defaultValue = "true")
	private boolean generateLandingPage;

	/**
	 * The top level output directory of the build. The default value is:
	 * <pre>
	 *     ${project.build.directory}
	 * </pre>
	 * <em>This value is not configurable by consumers.</em>
	 */
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File outputDirectory;

	/**
	 * The Maven project. The default value is:
	 * <pre>
	 *     ${project}
	 * </pre>
	 * <em>This value is not configurable by consumers.</em>
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	// TODO: Allow additional metadata for putObject call?

	/**
	 * Default constructor invoked at runtime.
	 */
	public AWSP2Mojo() throws IOException {
		this(new S3BucketRepositoryFactory(AmazonS3ClientBuilder.standard().withRegion(DEFAULT_REGION)
						.withForceGlobalBucketAccessEnabled(true)
						.withCredentials(new DefaultAWSCredentialsProviderChain())
						.build()),
				new LandingPageGenerator(new HtmlLandingPageFormatter(HtmlEscapers.htmlEscaper()),
						new HtmlLandingPageWriter(new FileFactory(), new FileWriterFactory())));
	}

	/**
	 * Constructor.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param repositoryFactory    The {@link S3BucketRepositoryFactory}.
	 * @param landingPageGenerator
	 */
	AWSP2Mojo(final S3BucketRepositoryFactory repositoryFactory, final LandingPageGenerator landingPageGenerator) {
		this.repositoryFactory = repositoryFactory;
		this.landingPageGenerator = landingPageGenerator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {
		if (skip) {
			LOGGER.info(ResourceUtil.getString(getClass(), "info.skippingExecution"));
			return;
		}
		final boolean isSnapshotVersion = isSnapshotVersion(project.getVersion());
		if (isSnapshotVersion && !deploySnapshots) {
			LOGGER.info(ResourceUtil.getString(getClass(), "info.skippingSnapshot"));
			return;
		}

		final S3BucketRepository repository;
		try {
			repository = repositoryFactory.create(bucket);
		} catch (final BucketDoesNotExistException e) {
			throw new MojoFailureException("The specified bucket does not exist", e);
		}

		final File repositoryDirectory = new File(outputDirectory, REPOSITORY_DIR);
		final BucketPath destination = new BucketPath();

		// If not using dedicated buckets, then specify release/snapshot in the path
		if (!dedicatedBuckets) {
			if (isSnapshotVersion) {
				destination.append(SNAPSHOT_DIR);
			} else {
				destination.append(RELEASE_DIR);
			}
		}

		destination.append(targetSiteDirectory);

		repository.deleteDirectory(destination.asString());
		final Trie<String, String> content = repository.uploadDirectory(repositoryDirectory, destination);
		// TODO: Log a message before this
		content.log();

		// Generate an HTML landing page if specified
		if (generateLandingPage) {
			try {
				final BucketPath landingPageDestination = new BucketPath(destination).append("index.html");
				final File index = landingPageGenerator.generate(bucket, project.getArtifactId(), content, new Date());
				repository.uploadFile(index, landingPageDestination);
			} catch (IOException e) {
				throw new MojoFailureException("Unable to write landing page", e);
			}
		}

		final String url = repository.getHostingUrl(destination.asString());
		LOGGER.info(ResourceUtil.getString(getClass(), "info.uploadComplete"), url);
	}

	/**
	 * Checks whether or not a version is a snapshot version. From Maven documentation, a version is a snapshot version
	 * if it contains the qualifier "-SNAPSHOT".
	 */
	private boolean isSnapshotVersion(final String version) {
		return version != null && version.trim().endsWith(SNAPSHOT_QUALIFIER);
	}

	/**
	 * Sets the Maven project.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param project The {@link MavenProject}.
	 */
	void setProject(final MavenProject project) {
		this.project = project;
	}

	/**
	 * Sets the bucket name.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param bucket The bucket name.
	 */
	void setBucket(final String bucket) {
		this.bucket = bucket;
	}

	/**
	 * Sets the target site directory.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param targetSiteDirectory The target site directory.
	 */
	void setTargetSiteDirectory(final String targetSiteDirectory) {
		this.targetSiteDirectory = targetSiteDirectory;
	}

	/**
	 * Sets the deploy snapshots flag.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param deploySnapshots The deploy snapshots flag.
	 */
	void setDeploySnapshots(final boolean deploySnapshots) {
		this.deploySnapshots = deploySnapshots;
	}

	/**
	 * Sets the skip execution flag.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param skip The skip execution flag.
	 */
	void setSkip(final boolean skip) {
		this.skip = skip;
	}

	/**
	 * Sets the dedicated buckets flag.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param dedicatedBuckets The dedicated buckets flag.
	 */
	void setDedicatedBuckets(final boolean dedicatedBuckets) {
		this.dedicatedBuckets = dedicatedBuckets;
	}

	/**
	 * Sets the write landing page flag.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param generateLandingPage The write landing page flag.
	 */
	void setGenerateLandingPage(final boolean generateLandingPage) {
		this.generateLandingPage = generateLandingPage;
	}

	/**
	 * Sets the output directory.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param outputDirectory The output directory {@link File}.
	 */
	void setOutputDirectory(final File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}
