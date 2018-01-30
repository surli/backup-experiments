package com.avojak.mojo.aws.p2.maven.plugin.s3.repository;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;

import java.io.File;
import java.util.List;

/**
 * Provides methods to interface with an S3 Bucket in a repository pattern.
 */
public interface S3BucketRepository {

	/**
	 * Uploads a file into the given location in the bucket. The destination path should refer to the desired name of
	 * the file in the bucket.
	 * <p>
	 * For example, uploading the file: <pre>target/dir/example.xml</pre>
	 * <p>
	 * The expected method call would be: <pre>uploadFile(new File("target/dir/example.xml"), new
	 * BucketPath("some/directory/example.xml"))</pre>
	 * <p>
	 * The destination path need not exist in the bucket prior to calling this method. Any non-existent folders will be
	 * created as needed.
	 * <p>
	 * In the event that a file at the same destination path already exists, that file will be overwritten.
	 *
	 * @param src  The source {@link File} to upload. Cannot be {@code null}.
	 * @param dest The destination {@link BucketPath} location within the bucket. Cannot be {@code null}.
	 *
	 * @return The {@link String} key of the file which was uploaded, or {@code null} if no file was uploaded.
	 */
	String uploadFile(final File src, final BucketPath dest);

	/**
	 * Uploads a directory and its contents into the given location in the bucket. The destination path should refer to
	 * the desired name of the folder in the bucket.
	 * <p>
	 * For example, uploading the directory: <pre>target/dir/</pre>
	 * <p>
	 * The expected method call would be: <pre>uploadFile(new File("target/dir/"), new
	 * BucketPath("some/directory/"))</pre>
	 * <p>
	 * The destination path need not exist in the bucket prior to calling this method. Any non-existent folders will be
	 * created as needed.
	 * <p>
	 * In the event that a folder at the same destination path already exists, files with matching names will be
	 * overwritten, and all other files will be left unchanged.
	 * <p>
	 * Empty directories will be ignored.
	 *
	 * @param srcDir The source directory {@link File} to upload. Cannot be {@code null}.
	 * @param dest   The destination {@link BucketPath} location within the bucket. Cannot be {@code null}.
	 *
	 * @return A non-{@link null}, possibly empty {@link Trie} of the directory which was uploaded.
	 */
	Trie<String, String> uploadDirectory(final File srcDir, final BucketPath dest);

	/**
	 * Deletes a "directory" at the given prefix. As there are no actual directories in S3, this method deletes all
	 * objects whose key matches the given prefix.
	 * <p>
	 * There is no consequence for attempting to delete non-existent objects.
	 *
	 * @param prefix The key prefix. Cannot be {@code null} or empty.
	 */
	void deleteDirectory(final String prefix);

	/**
	 * Enumerates all {@link S3Object} objects behind the given prefix.
	 *
	 * @param prefix The {@link S3Object} prefix {@code String}. Cannot be {@code null} or empty.
	 *
	 * @return The non-{@code null}, possibly empty {@link List} of {@link S3Object} objects.
	 */
	List<S3ObjectSummary> enumerate(final String prefix);

	/**
	 * Gets the AWS static website hosting URL for the object with the given key. If no key is provided, the URL
	 * returned will point to the root of the bucket.
	 *
	 * @return The static website hosting URL for the given object key, or the root of the bucket if no key is provided.
	 */
	String getHostingUrl(final String key);

}
