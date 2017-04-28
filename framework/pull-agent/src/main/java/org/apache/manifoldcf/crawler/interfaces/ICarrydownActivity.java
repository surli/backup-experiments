/* $Id$ */

/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.manifoldcf.crawler.interfaces;

import org.apache.manifoldcf.core.interfaces.*;
import org.apache.manifoldcf.agents.interfaces.*;

/** This interface describes activities related to carry-down information.
*/
public interface ICarrydownActivity
{
  /** Retrieve data passed from parents to a specified child document.
  *@param localIdentifier is the document identifier of the document we want the recorded data for.
  *@param dataName is the name of the data items to retrieve.
  *@return an array containing the unique data values passed from ALL parents.  Note that these are in no particular order, and there will not be any duplicates.
  */
  public String[] retrieveParentData(String localIdentifier, String dataName)
    throws ManifoldCFException;

  /** Retrieve data passed from parents to a specified child document.
  *@param localIdentifier is the document identifier of the document we want the recorded data for.
  *@param dataName is the name of the data items to retrieve.
  *@return an array containing the unique data values passed from ALL parents.  Note that these are in no particular order, and there will not be any duplicates.
  */
  public CharacterInput[] retrieveParentDataAsFiles(String localIdentifier, String dataName)
    throws ManifoldCFException;

}
