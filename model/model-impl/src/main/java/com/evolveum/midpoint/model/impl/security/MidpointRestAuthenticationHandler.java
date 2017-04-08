/*
 * Copyright (c) 2013-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.model.impl.security;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.evolveum.midpoint.model.impl.util.RestServiceUtil;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.evolveum.midpoint.model.api.AuthenticationEvaluator;
import com.evolveum.midpoint.model.impl.ModelRestService;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.security.api.ConnectionEnvironment;
import com.evolveum.midpoint.security.api.MidPointPrincipal;
import com.evolveum.midpoint.security.api.SecurityEnforcer;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author Katka Valalikova
 * @author Radovan Semancik
 */
public class MidpointRestAuthenticationHandler implements ContainerRequestFilter, ContainerResponseFilter {
	
	private static final Trace LOGGER = TraceManager.getTrace(MidpointRestAuthenticationHandler.class);
	 
	@Autowired(required=true)
	private AuthenticationEvaluator authenticationEvaluator;
	
	@Autowired(required = true)
	private SecurityEnforcer securityEnforcer;
			
	@Autowired(required = true)
	private SecurityHelper securityHelper;
	
	@Autowired(required=true)
	private TaskManager taskManager;
		
    public void handleRequest(Message m, ContainerRequestContext requestCtx) {
        AuthorizationPolicy policy = (AuthorizationPolicy)m.get(AuthorizationPolicy.class);
        
        if (policy == null){
        	requestCtx.abortWith(Response.status(401).header("WWW-Authenticate", "Basic").build());
        	return;
        }
        
        String enteredUsername = policy.getUserName();
        
        if (enteredUsername == null){
        	requestCtx.abortWith(Response.status(401).header("WWW-Authenticate", "Basic").build());
        	return;
        }
        
        LOGGER.trace("Authenticating username '{}' to REST service", enteredUsername);
        
        // We need to create task before attempting authentication. Task ID is also a session ID.
        Task task = taskManager.createTaskInstance(ModelRestService.OPERATION_REST_SERVICE);
        task.setChannel(SchemaConstants.CHANNEL_REST_URI);
        
        ConnectionEnvironment connEnv = createConnectionEnvironment();
        connEnv.setSessionId(task.getTaskIdentifier());
        String enteredPassword = policy.getPassword();
        UsernamePasswordAuthenticationToken token;
        try {
        	token = authenticationEvaluator.authenticateUserPassword(connEnv, enteredUsername, enteredPassword);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
        	LOGGER.trace("Exception while authenticating username '{}' to REST service: {}", enteredUsername, e.getMessage(), e);
        	requestCtx.abortWith(Response.status(401).header("WWW-Authenticate", "Basic authentication failed. Cannot authenticate user.").build());
			return;
        } catch (DisabledException | LockedException | CredentialsExpiredException | AccessDeniedException
        		| AuthenticationCredentialsNotFoundException | AuthenticationServiceException e) {
        	LOGGER.trace("Exception while authenticating username '{}' to REST service: {}", enteredUsername, e.getMessage(), e);
        	requestCtx.abortWith(Response.status(403).build());
			return;
        }
        
        UserType user = ((MidPointPrincipal)token.getPrincipal()).getUser();
        task.setOwner(user.asPrismObject());
        
        m.put(RestServiceUtil.MESSAGE_PROPERTY_TASK_NAME, task);
        securityEnforcer.setupPreAuthenticatedSecurityContext(user.asPrismObject());
        
        LOGGER.trace("Authenticated to REST service as {}", user);
           
        OperationResult authorizeResult = new OperationResult("Rest authentication/authorization operation.");
        
        
        try {
			securityEnforcer.authorize(AuthorizationConstants.AUTZ_REST_ALL_URL, null, null, null, null, null, authorizeResult);
		} catch (SecurityViolationException e){
			securityHelper.auditLoginFailure(enteredUsername, user, connEnv, "Not authorized");
			requestCtx.abortWith(Response.status(403).build());
			return;
		} catch (SchemaException e) {
			securityHelper.auditLoginFailure(enteredUsername, user, connEnv, "Schema error: "+e.getMessage());
			requestCtx.abortWith(Response.status(Status.BAD_REQUEST).build());
			return;
		}
        
        LOGGER.trace("Authorized to use REST service ({})", user);
        
    }

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		// nothing to do
	}

	@Override
	public void filter(ContainerRequestContext requestCtx) throws IOException {
		Message m = JAXRSUtils.getCurrentMessage();
		handleRequest(m, requestCtx);
	}

	private ConnectionEnvironment createConnectionEnvironment() {
		ConnectionEnvironment connEnv = new ConnectionEnvironment();
		connEnv.setChannel(SchemaConstants.CHANNEL_REST_URI);
		// TODO: remote host
		return connEnv;
	}
}
