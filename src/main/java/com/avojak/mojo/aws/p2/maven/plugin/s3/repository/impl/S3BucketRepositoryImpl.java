package com.avojak.mojo.aws.p2.maven.plugin.s3.repository.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.HeadBucketResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.avojak.mojo.aws.p2.maven.plugin.util.resource.ResourceUtil;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl.BucketTrie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepository;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepositoryFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.delete.DeleteObjectRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.head.HeadBucketRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.list.ListObjectsRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.put.PutObjectRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link S3BucketRepository} to wrap an {@link AmazonS3} bucket. Instances should be created with
 * {@link S3BucketRepositoryFactory}.
 */
public class S3BucketRepositoryImpl implements S3BucketRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(S3BucketRepositoryImpl.class);

	private final AmazonS3 client;
	private final String bucketName;
	private final PutObjectRequestFactory putObjectRequestFactory;
	private final DeleteObjectRequestFactory deleteObjectRequestFactory;
	private final ListObjectsRequestFactory listObjectsRequestFactory;
	private final HeadBucketRequestFactory headBucketRequestFactory;

	private String bucketRegion;

	/**
	 * Constructor.
	 *
	 * @param client                     The {@link AmazonS3} client. Cannot be {@code null}.
	 * @param bucketName                 The name of the bucket that this repository represents. Cannot be {@code null}
	 *                                   or empty.
	 * @param putObjectRequestFactory    The {@link PutObjectRequestFactory} for {@link File files}. Cannot be {@code
	 *                                   null}.
	 * @param deleteObjectRequestFactory The {@link DeleteObjectRequestFactory}. Cannot be {@code null}.
	 * @param listObjectsRequestFactory  The {@link ListObjectsRequestFactory}. Cannot be {@code null}.
	 * @param headBucketRequestFactory   The {@link HeadBucketRequestFactory}. Cannot be {@code null}.
	 *
	 * @throws BucketDoesNotExistException if the specified bucketName does not refer to an existing bucket.
	 */
	public S3BucketRepositoryImpl(final AmazonS3 client, final String bucketName,
	                              final PutObjectRequestFactory putObjectRequestFactory,
	                              final DeleteObjectRequestFactory deleteObjectRequestFactory,
	                              final ListObjectsRequestFactory listObjectsRequestFactory,
	                              final HeadBucketRequestFactory headBucketRequestFactory) throws BucketDoesNotExistException {
		this.client = checkNotNull(client, "client cannot be null");
		this.bucketName = checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
		this.putObjectRequestFactory = checkNotNull(putObjectRequestFactory, "putObjectRequestFactory cannot be null");
		this.deleteObjectRequestFactory =
				checkNotNull(deleteObjectRequestFactory, "deleteObjectRequestFactory cannot be null");
		this.listObjectsRequestFactory =
				checkNotNull(listObjectsRequestFactory, "listObjectsRequestFactory cannot be null");
		this.headBucketRequestFactory =
				checkNotNull(headBucketRequestFactory, "headBucketRequestFactory cannot be null");
		if (!client.doesBucketExist(bucketName)) {
			throw new BucketDoesNotExistException(bucketName);
		}
	}

	@Override
	public String uploadFile(final File src, final BucketPath dest) {
		checkNotNull(src, "src cannot be null");
		checkNotNull(dest, "dest cannot be null");
		if (!src.exists() || !src.isFile()) {
			LOGGER.warn(ResourceUtil.getString(getClass(), "warn.fileNotAccessible"), src.getName());
			return null;
		}
		final String key = dest.asString();
		try {
			LOGGER.debug(ResourceUtil.getString(getClass(), "debug.uploadingFile"), key);
			client.putObject(putObjectRequestFactory.create(src, key));
		} catch (final ObjectRequestCreationException e) {
			LOGGER.error(ResourceUtil.getString(getClass(), "error.failedUploadRequestCreation"), e);
			return null;
		}
		return key;
	}

	@Override
	public Trie<String, String> uploadDirectory(final File srcDir, final BucketPath dest) {
		checkNotNull(srcDir, "srcDir cannot be null");
		checkNotNull(dest, "dest cannot be null");
		final String prefix = getPrefix(dest.asString());
		LOGGER.debug("Determined trie prefix: " + prefix);
		final Trie<String, String> content = prefix == null ? new BucketTrie() : new BucketTrie(prefix);
		uploadDirectory(srcDir, dest, content);
		return content;
	}

	/**
	 * Determines the trie prefix to use based on the destination in the bucket. The prefix will be the substring of the
	 * given destination beginning after the last path separator.
	 * <p>
	 * For example, given the destination "some/folder/subfolder", the prefix will be "some/folder". If there is no
	 * path separator in the given destination, then there will be no prefix, {@code null} will be returned.
	 */
	private String getPrefix(final String destination) {
		final int lastSeparatorIndex = destination.lastIndexOf(BucketPath.PATH_DELIM);
		if (lastSeparatorIndex == -1) {
			return null;
		}
		return destination.substring(0, lastSeparatorIndex);
	}

	/**
	 * Recursive helper method for uploading a directory.
	 */
	private void uploadDirectory(final File srcDir, final BucketPath dest, final Trie<String, String> trie) {
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			LOGGER.warn(ResourceUtil.getString(getClass(), "warn.directoryNotAccessible"), srcDir.getName());
			return;
		}
		final File[] directoryContents = srcDir.listFiles();
		if (directoryContents == null) {
			// Should never happen, since we already verify that srcDir is a directory
			LOGGER.warn(ResourceUtil.getString(getClass(), "warn.directoryContentsNull"), srcDir.getName());
			return;
		}
		// Skipping upload of an empty directory. Can easily be removed later, but probably don't want empty folders.
		if (directoryContents.length == 0) {
			LOGGER.debug(ResourceUtil.getString(getClass(), "debug.skippingEmptyDirectory"), srcDir.getName());
			return;
		}
		for (final File file : directoryContents) {
			final BucketPath nextDest = new BucketPath(dest).append(file.getName());
			if (file.isFile()) {
				uploadFile(file, nextDest);
				final String key = nextDest.asString();
				trie.insert(key, getHostingUrl(key));
			} else if (file.isDirectory()) {
				uploadDirectory(file, nextDest, trie);
			}
		}
	}

	@Override
	public void deleteDirectory(final String prefix) {
		checkNotNull(prefix, "prefix cannot be null");
		checkArgument(!prefix.trim().isEmpty(), "prefix cannot be empty");
		final List<S3ObjectSummary> objectSummaries = enumerate(prefix);
		for (final S3ObjectSummary summary : objectSummaries) {
			final String key = summary.getKey();
			final DeleteObjectRequest deleteObjectRequest = deleteObjectRequestFactory.create(key);
			LOGGER.debug(ResourceUtil.getString(getClass(), "debug.deleteExistingObject"), key);
			client.deleteObject(deleteObjectRequest);
		}
	}

	@Override
	public List<S3ObjectSummary> enumerate(final String prefix) {
		checkNotNull(prefix, "prefix cannot be null");
		checkArgument(!prefix.trim().isEmpty(), "prefix cannot be empty");
		final List<S3ObjectSummary> objectSummaries = new ArrayList<S3ObjectSummary>();
		final ListObjectsRequest listObjectsRequest = listObjectsRequestFactory.create(prefix);
		ObjectListing objectListing = client.listObjects(listObjectsRequest);
		List<S3ObjectSummary> currentSummaries = objectListing.getObjectSummaries();
		while (true) {
			objectSummaries.addAll(currentSummaries);
			// Ensure that we get all objects. Not all may be returned by the first call to listObjects()
			if (objectListing.isTruncated()) {
				objectListing = client.listNextBatchOfObjects(objectListing);
				currentSummaries = objectListing.getObjectSummaries();
			} else {
				break;
			}
		}
		return objectSummaries;
	}

	@Override
	public String getHostingUrl(final String key) {
		final String hostingUrlFormat = ResourceUtil.getString(getClass(), "hostingUrlFormat");
		return MessageFormat.format(hostingUrlFormat, bucketName, getBucketRegion(), key == null ? "" : key);
	}

	/**
	 * Gets the bucket region. If the region is needed, this method should be used to retrieve it. The actual lookup
	 * should only have to be done once, so the result is cached.
	 */
	private String getBucketRegion() {
		if (bucketRegion == null) {
			final HeadBucketRequest headBucketRequest = headBucketRequestFactory.create();
			final HeadBucketResult headBucketResult = client.headBucket(headBucketRequest);
			bucketRegion = headBucketResult.getBucketRegion();
		}
		return bucketRegion;
	}

//	@Override
//	public Trie<String, String> getBucketContent(String prefix) {
//		checkNotNull(prefix, "prefix cannot be null");
//		final Trie<String, String> bucketTrie = new BucketTrie();
//		final ObjectListing objectListing = client.listObjects(bucketName, prefix);
//		for (final S3ObjectSummary summary : objectListing.getObjectSummaries()) {
//			final String key = summary.getKey();
//			if (isDirectory(summary)) {
//				bucketTrie.insert(key, null);
//			} else {
//				bucketTrie.insert(key, getHostingUrl(key));
//			}
//		}
//		return bucketTrie;
//	}

	/**
	 * Returns whether or not the given {@link S3ObjectSummary} refers to a directory. A summary refers to a directory
	 * created in the AWS web console if the size is 0B, and the key ends with a '/'.
	 */
	private boolean isDirectory(final S3ObjectSummary summary) {
		return summary.getSize() == 0 && summary.getKey().endsWith("/");
	}

}
