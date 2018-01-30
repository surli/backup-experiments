package com.avojak.mojo.aws.p2.maven.plugin.s3.exception;

/**
 * Signals that there was an error while generating an object request.
 */
public class ObjectRequestCreationException extends Exception {

	private static final long serialVersionUID = -2133517635436883204L;

	/**
	 * Default constructor. To be used when the cause is unknown or non-existent. Equivalent to calling {@link
	 * ObjectRequestCreationException#ObjectRequestCreationException(Throwable)} with a {@code null} {@link Throwable}.
	 */
	public ObjectRequestCreationException() {
		this(null);
	}

	/**
	 * Constructor.
	 *
	 * @param throwable The {@link Throwable} which caused the error. May be {@code null}, indicating that the cause is
	 *                  non-existent or unknown.
	 */
	public ObjectRequestCreationException(final Throwable throwable) {
		super(throwable);
	}

}
