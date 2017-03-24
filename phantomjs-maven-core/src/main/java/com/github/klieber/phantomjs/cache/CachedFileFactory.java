package com.github.klieber.phantomjs.cache;

import com.github.klieber.phantomjs.archive.PhantomJSArchive;
import com.github.klieber.phantomjs.util.ArtifactBuilder;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.LocalRepositoryManager;

import java.io.File;

public class CachedFileFactory {

  private final ArtifactBuilder artifactBuilder;

  public CachedFileFactory(ArtifactBuilder artifactBuilder) {
    this.artifactBuilder = artifactBuilder;
  }

  public File create(PhantomJSArchive phantomJSArchive, RepositorySystemSession repositorySystemSession) {
    Artifact artifact = artifactBuilder.createArtifact(phantomJSArchive);
    LocalRepositoryManager manager = repositorySystemSession.getLocalRepositoryManager();
    return new File(manager.getRepository().getBasedir(), manager.getPathForLocalArtifact(artifact));
  }
}
