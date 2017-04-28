/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

/**
 * Application Programming Interface definition for Apache Airavata Services.
 *   this parent thrift file is contains all service interfaces. The data models are
 *   described in respective thrift files.
*/

include "airavata_errors.thrift"
include "security_model.thrift"
include "../data-models/airavata_data_models.thrift"
include "../data-models/experiment-catalog-models/status_models.thrift"
include "../data-models/experiment-catalog-models/job_model.thrift"
include "../data-models/experiment-catalog-models/experiment_model.thrift"
include "../data-models/experiment-catalog-models/workspace_model.thrift"
include "../data-models/experiment-catalog-models/scheduling_model.thrift"
include "../data-models/app-catalog-models/application_io_models.thrift"
include "../data-models/app-catalog-models/application_deployment_model.thrift"
include "../data-models/app-catalog-models/application_interface_model.thrift"
include "../data-models/resource-catalog-models/compute_resource_model.thrift"
include "../data-models/resource-catalog-models/storage_resource_model.thrift"
include "../data-models/resource-catalog-models/gateway_resource_profile_model.thrift"
include "../data-models/resource-catalog-models/data_movement_models.thrift"
include "../data-models/workflow-models/workflow_data_model.thrift"
include "../data-models/replica-catalog-models/replica_catalog_models.thrift"
include "../data-models/user-group-models/group_manager_model.thrift"

namespace java org.apache.airavata.api
namespace php Airavata.API
namespace cpp apache.airavata.api
namespace perl ApacheAiravataAPI
namespace py apache.airavata.api
namespace js ApacheAiravataAPI

/**
 * Airavata Interface Versions depend upon this Thrift Interface File. When making changes, please edit the
 *  Version Constants according to Semantic Versioning Specification (SemVer) http://semver.org.
 *
 * Note: The Airavata API version may be different from the Airavata software release versions.
 *
 * The Airavata API version is composed as a dot delimited string with major, minor, and patch level components.
 *
 *  - Major: Incremented for backward incompatible changes. An example would be changes to interfaces.
 *  - Minor: Incremented for backward compatible changes. An example would be the addition of a new optional methods.
 *  - Patch: Incremented for bug fixes. The patch level should be increased for every edit that doesn't result
 *              in a change to major/minor version numbers.
 *
*/

service AiravataAppCatalogService {

/*
 *
 * API definitions for App Catalog related operations
 *
*/

/*
 *
 * Application Module
 *  A specific computational application. Many applications, particularly scientific applications
 *  are really a suite of applications or encompass of an ecosystem. For instance, Amber is referred to dozens of binaries.
 *  WRF is referred for an ecosystem of applications. In this context, we refer to module as a single binary.
 *
 * Note: A module has to be defined before a deployment can be registered.
 *
*/

  /**
   *
   * Register a Application Module.
   *
   * @gatewayId
   *    ID of the gateway which is registering the new Application Module.
   *
   * @param applicationModule
   *    Application Module Object created from the datamodel.
   *
   * @return appModuleId
   *   Returns the server-side generated airavata appModule globally unique identifier.
   *
  */
  string registerApplicationModule(1: required security_model.AuthzToken authzToken,
                        2: required string gatewayId,
                        3: required application_deployment_model.ApplicationModule applicationModule)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch a Application Module.
   *
   * @param appModuleId
   *   The unique identifier of the application module required
   *
   * @return applicationModule
   *   Returns an Application Module Object.
   *
  */
  application_deployment_model.ApplicationModule getApplicationModule(1: required security_model.AuthzToken authzToken,
                2: required string appModuleId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Update a Application Module.
   *
   * @param appModuleId
   *   The identifier for the requested application module to be updated.
   *
   * @param applicationModule
   *    Application Module Object created from the datamodel.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateApplicationModule(1: required security_model.AuthzToken authzToken,
            2: required string appModuleId,
            3: required application_deployment_model.ApplicationModule applicationModule)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch all Application Module Descriptions.
   *
   * @param gatewayId
   *    ID of the gateway which need to list all available application deployment documentation.
   *
   * @return list
   *    Returns the list of all Application Module Objects.
   *
  */
  list<application_deployment_model.ApplicationModule> getAllAppModules (1: required security_model.AuthzToken authzToken,
                2: required string gatewayId)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Delete an Application Module.
   *
   * @param appModuleId
   *   The identifier of the Application Module to be deleted.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteApplicationModule(1: required security_model.AuthzToken authzToken,
                               2: required string appModuleId)
         	throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

/*
 *
 * Application Deployment
 *  Registers a deployment of an Application Module on a Compute Resource.
 *
*/

  /**
   *
   * Register an Application Deployment.
   *
   * @param gatewayId
   *    ID of the gateway which is registering the new Application Deployment.
   *
   * @param applicationDeployment
   *    Application Module Object created from the datamodel.
   *
   * @return appDeploymentId
   *   Returns a server-side generated airavata appDeployment globally unique identifier.
   *
  */
  string registerApplicationDeployment(1: required security_model.AuthzToken authzToken,
                2: required string gatewayId,
                3: required application_deployment_model.ApplicationDeploymentDescription applicationDeployment)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch a Application Deployment.
   *
   * @param appDeploymentId
   *   The identifier for the requested application module
   *
   * @return applicationDeployment
   *   Returns a application Deployment Object.
   *
  */
  application_deployment_model.ApplicationDeploymentDescription getApplicationDeployment(1: required security_model.AuthzToken authzToken,
                2: required string appDeploymentId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Update an Application Deployment.
   *
   * @param appDeploymentId
   *   The identifier of the requested application deployment to be updated.
   *
   * @param appDeployment
   *    Application Deployment Object created from the datamodel.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateApplicationDeployment(1: required security_model.AuthzToken authzToken,
            2: required string appDeploymentId,
            3: required application_deployment_model.ApplicationDeploymentDescription applicationDeployment)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Delete an Application Deployment.
   *
   * @param appDeploymentId
   *   The unique identifier of application deployment to be deleted.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteApplicationDeployment(1: required security_model.AuthzToken authzToken,
                    2: required string appDeploymentId)
         	throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch all Application Deployment Descriptions.
   *
   * @param gatewayId
   *    ID of the gateway which need to list all available application deployment documentation.
   *
   * @return list<applicationDeployment.
   *    Returns the list of all application Deployment Objects.
   *
  */
  list<application_deployment_model.ApplicationDeploymentDescription> getAllApplicationDeployments(1: required security_model.AuthzToken authzToken,
                2: required string gatewayId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   * Fetch a list of Deployed Compute Hosts.
   *
   * @param appModuleId
   *   The identifier for the requested application module
   *
   * @return list<string>
   *   Returns a list of Deployed Resources.
   *
  */
  list<string> getAppModuleDeployedResources(1: required security_model.AuthzToken authzToken, 2: required string appModuleId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

/*
 *
 * Application Interface
 *
*/

  /**
   *
   * Register a Application Interface.
   *
   * @param applicationInterface
   *    Application Module Object created from the datamodel.
   *
   * @return appInterfaceId
   *   Returns a server-side generated airavata application interface globally unique identifier.
   *
  */
  string registerApplicationInterface(1: required security_model.AuthzToken authzToken, 2: required string gatewayId,
                3: required application_interface_model.ApplicationInterfaceDescription applicationInterface)
    	throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase,
              4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Clone an Application Interface.
   *
   * @gatewayId
   *    The identifier for the gateway profile to be requested
   *
   * @param existingAppInterfaceID
   *    Identifier of the existing Application interface you wich to clone.
   *
   * @param newApplicationName
   *    Name for the new application interface.
   *
   * @return appInterfaceId
   *    Returns a server-side generated globally unique identifier for the newly cloned application interface.
   *
  */
  string cloneApplicationInterface(1: required security_model.AuthzToken authzToken,
                         2: string existingAppInterfaceID,
                         3: string newApplicationName,
                         4: string gatewayId)
    throws (1: airavata_errors.InvalidRequestException ire,
                  2: airavata_errors.AiravataClientException ace,
                  3: airavata_errors.AiravataSystemException ase,
                  4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch an Application Interface.
   *
   * @param appInterfaceId
   *   The identifier for the requested application interface.
   *
   * @return applicationInterface
   *   Returns an application Interface Object.
   *
  */
  application_interface_model.ApplicationInterfaceDescription getApplicationInterface(1: required security_model.AuthzToken authzToken,
                2: required string appInterfaceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Update a Application Interface.
   *
   * @param appInterfaceId
   *   The identifier of the requested application deployment to be updated.
   *
   * @param appInterface
   *    Application Interface Object created from the datamodel.
   *
   * @return status
   *   Returns a success/failure of the update.
   *
  */
  bool updateApplicationInterface(1: required security_model.AuthzToken authzToken,
            2: required string appInterfaceId,
            3: required application_interface_model.ApplicationInterfaceDescription applicationInterface)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Delete an Application Interface.
   *
   * @param appInterfaceId
   *   The identifier for the requested application interface to be deleted.
   *
   * @return status
   *   Returns a success/failure of the deletion.
   *
  */
  bool deleteApplicationInterface(1: required security_model.AuthzToken authzToken, 2: required string appInterfaceId)
         	throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase,
                   4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch name and ID of  Application Interface documents.
   *
   *
   * @return map<applicationId, applicationInterfaceNames>
   *   Returns a list of application interfaces with corresponsing ID's
   *
  */
  map<string, string> getAllApplicationInterfaceNames (1: required security_model.AuthzToken authzToken, 2: required string gatewayId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch all Application Interface documents.
   *
   *
   * @return map<applicationId, applicationInterfaceNames>
   *   Returns a list of application interfaces documents (Application Interface ID, name, description, Inputs and Outputs objects).
   *
  */
  list<application_interface_model.ApplicationInterfaceDescription> getAllApplicationInterfaces (1: required security_model.AuthzToken authzToken,
                2: required string gatewayId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch the list of Application Inputs.
   *
   * @param appInterfaceId
   *   The identifier of the application interface which need inputs to be fetched.
   *
   * @return list<application_interface_model.InputDataObjectType>
   *   Returns a list of application inputs.
   *
  */
  list<application_io_models.InputDataObjectType> getApplicationInputs(1: required security_model.AuthzToken authzToken,
                2: required string appInterfaceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch list of Application Outputs.
   *
   * @param appInterfaceId
   *   The identifier of the application interface which need outputs to be fetched.
   *
   * @return list<application_interface_model.OutputDataObjectType>
   *   Returns a list of application outputs.
   *
  */
  list<application_io_models.OutputDataObjectType> getApplicationOutputs(1: required security_model.AuthzToken authzToken,
                2: required string appInterfaceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

  /**
   *
   * Fetch a list of all deployed Compute Hosts for a given application interfaces.
   *
   * @param appInterfaceId
   *   The identifier for the requested application interface.
   *
   * @return map<computeResourceId, computeResourceName>
   *   A map of registered compute resource id's and their corresponding hostnames.
   *   Deployments of each modules listed within the interfaces will be listed.
   *
  */
  map<string, string> getAvailableAppInterfaceComputeResources(1: required security_model.AuthzToken authzToken, 2: required string appInterfaceId)
      	throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase,
                4: airavata_errors.AuthorizationException ae)

}