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
package org.apache.manifoldcf.crawler.connectors.cmis.tests;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/** Class representing an instance of CMIS Server
 */
public class CMISServer
{
  protected Server cmisServer = null;
  protected WebAppContext openCmisServerApi = null;
  
  public CMISServer(int port, String warPath)
  {
    cmisServer = new Server(port);
    cmisServer.setStopAtShutdown(true);

    //Initialize OpenCMIS Server bindings
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    cmisServer.setHandler(contexts);

    openCmisServerApi = new WebAppContext(warPath,"/chemistry-opencmis-server-inmemory");
    openCmisServerApi.setParentLoaderPriority(false);
    contexts.addHandler(openCmisServerApi);
  }
  
  public void start()
    throws Exception
  {
    cmisServer.start();
    boolean entered = false;
    
    while(cmisServer.isStarted() 
        && openCmisServerApi.isStarted()
        && !entered){
      entered = true;
      Thread.sleep(5000);
    }
  }
  
  public void stop()
    throws Exception
  {
  }

}
