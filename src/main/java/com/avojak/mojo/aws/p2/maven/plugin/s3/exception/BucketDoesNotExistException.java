package com.avojak.mojo.aws.p2.maven.plugin.s3.exception;

import com.avojak.mojo.aws.p2.maven.plugin.util.resource.ResourceUtil;

import java.text.MessageFormat;

/**
 * Signals that an S3 bucket does not exist.
 */
public class BucketDoesNotExistException extends Exception {

	private static final long serialVersionUID = -2145123430228667602L;

	/**
	 * Default no-args constructor.
	 */
	public BucketDoesNotExistException() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param bucketName The name of the non-existent bucket.
	 */
	public BucketDoesNotExistException(final String bucketName) {
		super(getFormattedExceptionMessage(bucketName));
	}

	private static String getFormattedExceptionMessage(final String bucketName) {
		final String messageFormat = ResourceUtil.getString(BucketDoesNotExistException.class, "message");
		return MessageFormat.format(messageFormat, bucketName);
	}

}
