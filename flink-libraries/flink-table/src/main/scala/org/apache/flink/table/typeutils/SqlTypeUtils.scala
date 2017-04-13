/*
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
package org.apache.flink.table.typeutils

import org.apache.calcite.rel.`type`.{RelDataType, RelDataTypeFactory}
import org.apache.calcite.sql.`type`.SqlTypeName
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.table.api.Types._
import org.apache.flink.table.api.TableException

object SqlTypeUtils {

  def createSqlType(
    typeFactory: RelDataTypeFactory,
    aggReturnType: TypeInformation[_]): RelDataType = {

    aggReturnType match {
      case STRING => typeFactory.createSqlType(SqlTypeName.VARCHAR)
      case BOOLEAN => typeFactory.createSqlType(SqlTypeName.BOOLEAN)
      case BYTE => typeFactory.createSqlType(SqlTypeName.BINARY)
      case SHORT => typeFactory.createSqlType(SqlTypeName.INTEGER)
      case INT => typeFactory.createSqlType(SqlTypeName.INTEGER)
      case LONG => typeFactory.createSqlType(SqlTypeName.BIGINT)
      case FLOAT => typeFactory.createSqlType(SqlTypeName.FLOAT)
      case DOUBLE => typeFactory.createSqlType(SqlTypeName.DOUBLE)
      case DECIMAL => typeFactory.createSqlType(SqlTypeName.DECIMAL)
      case _ =>
        throw TableException(s"Can not create a sql type by [${aggReturnType}]")
    }
  }

}
