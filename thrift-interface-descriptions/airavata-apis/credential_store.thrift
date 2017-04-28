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
   * Generate and Register SSH Key Pair with Airavata Credential Store.
   *
   * @param gatewayId
   *    The identifier for the requested Gateway.
   *
   * @param userName
   *    The User for which the credential should be registered. For community accounts, this user is the name of the
   *    community user name. For computational resources, this user name need not be the same user name on resoruces.
   *
   * @return airavataCredStoreToken
   *   An SSH Key pair is generated and stored in the credential store and associated with users or community account
   *   belonging to a Gateway.
   *
   **/
   string generateAndRegisterSSHKeys (1: required security_model.AuthzToken authzToken,
                    2: required string gatewayId,
                    3: required string userName)
           throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase)


 /**
   * Generate and Register Username PWD Pair with Airavata Credential Store.
   *
   * @param gatewayId
   *    The identifier for the requested Gateway.
   *
   * @param portalUserName
   *    The User for which the credential should be registered. For community accounts, this user is the name of the
   *    community user name. For computational resources, this user name need not be the same user name on resoruces.
   *
   * @param loginUserName
   *
   * @param password
   *
   * @return airavataCredStoreToken
   *   An SSH Key pair is generated and stored in the credential store and associated with users or community account
   *   belonging to a Gateway.
   *
   **/
  string registerPwdCredential (1: required security_model.AuthzToken authzToken,
                      2: required string gatewayId,
                      3: required string portalUserName,
                      4: required string loginUserName,
                      5: required string password,
                      6: required string description)
             throws (1: airavata_errors.InvalidRequestException ire,
                     2: airavata_errors.AiravataClientException ace,
                     3: airavata_errors.AiravataSystemException ase)

   /**
   * Get a Public Key by Providing the Token
   *
   * @param CredStoreToken
   *    Credential Store Token which you want to find the Public Key for.
   *
   * @param gatewayId
   *    This is the unique identifier of your gateway where the token and public key was generated from.
   *
   * @return publicKey
   *
   **/
   string getSSHPubKey (1: required security_model.AuthzToken authzToken,
                        2: required string airavataCredStoreToken,
                        3: required string gatewayId)
           throws (1: airavata_errors.InvalidRequestException ire,
                   2: airavata_errors.AiravataClientException ace,
                   3: airavata_errors.AiravataSystemException ase)

   /**
   *
   * Get all Public Keys of the Gateway
   *
   * @param CredStoreToken
   *    Credential Store Token which you want to find the Public Key for.
   *
   * @param gatewayId
   *    This is the unique identifier of your gateway where the token and public key was generated from.
   *
   * @return publicKey
   *
   **/
  map<string, string> getAllGatewaySSHPubKeys (1: required security_model.AuthzToken authzToken,
                                               2: required string gatewayId)
             throws (1: airavata_errors.InvalidRequestException ire,
                     2: airavata_errors.AiravataClientException ace,
                     3: airavata_errors.AiravataSystemException ase)


  map<string, string> getAllGatewayPWDCredentials (1: required security_model.AuthzToken authzToken,
                                                 2: required string gatewayId)
               throws (1: airavata_errors.InvalidRequestException ire,
                       2: airavata_errors.AiravataClientException ace,
                       3: airavata_errors.AiravataSystemException ase)

    /**
    *
    * Delete a Gateway
    *
    * @param gatewayId
    *    The gateway Id of the Gateway to be deleted.
    *
    * @return boolean
    *    Boolean identifier for the success or failure of the deletion operation.
    *
    **/
  bool deleteSSHPubKey (1: required security_model.AuthzToken authzToken,
                          2: required string airavataCredStoreToken,
                          3: required string gatewayId)
             throws (1: airavata_errors.InvalidRequestException ire,
                     2: airavata_errors.AiravataClientException ace,
                     3: airavata_errors.AiravataSystemException ase)


  bool deletePWDCredential (1: required security_model.AuthzToken authzToken,
                            2: required string airavataCredStoreToken,
                            3: required string gatewayId)
               throws (1: airavata_errors.InvalidRequestException ire,
                       2: airavata_errors.AiravataClientException ace,
                       3: airavata_errors.AiravataSystemException ase)
