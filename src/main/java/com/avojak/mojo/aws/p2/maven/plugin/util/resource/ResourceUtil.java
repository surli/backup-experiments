package com.avojak.mojo.aws.p2.maven.plugin.util.resource;

import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility to read resources from the "resources" bundle.
 */
public class ResourceUtil {

	private static final String BUNDLE = "resources";

	/**
	 * Gets the resource string for the given class and key.
	 *
	 * @param clazz The {@link Class} for the key. Cannot be null.
	 * @param key   The unqualified resource key. Cannot be null or empty.
	 *
	 * @return The {@link String} resource.
	 *
	 * @throws java.util.MissingResourceException if no resource for the given key can be found.
	 */
	public static String getString(final Class clazz, final String key) {
		checkNotNull(clazz, "clazz cannot be null");
		checkNotNull(key, "key cannot be null");
		checkArgument(!key.trim().isEmpty(), "key cannot be empty");
		final ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE);
		return resourceBundle.getString(clazz.getName() + "." + key);
	}

}
