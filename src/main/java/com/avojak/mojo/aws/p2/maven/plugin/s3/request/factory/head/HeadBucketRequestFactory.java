package com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.head;

import com.amazonaws.services.s3.model.HeadBucketRequest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class to create instances of {@link HeadBucketRequestFactory}.
 */
public class HeadBucketRequestFactory {

	private final String bucketName;

	/**
	 * Constructor.
	 *
	 * @param bucketName The name of the bucket for which requests are created. Cannot be {@code null} or empty.
	 */
	public HeadBucketRequestFactory(final String bucketName) {
		this.bucketName = checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
	}

	/**
	 * Creates and returns a new {@link HeadBucketRequest}.
	 *
	 * @return A new, non-{@code null} {@link HeadBucketRequest}.
	 */
	public HeadBucketRequest create() {
		return new HeadBucketRequest(bucketName);
	}

}
