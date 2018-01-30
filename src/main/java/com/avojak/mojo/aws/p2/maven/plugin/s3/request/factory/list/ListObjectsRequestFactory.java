package com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.list;

import com.amazonaws.services.s3.model.ListObjectsRequest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class to create instances of {@link ListObjectsRequest}.
 */
public class ListObjectsRequestFactory {

	private final String bucketName;

	/**
	 * Constructor.
	 *
	 * @param bucketName The name of the bucket for which requests are created. Cannot be {@code null} or empty.
	 */
	public ListObjectsRequestFactory(final String bucketName) {
		this.bucketName = checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
	}

	/**
	 * Creates and returns a new {@link ListObjectsRequest}.
	 *
	 * @param prefix The object prefix.
	 *
	 * @return A new, non-{@code null} {@link ListObjectsRequest}.
	 */
	public ListObjectsRequest create(final String prefix) {
		final ListObjectsRequest request = new ListObjectsRequest();
		request.setBucketName(bucketName);
		request.setPrefix(prefix);
		return request;
	}

}
