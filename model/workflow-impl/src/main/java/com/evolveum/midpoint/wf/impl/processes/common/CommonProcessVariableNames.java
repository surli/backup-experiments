/*
 * Copyright (c) 2010-2013 Evolveum
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

package com.evolveum.midpoint.wf.impl.processes.common;

/**
 * @author mederly
 */
public class CommonProcessVariableNames {

    // [String]
    // Process instance name, e.g. "Approving adding Webmaster to JoeDoe".
	// Used for diagnostic purposes.
    public static final String VARIABLE_PROCESS_INSTANCE_NAME = "processInstanceName";

	// [java.util.Date]
    // When the process instance was started.
    public static final String VARIABLE_START_TIME = "startTime";

	// [String]
    // OID of task related to the process instance.
    public static final String VARIABLE_MIDPOINT_TASK_OID = "midPointTaskOid";

	// [String]
    // Java class name of the change processor (the same as wf:changeProcessor task property)
    public static final String VARIABLE_CHANGE_PROCESSOR = "changeProcessor";

	// [LightweightObjectRef]
    // Requester - OID + name + perhaps additional information
    public static final String VARIABLE_REQUESTER_REF = "requesterRef";

	// [LightweightObjectRef]
	// Object of the operation - if can be specified like this
	public static final String VARIABLE_OBJECT_REF = "objectRef";

	// [LightweightObjectRef]
	// Target of the operation - if any
	public static final String VARIABLE_TARGET_REF = "targetRef";

	// [Boolean]
    // A signal that the process instance is being stopped. Used e.g. to suppress propagation of exceptions
    // occurring in the process instance end listener.
    public static final String VARIABLE_PROCESS_INSTANCE_IS_STOPPING = "processInstanceIsStopping";

	// [String]
    // Name of process interface bean (ProcessMidPointInterface implementation) that is related to this process
    public static final String VARIABLE_PROCESS_INTERFACE_BEAN_NAME = "processInterfaceBeanName";

	// [ActivitiUtil]
	// Object that provides various utility methods for use in processes, e.g. getApprover(RoleType r).
	public static final String VARIABLE_UTIL = "util";

	// [String]
	// Basic decision returned from a work item.
	// for most work items it is simple __APPROVED__ or __REJECTED__, but in principle this can be any string value
	public static final String FORM_FIELD_DECISION = "[H]decision";

	// [String]
	// Comment related to that decision - set by user task (form).
	// this value is put into audit record, so its advisable to use this particular name
	public static final String FORM_FIELD_COMMENT = "comment";

	public static final String FORM_BUTTON_PREFIX = "[B]";
}
