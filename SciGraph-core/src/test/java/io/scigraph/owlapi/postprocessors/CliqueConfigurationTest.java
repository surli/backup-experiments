/**
 * Copyright (C) 2014 The SciGraph authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.scigraph.owlapi.postprocessors;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import io.scigraph.owlapi.loader.OwlLoadConfiguration;
import io.scigraph.owlapi.loader.OwlLoadConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class CliqueConfigurationTest {

  OwlLoadConfiguration loaderConfig;
  CliqueConfiguration cliqueConfiguration;

  @Before
  public void setup() throws URISyntaxException, JsonParseException, JsonMappingException,
      IOException {
    URL url = this.getClass().getResource("/cliqueConfiguration.yaml");
    File configFile = new File(url.getFile());

    assertThat(configFile.exists(), is(true));

    OwlLoadConfigurationLoader owlLoadConfigurationLoader =
        new OwlLoadConfigurationLoader(configFile);
    loaderConfig = owlLoadConfigurationLoader.loadConfig();
    cliqueConfiguration = loaderConfig.getCliqueConfiguration().get();
  }

  @Test
  public void parseRelationships() {
    Set<String> relationships = cliqueConfiguration.getRelationships();
    assertThat(relationships, containsInAnyOrder("sameAs", "equivalentClass"));
  }

  @Test
  public void parseLeaderAnnotation() {
    String leaderAnnotation = cliqueConfiguration.getLeaderAnnotation();
    assertThat(leaderAnnotation, equalTo("http://www.monarchinitiative.org/MONARCH_cliqueLeader"));
  }

  @Test
  public void parseLeaderForbiddenLabels() {
    Set<String> relationships = cliqueConfiguration.getLeaderForbiddenLabels();
    assertThat(relationships, containsInAnyOrder("anonymous"));
  }

  @Test
  public void parseLeaderPriority() {
    List<String> leaderPriority = cliqueConfiguration.getLeaderPriority();
    assertThat(
        leaderPriority,
        contains("http://www.ncbi.nlm.nih.gov/gene/", "http://www.ncbi.nlm.nih.gov/pubmed/",
            "http://purl.obolibrary.org/obo/NCBITaxon_", "http://identifiers.org/ensembl/",
            "http://purl.obolibrary.org/obo/DOID_", "http://purl.obolibrary.org/obo/HP_"));
  }

  @Test
  public void parseBatchCommitSize() {
    assertEquals(cliqueConfiguration.getBatchCommitSize(), 1);
  }


}
