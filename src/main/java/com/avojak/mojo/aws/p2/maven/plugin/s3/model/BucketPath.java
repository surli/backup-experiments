package com.avojak.mojo.aws.p2.maven.plugin.s3.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Models a path within an S3 bucket.
 */
public class BucketPath {

	public static final char PATH_DELIM = '/';

	private final StringBuilder stringBuilder;

	/**
	 * Default no-args constructor.
	 */
	public BucketPath() {
		stringBuilder = new StringBuilder();
	}

	/**
	 * Constructs a {@link BucketPath} from an existing instance.
	 *
	 * @param bucketPath The existing instance of {@link BucketPath}. Cannot be {@code null}.
	 */
	public BucketPath(final BucketPath bucketPath) {
		this();
		checkNotNull(bucketPath, "bucketPath cannot be null");
		stringBuilder.append(bucketPath.asString());
	}

	/**
	 * Appends the given path.
	 *
	 * @param path The path. Cannot be {@code null} or empty.
	 *
	 * @return The current instance of {@link BucketPath}.
	 */
	public BucketPath append(final String path) {
		checkNotNull(path, "path cannot be null");
		checkArgument(!path.trim().isEmpty(), "path cannot be empty");

		String cleanedPath = path.trim().replace('\\', PATH_DELIM);

		// Remove a deliminator prefix or suffix
		if (cleanedPath.startsWith("" + PATH_DELIM)) {
			cleanedPath = cleanedPath.substring(1);
		}
		if (cleanedPath.endsWith("" + PATH_DELIM)) {
			cleanedPath = cleanedPath.substring(0, cleanedPath.length() - 1);
		}

		// If this isn't the first piece of the path, add a deliminator
		if (!stringBuilder.toString().isEmpty()) {
			stringBuilder.append(PATH_DELIM);
		}

		stringBuilder.append(cleanedPath);

		return this;
	}

	/**
	 * Returns the {@link BucketPath} as a {@code String}.
	 *
	 * @return The non-{@code null} {@code String} representation of the {@link BucketPath}.
	 */
	public String asString() {
		return stringBuilder.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final BucketPath that = (BucketPath) o;

		return stringBuilder.toString().equals(that.stringBuilder.toString());
	}

	@Override
	public int hashCode() {
		return stringBuilder.toString().hashCode();
	}

	@Override
	public String toString() {
		return "BucketPath{" + asString() + '}';
	}

}
