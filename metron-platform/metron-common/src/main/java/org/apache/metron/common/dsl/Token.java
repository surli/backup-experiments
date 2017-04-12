/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.metron.common.dsl;

public class Token<T> {
  T value;
  Class<T> underlyingType;
  public Token(T value, Class<T> clazz) {
    this.value = value;
    this.underlyingType = clazz;
  }
  public T getValue() {
    return value;
  }
  public Class<T> getUnderlyingType() {
    return underlyingType;
  }

  @Override
  public String toString() {
    return "" + value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Token<?> token = (Token<?>) o;

    if (getValue() != null ? !getValue().equals(token.getValue()) : token.getValue() != null) return false;
    return getUnderlyingType() != null ? getUnderlyingType().equals(token.getUnderlyingType()) : token.getUnderlyingType() == null;

  }

  @Override
  public int hashCode() {
    int result = getValue() != null ? getValue().hashCode() : 0;
    result = 31 * result + (getUnderlyingType() != null ? getUnderlyingType().hashCode() : 0);
    return result;
  }
}
