package com.avojak.mojo.aws.p2.maven.plugin.s3.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.impl.S3BucketRepositoryImpl;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.delete.DeleteObjectRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.head.HeadBucketRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.list.ListObjectsRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.put.PutObjectRequestFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class to create instances of {@link S3BucketRepositoryImpl}.
 */
public class S3BucketRepositoryFactory {

	private final AmazonS3 client;

	/**
	 * Constructor.
	 *
	 * @param client The instance of {@link AmazonS3}. Cannot be {@code null}.
	 */
	public S3BucketRepositoryFactory(final AmazonS3 client) {
		this.client = checkNotNull(client, "client cannot be null");
	}

	/**
	 * Creates and returns a new instance of {@link S3BucketRepositoryImpl}.
	 *
	 * @param bucketName The name of the S3 bucket.
	 *
	 * @return A new, non-{@code null} instance of {@link S3BucketRepositoryImpl}.
	 *
	 * @throws BucketDoesNotExistException if the specified bucket does not exist.
	 */
	public S3BucketRepository create(final String bucketName) throws BucketDoesNotExistException {
		final PutObjectRequestFactory filePutObjectRequestFactory = new PutObjectRequestFactory(bucketName);
		final DeleteObjectRequestFactory deleteObjectRequestFactory = new DeleteObjectRequestFactory(bucketName);
		final ListObjectsRequestFactory listObjectsRequestFactory = new ListObjectsRequestFactory(bucketName);
		final HeadBucketRequestFactory headBucketRequestFactory = new HeadBucketRequestFactory(bucketName);
		return new S3BucketRepositoryImpl(client, bucketName, filePutObjectRequestFactory, deleteObjectRequestFactory,
				listObjectsRequestFactory, headBucketRequestFactory);
	}

}
