/*
 * Copyright 2017 SPF4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.jdiff;


import java.io.IOException;
import java.io.File;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Zoltan Farkas
 */
public class JDiffRunnerTest {

  @Test
  public void testJDiff() throws VersionRangeResolutionException,
          ArtifactResolutionException, DependencyResolutionException, JavadocExecutionException, IOException {
    JDiffRunner jDiffRunner = new JDiffRunner();
    File destination = new File("target/jdiff");
    jDiffRunner.runDiffBetweenReleases("org.spf4j", "spf4j-core", "[8.3,]", destination,  10);
    jDiffRunner.writeChangesIndexHtml(destination, "changes.html");
    Assert.assertTrue(new File(destination, "changes.html").exists());
  }


}
