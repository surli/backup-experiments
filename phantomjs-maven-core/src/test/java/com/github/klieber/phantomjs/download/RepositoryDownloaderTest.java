/*
 * Copyright (c) 2014 Kyle Lieber
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.klieber.phantomjs.download;

import com.github.klieber.phantomjs.archive.PhantomJSArchive;
import com.github.klieber.phantomjs.locate.RepositoryDetails;
import com.github.klieber.phantomjs.util.ArtifactBuilder;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PhantomJSArchive.class,ArtifactResult.class})
public class RepositoryDownloaderTest {

  @Mock
  private ArtifactBuilder artifactBuilder;

  @Mock
  private RepositorySystem repositorySystem;

  @Mock
  private RepositorySystemSession repositorySystemSession;

  @Mock
  private List<RemoteRepository> remoteRepositories;

  @Mock
  private RepositoryDetails repositoryDetails;

  @Mock
  private PhantomJSArchive phantomJSArchive;

  @Captor
  private ArgumentCaptor<ArtifactRequest> artifactRequestCaptor;

  @Mock
  private ArtifactResult artifactResult;

  @Mock
  private Artifact artifact;

  @Mock
  private File archiveFile;

  @InjectMocks
  private RepositoryDownloader repositoryDownloader;

  @Before
  public void before() {
    when(repositoryDetails.getRepositorySystem()).thenReturn(repositorySystem);
    when(repositoryDetails.getRemoteRepositories()).thenReturn(remoteRepositories);
    when(repositoryDetails.getRepositorySystemSession()).thenReturn(repositorySystemSession);
  }

  @Test
  public void shouldDownload() throws DownloadException, ArtifactResolutionException {
    when(artifactBuilder.createArtifact(phantomJSArchive)).thenReturn(artifact);
    when(repositorySystem.resolveArtifact(same(repositorySystemSession), artifactRequestCaptor.capture())).thenReturn(artifactResult);
    when(artifactResult.getArtifact()).thenReturn(artifact);
    when(artifact.getFile()).thenReturn(archiveFile);

    assertSame(archiveFile, repositoryDownloader.download(phantomJSArchive));

    ArtifactRequest request = artifactRequestCaptor.getValue();
    assertSame(artifact, request.getArtifact());
    assertSame(remoteRepositories, request.getRepositories());
  }

  @Test
  public void shouldHandleArtifactResolutionException() throws DownloadException, ArtifactResolutionException {
    when(artifactBuilder.createArtifact(phantomJSArchive)).thenReturn(artifact);
    when(repositorySystem.resolveArtifact(same(repositorySystemSession), artifactRequestCaptor.capture())).thenThrow(new ArtifactResolutionException(Collections.<ArtifactResult>emptyList()));

    catchException(repositoryDownloader).download(phantomJSArchive);
    assertThat(caughtException(), is(instanceOf(DownloadException.class)));
  }
}
