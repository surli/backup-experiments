package org.testcontainers.utility;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.testcontainers.utility.PathUtils.recursiveDeleteDir;

/**
 * An abstraction over files and classpath resources aimed at encapsulating all the complexity of generating
 * a path that the Docker daemon is about to create a volume mount for.
 */
@Slf4j
public class MountableFile {

    private final String path;
    private String resolvedPath = null;

    private MountableFile(final String path) {
        this.path = path;
    }

    /**
     * Obtains a {@link MountableFile} corresponding to a resource on the classpath (including resources in JAR files)
     *
     * @param resourceName the classpath path to the resource
     * @return a {@link MountableFile} that may be used to obtain a mountable path
     */
    public static MountableFile forClasspathResource(@NotNull final String resourceName) {
        return new MountableFile(getClasspathResource(resourceName, new HashSet<>()).toString());
    }

    /**
     * Obtains a {@link MountableFile} corresponding to a file on the docker host filesystem.
     *
     * @param path the path to the resource
     * @return a {@link MountableFile} that may be used to obtain a mountable path
     */
    public static MountableFile forHostPath(@NotNull final String path) {
        return new MountableFile(new File(path).toURI().toString());
    }

    /**
     * Obtain a path that the Docker daemon should be able to use to volume mount a file/resource
     * into a container. If this is a classpath resource residing in a JAR, it will be extracted to
     * a temporary location so that the Docker daemon is able to access it.
     *
     * @return a volume-mountable path.
     */
    public String getMountablePath() {

        // Don't recompute if already resolved
        if (resolvedPath != null) {
            return resolvedPath;
        }

        if (path.contains(".jar!")) {
            resolvedPath = extractClassPathResourceToTempLocation(this.path);
        } else {
            resolvedPath = unencodeResourceURIToFilePath(path);
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            resolvedPath = PathUtils.createMinGWPath(resolvedPath);
        }

        return resolvedPath;
    }

    @NotNull
    private static URL getClasspathResource(@NotNull final String resourcePath, @NotNull final Set<ClassLoader> classLoaders) {

        // try context and system classloaders as well
        classLoaders.add(Thread.currentThread().getContextClassLoader());
        classLoaders.add(ClassLoader.getSystemClassLoader());
        classLoaders.add(MountableFile.class.getClassLoader());

        for (final ClassLoader classLoader : classLoaders) {
            URL resource = classLoader.getResource(resourcePath);
            if (resource != null) {
                return resource;
            }

            // Be lenient if an absolute path was given
            if (resourcePath.startsWith("/")) {
                resource = classLoader.getResource(resourcePath.replaceFirst("/", ""));
                if (resource != null) {
                    return resource;
                }
            }
        }

        throw new IllegalArgumentException("Resource with path " + resourcePath + " could not be found on any of these classloaders: " + classLoaders);
    }

    private static String unencodeResourceURIToFilePath(@NotNull final String resource) {
        try {
            // Convert any url-encoded characters (e.g. spaces) back into unencoded form
            return new URI(resource).getPath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Extract a file or directory tree from a JAR file to a temporary location.
     * This allows Docker to mount classpath resources as files.
     *
     * @param hostPath the path on the host, expected to be of the format 'file:/path/to/some.jar!/classpath/path/to/resource'
     * @return the path of the temporary file/directory
     */
    private String extractClassPathResourceToTempLocation(final String hostPath) {
        File tmpLocation = new File(".testcontainers-tmp-" + Base58.randomString(5));
        //noinspection ResultOfMethodCallIgnored
        tmpLocation.delete();

        String jarPath = hostPath.replaceFirst("jar:", "").replaceFirst("file:", "").replaceAll("!.*", "");
        String urldecodedJarPath;
        try {
            urldecodedJarPath = URLDecoder.decode(jarPath, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Could not URLDecode path with UTF-8 encoding: " + hostPath, e);
        }
        String internalPath = hostPath.replaceAll("[^!]*!/", "");

        try (JarFile jarFile = new JarFile(urldecodedJarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                if (name.startsWith(internalPath)) {
                    log.debug("Copying classpath resource(s) from {} to {} to permit Docker to bind",
                            hostPath,
                            tmpLocation);
                    copyFromJarToLocation(jarFile, entry, internalPath, tmpLocation);
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("Failed to process JAR file when extracting classpath resource: " + hostPath, e);
        }

        // Mark temporary files/dirs for deletion at JVM shutdown
        deleteOnExit(tmpLocation.toPath());

        return tmpLocation.getAbsolutePath();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void copyFromJarToLocation(final JarFile jarFile,
                                              final JarEntry entry,
                                              final String fromRoot,
                                              final File toRoot) throws IOException {

        String destinationName = entry.getName().replaceFirst(fromRoot, "");
        File newFile = new File(toRoot, destinationName);

        log.debug("Copying resource {} from JAR file {}",
                fromRoot,
                jarFile.getName());

        if (!entry.isDirectory()) {
            // Create parent directories
            newFile.mkdirs();
            newFile.delete();
            newFile.deleteOnExit();

            try (InputStream is = jarFile.getInputStream(entry)) {
                Files.copy(is, newFile.toPath());
            } catch (IOException e) {
                log.error("Failed to extract classpath resource " + entry.getName() + " from JAR file " + jarFile.getName(), e);
                throw e;
            }
        }
    }

    private void deleteOnExit(final Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> recursiveDeleteDir(path)));
    }
}
