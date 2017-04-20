/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.plan;

import com.google.common.collect.ImmutableList;

import java.util.List;

/** RelOptReferentialConstraint base implementation. */
public class RelOptReferentialConstraintImpl implements RelOptReferentialConstraint {

  private final List<String> foreignKeyTableQualifiedName;
  private final List<Integer> foreignKeyColumns;
  private final List<String> parentTableQualifiedName;
  private final List<Integer> uniqueKeyColumns;

  private RelOptReferentialConstraintImpl(List<String> foreignKeyTableQualifiedName,
      List<Integer> foreignKeyColumns,
      List<String> parentTableQualifiedName,
      List<Integer> uniqueKeyColumns) {
    assert foreignKeyColumns.size() == uniqueKeyColumns.size();
    this.foreignKeyTableQualifiedName = ImmutableList.copyOf(foreignKeyTableQualifiedName);
    this.foreignKeyColumns = ImmutableList.copyOf(foreignKeyColumns);
    this.parentTableQualifiedName = ImmutableList.copyOf(parentTableQualifiedName);
    this.uniqueKeyColumns = ImmutableList.copyOf(uniqueKeyColumns);
  }

  @Override public List<String> getForeignKeyTableQualifiedName() {
    return foreignKeyTableQualifiedName;
  }

  @Override public List<String> getParentTableQualifiedName() {
    return parentTableQualifiedName;
  }

  @Override public List<Integer> getForeignKeyColumns() {
    return foreignKeyColumns;
  }

  @Override public List<Integer> getUniqueKeyColumns() {
    return uniqueKeyColumns;
  }

  @Override public int getNumColumns() {
    return foreignKeyColumns.size();
  }

  public static RelOptReferentialConstraintImpl of(
      List<String> foreignKeyTableQualifiedName, List<Integer> foreignKeyColumns,
      List<String> parentTableQualifiedName, List<Integer> uniqueKeyColumns) {
    return new RelOptReferentialConstraintImpl(
        foreignKeyTableQualifiedName, foreignKeyColumns,
        parentTableQualifiedName, uniqueKeyColumns);
  }

  @Override public String toString() {
    return "{ { " + foreignKeyTableQualifiedName + ", " + foreignKeyColumns + "}, {"
        + parentTableQualifiedName + ", " + uniqueKeyColumns + "} }";
  }
}

// End RelOptReferentialConstraintImpl.java
