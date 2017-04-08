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
package com.evolveum.midpoint.model.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.model.api.*;
import com.evolveum.midpoint.model.api.validator.ResourceValidator;
import com.evolveum.midpoint.model.api.validator.Scope;
import com.evolveum.midpoint.model.api.validator.ValidationResult;
import com.evolveum.midpoint.model.impl.util.RestServiceUtil;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.builder.QueryBuilder;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.midpoint.xml.ns._public.model.model_3.ExecuteScriptsResponseType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ItemListType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ScriptingExpressionType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import org.apache.commons.lang.Validate;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evolveum.midpoint.model.impl.rest.PATCH;
import com.evolveum.midpoint.model.impl.security.SecurityHelper;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.QueryJaxbConvertor;
import com.evolveum.midpoint.schema.DeltaConvertor;
import com.evolveum.midpoint.schema.constants.MidPointConstants;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;

/**
 * @author katkav
 * @author semancik
 */
@Service
@Produces({"application/xml", "application/json"})
public class ModelRestService {

	public static final String CLASS_DOT = ModelRestService.class.getName() + ".";
	public static final String OPERATION_REST_SERVICE = CLASS_DOT + "restService";
	public static final String OPERATION_GET = CLASS_DOT + "get";
	public static final String OPERATION_ADD_OBJECT = CLASS_DOT + "addObject";
	public static final String OPERATION_DELETE_OBJECT = CLASS_DOT + "deleteObject";
	public static final String OPERATION_MODIFY_OBJECT = CLASS_DOT + "modifyObject";
	public static final String OPERATION_NOTIFY_CHANGE = CLASS_DOT + "notifyChange";
	public static final String OPERATION_FIND_SHADOW_OWNER = CLASS_DOT + "findShadowOwner";
	public static final String OPERATION_SEARCH_OBJECTS = CLASS_DOT + "searchObjects";
	public static final String OPERATION_IMPORT_FROM_RESOURCE = CLASS_DOT + "importFromResource";
	public static final String OPERATION_TEST_RESOURCE = CLASS_DOT + "testResource";
	public static final String OPERATION_SUSPEND_TASKS = CLASS_DOT + "suspendTasks";
	public static final String OPERATION_SUSPEND_AND_DELETE_TASKS = CLASS_DOT + "suspendAndDeleteTasks";
	public static final String OPERATION_RESUME_TASKS = CLASS_DOT + "resumeTasks";
	public static final String OPERATION_SCHEDULE_TASKS_NOW = CLASS_DOT + "scheduleTasksNow";
	public static final String OPERATION_EXECUTE_SCRIPT = CLASS_DOT + "executeScript";
	public static final String OPERATION_COMPARE = CLASS_DOT + "compare";
	public static final String OPERATION_GET_LOG_FILE_CONTENT = CLASS_DOT + "getLogFileContent";
	public static final String OPERATION_GET_LOG_FILE_SIZE = CLASS_DOT + "getLogFileSize";
	private static final String CURRENT = "current";
	private static final String VALIDATE = "validate";

	@Autowired
	private ModelCrudService model;

	@Autowired
	private ScriptingService scriptingService;

	@Autowired
	private ModelService modelService;

	@Autowired
	private ModelDiagnosticService modelDiagnosticService;

	@Autowired
	private ModelInteractionService modelInteraction;

	@Autowired
	private PrismContext prismContext;

	@Autowired
	private SecurityHelper securityHelper;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private ResourceValidator resourceValidator;

	private static final Trace LOGGER = TraceManager.getTrace(ModelRestService.class);

	public static final long WAIT_FOR_TASK_STOP = 2000L;

	public ModelRestService() {
		// nothing to do
	}

	@GET
	@Path("/users/{id}/policy")
	public Response getValuePolicyForUser(@PathParam("id") String oid, @Context MessageContext mc) {
		LOGGER.debug("getValuePolicyForUser start");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_GET);

		Response response;
		try {
			Collection<SelectorOptions<GetOperationOptions>> options =
					SelectorOptions.createCollection(GetOperationOptions.createRaw());
			PrismObject<UserType> user = model.getObject(UserType.class, oid, options, task, parentResult);

			CredentialsPolicyType policy = modelInteraction.getCredentialsPolicy(user, task, parentResult);

			ResponseBuilder builder = Response.ok();
			builder.entity(policy);
			response = builder.build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);

		LOGGER.debug("getValuePolicyForUser finish");

		return response;
	}

	@GET
	@Path("/{type}/{id}")
	public <T extends ObjectType> Response getObject(@PathParam("type") String type, @PathParam("id") String id,
			@QueryParam("options") List<String> options,
			@QueryParam("include") List<String> include,
			@QueryParam("exclude") List<String> exclude,
			@Context MessageContext mc){
		LOGGER.debug("model rest service for get operation start");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_GET);

		Class<T> clazz = ObjectTypes.getClassFromRestType(type);
		Collection<SelectorOptions<GetOperationOptions>> getOptions = GetOperationOptions.fromRestOptions(options, include, exclude);
		Response response;

		try {
			PrismObject<T> object;
			if (NodeType.class.equals(clazz) && CURRENT.equals(id)) {
				String nodeId = taskManager.getNodeId();
				ObjectQuery query = QueryBuilder.queryFor(NodeType.class, prismContext)
						.item(NodeType.F_NODE_IDENTIFIER).eq(nodeId)
						.build();
			 	List<PrismObject<NodeType>> objects = model.searchObjects(NodeType.class, query, getOptions, task, parentResult);
				if (objects.isEmpty()) {
					throw new ObjectNotFoundException("Current node (id " + nodeId + ") couldn't be found.");
				} else if (objects.size() > 1) {
					throw new IllegalStateException("More than one 'current' node (id " + nodeId + ") found.");
				} else {
					object = (PrismObject<T>) objects.get(0);
				}
			} else {
				object = model.getObject(clazz, id, getOptions, task, parentResult);
			}
			removeExcludes(object, exclude);		// temporary measure until fixed in repo

			ResponseBuilder builder = Response.ok();
			builder.entity(object);
			response = builder.build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}


	@POST
	@Path("/{type}")
//	@Produces({"text/html", "application/xml"})
	@Consumes({"application/xml", "application/json"})
	public <T extends ObjectType> Response addObject(@PathParam("type") String type, PrismObject<T> object,
													 @QueryParam("options") List<String> options,
			@Context UriInfo uriInfo, @Context MessageContext mc) {
		LOGGER.debug("model rest service for add operation start");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_ADD_OBJECT);

		Class clazz = ObjectTypes.getClassFromRestType(type);
		if (!object.getCompileTimeClass().equals(clazz)){
			finishRequest(task);
			return RestServiceUtil.buildErrorResponse(Status.BAD_REQUEST, "Request to add object of type "
					+ object.getCompileTimeClass().getSimpleName() + " to the collection of " + type);
		}


		ModelExecuteOptions modelExecuteOptions = ModelExecuteOptions.fromRestOptions(options);

		String oid;
		Response response;
		try {
			oid = model.addObject(object, modelExecuteOptions, task, parentResult);
			LOGGER.debug("returned oid :  {}", oid );

			ResponseBuilder builder;

			if (oid != null) {
				URI resourceURI = uriInfo.getAbsolutePathBuilder().path(oid).build(oid);
				builder = clazz.isAssignableFrom(TaskType.class) ?		// TODO not the other way around?
						Response.accepted().location(resourceURI) : Response.created(resourceURI);
			} else {
				// OID might be null e.g. if the object creation is a subject of workflow approval
				builder = Response.accepted();			// TODO is this ok ?
			}
			// (not used currently)
			//validateIfRequested(object, options, builder, task, parentResult);
			response = builder.build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}

	// currently unused; but potentially useful in future
	private <T extends ObjectType> void validateIfRequested(PrismObject<?> object,
			List<String> options, ResponseBuilder builder, Task task,
			OperationResult parentResult) {
		if (options != null && options.contains(VALIDATE) && object.asObjectable() instanceof ResourceType) {
			ValidationResult validationResult = resourceValidator
					.validate((PrismObject<ResourceType>) object, Scope.THOROUGH, null, task, parentResult);
			builder.entity(validationResult.toValidationResultType());			// TODO move to parentResult, and return the result!
		}
	}

	@PUT
	@Path("/{type}/{id}")
//	@Produces({"text/html", "application/xml"})
	public <T extends ObjectType> Response addObject(@PathParam("type") String type, @PathParam("id") String id,
			PrismObject<T> object, @QueryParam("options") List<String> options, @Context UriInfo uriInfo,
			@Context Request request, @Context MessageContext mc){

		LOGGER.debug("model rest service for add operation start");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_ADD_OBJECT);

		Class clazz = ObjectTypes.getClassFromRestType(type);
		if (!object.getCompileTimeClass().equals(clazz)){
			finishRequest(task);
			return RestServiceUtil.buildErrorResponse(Status.BAD_REQUEST, "Request to add object of type "
					+ object.getCompileTimeClass().getSimpleName()
					+ " to the collection of " + type);
		}

		ModelExecuteOptions modelExecuteOptions = ModelExecuteOptions.fromRestOptions(options);
		if (modelExecuteOptions == null) {
			modelExecuteOptions = ModelExecuteOptions.createOverwrite();
		} else if (!ModelExecuteOptions.isOverwrite(modelExecuteOptions)){
			modelExecuteOptions.setOverwrite(Boolean.TRUE);
		}

		String oid;
		Response response;
		try {
			oid = model.addObject(object, modelExecuteOptions, task, parentResult);
			LOGGER.debug("returned oid :  {}", oid );

			URI resourceURI = uriInfo.getAbsolutePathBuilder().path(oid).build(oid);
			ResponseBuilder builder = clazz.isAssignableFrom(TaskType.class) ?
					Response.accepted().location(resourceURI) : Response.created(resourceURI);

			// (not used currently)
			//validateIfRequested(object, options, builder, task, parentResult);
			response = builder.build();
		} catch (ObjectAlreadyExistsException e) {
			response = Response.serverError().entity(e.getMessage()).build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}

	@DELETE
	@Path("/{type}/{id}")
//	@Produces({"text/html", "application/xml"})
	public Response deleteObject(@PathParam("type") String type, @PathParam("id") String id,
			@QueryParam("options") List<String> options, @Context MessageContext mc){

		LOGGER.debug("model rest service for delete operation start");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_DELETE_OBJECT);

		Class clazz = ObjectTypes.getClassFromRestType(type);
		Response response;
		try {
			if (clazz.isAssignableFrom(TaskType.class)){
				model.suspendAndDeleteTasks(MiscUtil.createCollection(id), WAIT_FOR_TASK_STOP, true, parentResult);
				parentResult.computeStatus();
				finishRequest(task);
				if (parentResult.isSuccess()){
					return Response.noContent().build();
				}

				return Response.serverError().entity(parentResult.getMessage()).build();

			}

			ModelExecuteOptions modelExecuteOptions = ModelExecuteOptions.fromRestOptions(options);

			model.deleteObject(clazz, id, modelExecuteOptions, task, parentResult);
			response = Response.noContent().build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}

	@POST
	@Path("/{type}/{oid}")
	public <T extends ObjectType> Response modifyObjectPost(@PathParam("type") String type, @PathParam("oid") String oid,
			ObjectModificationType modificationType, @QueryParam("options") List<String> options, @Context MessageContext mc) {
		return modifyObjectPatch(type, oid, modificationType, options, mc);
	}

	@PATCH
	@Path("/{type}/{oid}")
//	@Produces({"text/html", "application/xml"})
	public <T extends ObjectType> Response modifyObjectPatch(@PathParam("type") String type, @PathParam("oid") String oid,
			ObjectModificationType modificationType, @QueryParam("options") List<String> options, @Context MessageContext mc) {

		LOGGER.debug("model rest service for modify operation start");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_MODIFY_OBJECT);

		Class clazz = ObjectTypes.getClassFromRestType(type);
		Response response;
		try {
			ModelExecuteOptions modelExecuteOptions = ModelExecuteOptions.fromRestOptions(options);
			Collection<? extends ItemDelta> modifications = DeltaConvertor.toModifications(modificationType, clazz, prismContext);
			model.modifyObject(clazz, oid, modifications, modelExecuteOptions, task, parentResult);
			response = Response.noContent().build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}

	@POST
	@Path("/notifyChange")
	public Response notifyChange(ResourceObjectShadowChangeDescriptionType changeDescription,
			@Context UriInfo uriInfo, @Context MessageContext mc) {
		LOGGER.debug("model rest service for notify change operation start");
		Validate.notNull(changeDescription, "Chnage description must not be null");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_NOTIFY_CHANGE);

		Response response;
		try {
			model.notifyChange(changeDescription, parentResult, task);
			return Response.ok().build();
//			String oldShadowOid = changeDescription.getOldShadowOid();
//			if (oldShadowOid != null){
//				URI resourceURI = uriInfo.getAbsolutePathBuilder().path(oldShadowOid).build(oldShadowOid);
//				return Response.accepted().location(resourceURI).build();
//			} else {
//				changeDescription.get
//			}
//			response = Response.seeOther((uriInfo.getBaseUriBuilder().path(this.getClass(), "getObject").build(ObjectTypes.TASK.getRestType(), task.getOid()))).build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}



	@GET
	@Path("/shadows/{oid}/owner")
//	@Produces({"text/html", "application/xml"})
	public Response findShadowOwner(@PathParam("oid") String shadowOid, @Context MessageContext mc){

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_FIND_SHADOW_OWNER);

		Response response;
		try {
			PrismObject<UserType> user = model.findShadowOwner(shadowOid, task, parentResult);
			response = Response.ok().entity(user).build();
		} catch (ConfigurationException e) {
			response = RestServiceUtil.buildErrorResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}

	@POST
	@Path("/{type}/search")
//	@Produces({"text/html", "application/xml"})
	public Response searchObjects(@PathParam("type") String type, QueryType queryType,
			@QueryParam("options") List<String> options,
			@QueryParam("include") List<String> include,
			@QueryParam("exclude") List<String> exclude,
			@Context MessageContext mc){

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_SEARCH_OBJECTS);

		Class clazz = ObjectTypes.getClassFromRestType(type);
		Response response;
		try {
			ObjectQuery query = QueryJaxbConvertor.createObjectQuery(clazz, queryType, prismContext);
			Collection<SelectorOptions<GetOperationOptions>> searchOptions = GetOperationOptions.fromRestOptions(options, include, exclude);
			List<PrismObject<? extends ShadowType>> objects = model.searchObjects(clazz, query, searchOptions, task, parentResult);

			ObjectListType listType = new ObjectListType();
			for (PrismObject<? extends ObjectType> o : objects) {
				removeExcludes(o, exclude);		// temporary measure until fixed in repo
				listType.getObject().add(o.asObjectable());
			}

			response = Response.ok().entity(listType).build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}

	private void removeExcludes(PrismObject<? extends ObjectType> object, List<String> exclude) {
		for (ItemPath path : ItemPath.fromStringList(exclude)) {
			Item item = object.findItem(path);		// reduce to "removeItem" after fixing that method implementation
			if (item != null) {
				object.removeItem(item.getPath(), Item.class);
			}
		}
	}

	@POST
	@Path("/resources/{resourceOid}/import/{objectClass}")
//	@Produces({"text/html", "application/xml"})
	public Response importFromResource(@PathParam("resourceOid") String resourceOid, @PathParam("objectClass") String objectClass,
			@Context MessageContext mc, @Context UriInfo uriInfo) {
		LOGGER.debug("model rest service for import from resource operation start");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_IMPORT_FROM_RESOURCE);

		QName objClass = new QName(MidPointConstants.NS_RI, objectClass);
		Response response;
		try {
			model.importFromResource(resourceOid, objClass, task, parentResult);
			response = Response.seeOther((uriInfo.getBaseUriBuilder().path(this.getClass(), "getObject")
					.build(ObjectTypes.TASK.getRestType(), task.getOid()))).build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		parentResult.computeStatus();
		finishRequest(task);
		return response;
	}

	@POST
	@Path("/resources/{resourceOid}/test")
//	@Produces({"text/html", "application/xml"})
	public Response testResource(@PathParam("resourceOid") String resourceOid, @Context MessageContext mc) {
		LOGGER.debug("model rest service for test resource operation start");

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_TEST_RESOURCE);

		Response response;
		OperationResult testResult = null;
		try {
			testResult = model.testResource(resourceOid, task);
			response = Response.ok(testResult).build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		if (testResult != null) {
			parentResult.getSubresults().add(testResult);
		}

		finishRequest(task);
		return response;
	}

	@POST
	@Path("/tasks/{oid}/suspend")
    public Response suspendTasks(@PathParam("oid") String taskOid, @Context MessageContext mc) {

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_SUSPEND_TASKS);

		Response response;
		Collection<String> taskOids = MiscUtil.createCollection(taskOid);
		try {
			model.suspendTasks(taskOids, WAIT_FOR_TASK_STOP, parentResult);
			parentResult.computeStatus();
			if (parentResult.isSuccess()){
				response = Response.noContent().build();
			} else {
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(parentResult.getMessage()).build();
			}
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		finishRequest(task);
		return response;
    }

//	@DELETE
//	@Path("tasks/{oid}/suspend")
    public Response suspendAndDeleteTasks(@PathParam("oid") String taskOid, @Context MessageContext mc) {

    	Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_SUSPEND_AND_DELETE_TASKS);

		Response response;
		Collection<String> taskOids = MiscUtil.createCollection(taskOid);
		try {
			model.suspendAndDeleteTasks(taskOids, WAIT_FOR_TASK_STOP, true, parentResult);

			parentResult.computeStatus();
			if (parentResult.isSuccess()) {
				response = Response.accepted().build();
			} else {
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(parentResult.getMessage()).build();
			}
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		finishRequest(task);
		return response;
    }

	@POST
	@Path("/tasks/{oid}/resume")
    public Response resumeTasks(@PathParam("oid") String taskOid, @Context MessageContext mc) {

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_RESUME_TASKS);

		Response response;
		Collection<String> taskOids = MiscUtil.createCollection(taskOid);
		try {
			model.resumeTasks(taskOids, parentResult);

			parentResult.computeStatus();

			if (parentResult.isSuccess()) {
				response = Response.accepted().build();
			} else {
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(parentResult.getMessage()).build();
			}
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		finishRequest(task);
		return response;
    }


	@POST
	@Path("tasks/{oid}/run")
    public Response scheduleTasksNow(@PathParam("oid") String taskOid, @Context MessageContext mc) {

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult parentResult = task.getResult().createSubresult(OPERATION_SCHEDULE_TASKS_NOW);

		Collection<String> taskOids = MiscUtil.createCollection(taskOid);

		Response response;
		try {
			model.scheduleTasksNow(taskOids, parentResult);

			parentResult.computeStatus();

			if (parentResult.isSuccess()) {
				response = Response.accepted().build();
			} else {
				response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(parentResult.getMessage()).build();
			}
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		finishRequest(task);
		return response;
    }

	@POST
	@Path("/rpc/executeScript")
	//	@Produces({"text/html", "application/xml"})
	@Consumes({"application/xml" })
	public <T extends ObjectType> Response executeScript(ScriptingExpressionType scriptingExpression,
			@QueryParam("asynchronous") Boolean asynchronous,
			@Context UriInfo uriInfo, @Context MessageContext mc) {

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult result = task.getResult().createSubresult(OPERATION_EXECUTE_SCRIPT);

		String oid;
		Response response;
		try {
			ResponseBuilder builder;
			if (Boolean.TRUE.equals(asynchronous)) {
				scriptingService.evaluateExpression(scriptingExpression, task, result);
				URI resourceUri = uriInfo.getAbsolutePathBuilder().path(task.getOid()).build(task.getOid());
				builder = Response.created(resourceUri);
			} else {
				ScriptExecutionResult executionResult = scriptingService.evaluateExpression(scriptingExpression, task, result);

				ExecuteScriptsResponseType operationOutput = new ExecuteScriptsResponseType();
				operationOutput.setResult(result.createOperationResultType());
				ScriptOutputsType outputs = new ScriptOutputsType();
				operationOutput.setOutputs(outputs);
				SingleScriptOutputType output = new SingleScriptOutputType();
				output.setTextOutput(executionResult.getConsoleOutput());
				output.setXmlData(prepareXmlData(executionResult.getDataOutput()));
				outputs.getOutput().add(output);

				builder = Response.ok();
				builder.entity(operationOutput);
			}

			response = builder.build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
			LoggingUtils.logUnexpectedException(LOGGER, "Couldn't execute script.", ex);
		}

		result.computeStatus();
		finishRequest(task);
		return response;
	}

	private ItemListType prepareXmlData(List<PrismValue> output) throws JAXBException, SchemaException {
		ItemListType itemListType = new ItemListType();
		if (output != null) {
			for (PrismValue value : output) {
				RawType rawType = new RawType(prismContext.xnodeSerializer().root(SchemaConstants.C_VALUE).serialize(value), prismContext);
				itemListType.getItem().add(rawType);
			}
		}
		return itemListType;
	}

	@POST
	@Path("/rpc/compare")
	//	@Produces({"text/html", "application/xml"})
	@Consumes({"application/xml" })
	public <T extends ObjectType> Response compare(PrismObject<T> clientObject,
			@QueryParam("readOptions") List<String> restReadOptions,
			@QueryParam("compareOptions") List<String> restCompareOptions,
			@QueryParam("ignoreItems") List<String> restIgnoreItems,
			@Context MessageContext mc) {

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult result = task.getResult().createSubresult(OPERATION_COMPARE);

		Response response;
		try {
			ResponseBuilder builder;
			List<ItemPath> ignoreItemPaths = ItemPath.fromStringList(restIgnoreItems);
			final GetOperationOptions getOpOptions = GetOperationOptions.fromRestOptions(restReadOptions);
			Collection<SelectorOptions<GetOperationOptions>> readOptions =
					getOpOptions != null ? SelectorOptions.createCollection(getOpOptions) : null;
			ModelCompareOptions compareOptions = ModelCompareOptions.fromRestOptions(restCompareOptions);
			CompareResultType compareResult = modelService.compareObject(clientObject, readOptions, compareOptions, ignoreItemPaths, task, result);

			builder = Response.ok();
			builder.entity(compareResult);

			response = builder.build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		result.computeStatus();
		finishRequest(task);
		return response;
	}

	@GET
	@Path("/log/size")
	@Produces({"text/plain"})
	public Response getLogFileSize(@Context MessageContext mc) {

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult result = task.getResult().createSubresult(OPERATION_GET_LOG_FILE_SIZE);

		Response response;
		try {
			long size = modelDiagnosticService.getLogFileSize(task, result);

			ResponseBuilder builder = Response.ok();
			builder.entity(String.valueOf(size));
			response = builder.build();
		} catch (Exception ex) {
			response = RestServiceUtil.handleException(ex);
		}

		result.computeStatus();
		finishRequest(task);
		return response;
	}

	@GET
	@Path("/log")
	@Produces({"text/plain"})
	public Response getLog(@QueryParam("fromPosition") Long fromPosition, @QueryParam("maxSize") Long maxSize, @Context MessageContext mc) {

		Task task = RestServiceUtil.initRequest(mc);
		OperationResult result = task.getResult().createSubresult(OPERATION_GET_LOG_FILE_CONTENT);

		Response response;
		try {
			LogFileContentType content = modelDiagnosticService.getLogFileContent(fromPosition, maxSize, task, result);

			ResponseBuilder builder = Response.ok();
			builder.entity(content.getContent());
			builder.header("ReturnedDataPosition", content.getAt());
			builder.header("ReturnedDataComplete", content.isComplete());
			builder.header("CurrentLogFileSize", content.getLogFileSize());

			response = builder.build();
		} catch (Exception ex) {
			LoggingUtils.logUnexpectedException(LOGGER, "Cannot get log file content: fromPosition={}, maxSize={}", ex, fromPosition, maxSize);
			response = RestServiceUtil.handleException(ex);
		}

		result.computeStatus();
		finishRequest(task);
		return response;
	}


	//    @GET
//    @Path("tasks/{oid}")
//    public Response getTaskByIdentifier(@PathParam("oid") String identifier) throws SchemaException, ObjectNotFoundException {
//    	OperationResult parentResult = new OperationResult("getTaskByIdentifier");
//        PrismObject<TaskType> task = model.getTaskByIdentifier(identifier, null, parentResult);
//
//        return Response.ok(task).build();
//    }
//
//
//    public boolean deactivateServiceThreads(long timeToWait, OperationResult parentResult) {
//        return model.deactivateServiceThreads(timeToWait, parentResult);
//    }
//
//    public void reactivateServiceThreads(OperationResult parentResult) {
//        model.reactivateServiceThreads(parentResult);
//    }
//
//    public boolean getServiceThreadsActivationState() {
//        return model.getServiceThreadsActivationState();
//    }
//
//    public void stopSchedulers(Collection<String> nodeIdentifiers, OperationResult parentResult) {
//        model.stopSchedulers(nodeIdentifiers, parentResult);
//    }
//
//    public boolean stopSchedulersAndTasks(Collection<String> nodeIdentifiers, long waitTime, OperationResult parentResult) {
//        return model.stopSchedulersAndTasks(nodeIdentifiers, waitTime, parentResult);
//    }
//
//    public void startSchedulers(Collection<String> nodeIdentifiers, OperationResult parentResult) {
//        model.startSchedulers(nodeIdentifiers, parentResult);
//    }
//
//    public void synchronizeTasks(OperationResult parentResult) {
//    	model.synchronizeTasks(parentResult);
//    }

	private void finishRequest(Task task) {
		RestServiceUtil.finishRequest(task, securityHelper);
	}

}
