package com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.put;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.avojak.mojo.aws.p2.maven.plugin.util.resource.ResourceUtil;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class to create instances of {@link PutObjectRequest}.
 */
public class PutObjectRequestFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(PutObjectRequestFactory.class);

	private final String bucketName;

	/**
	 * Constructor.
	 *
	 * @param bucketName The name of the bucket for which requests are created. Cannot be {@code null} or empty.
	 */
	public PutObjectRequestFactory(final String bucketName) {
		this.bucketName = checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
	}

	/**
	 * Creates a new instance of {@link PutObjectRequest}.
	 *
	 * @param file The {@link File} to be uploaded in the request. Cannot be {@code null}.
	 * @param dest The destination path in the bucket for the file. Cannot be {@code null} or empty.
	 *
	 * @return A new, non-{@code null} instance of {@link PutObjectRequest}.
	 *
	 * @throws ObjectRequestCreationException if there is an error while creating the request.
	 */
	public PutObjectRequest create(final File file, final String dest) throws ObjectRequestCreationException {
		checkNotNull(file, "file cannot be null");
		checkNotNull(dest, "dest cannot be null");
		checkArgument(!dest.trim().isEmpty(), "dest cannot be empty");

		final InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (final FileNotFoundException e) {
			throw new ObjectRequestCreationException(e);
		}
		final ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.length());
		// Need to set the content type to text/html for static hosting
		if (isFileHTML(file)) {
			LOGGER.debug(ResourceUtil.getString(getClass(), "debug.setHtmlContentType"), file.getName());
			metadata.setContentType("text/html");
		}
		return new PutObjectRequest(bucketName, dest, inputStream, metadata)
				.withCannedAcl(CannedAccessControlList.PublicRead);
	}

	private boolean isFileHTML(final File file) {
		return endsWithIgnoreCase(file.getName(), ".html");
	}

	private boolean endsWithIgnoreCase(final String str, final String suffix) {
		int suffixLength = suffix.length();
		return str.regionMatches(true, str.length() - suffixLength, suffix, 0, suffixLength);
	}

}
