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
package io.scigraph.neo4j;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.neo4j.graphdb.RelationshipType;

public class RelationshipMapTest {

  @Test
  public void test() {
    RelationshipMap map = new RelationshipMap();
    assertThat(map.containsKey(0L, 1L, RelationshipType.withName("foo")), is(false));
    map.put(0L, 1L, RelationshipType.withName("foo"), 1L);
    assertThat(map.containsKey(0L, 1L, RelationshipType.withName("foo")), is(true));
    assertThat(map.get(0L, 1L, RelationshipType.withName("foo")), is(1L));
  }

}
