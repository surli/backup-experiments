package com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.delete;

import com.amazonaws.services.s3.model.DeleteObjectRequest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class to create instances of {@link DeleteObjectRequest}.
 */
public class DeleteObjectRequestFactory {

	private final String bucketName;

	/**
	 * Constructor.
	 *
	 * @param bucketName The name of the bucket for which requests are created. Cannot be {@code null} or empty.
	 */
	public DeleteObjectRequestFactory(final String bucketName) {
		this.bucketName = checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
	}

	/**
	 * Creates a new instance of {@link DeleteObjectRequest}.
	 *
	 * @param key The object key for which to create the request. Cannot be null or empty.
	 *
	 * @return The new, non-{@code null} instance of {@link DeleteObjectRequest}.
	 */
	public DeleteObjectRequest create(final String key) {
		checkNotNull(key, "key cannot be null");
		checkArgument(!key.trim().isEmpty(), "key cannot be empty");
		return new DeleteObjectRequest(bucketName, key);
	}

}
