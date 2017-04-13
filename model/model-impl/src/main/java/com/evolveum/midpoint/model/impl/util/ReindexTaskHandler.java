/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.model.impl.util;

import com.evolveum.midpoint.model.api.ModelPublicConstants;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.result.OperationConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskCategory;
import com.evolveum.midpoint.task.api.TaskRunResult;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Task handler for "reindex" task.
 * It simply executes empty modification delta on each repository object.
 *
 * TODO implement also for sub-objects, namely certification cases.
 *
 * @author Pavol Mederly
 */
@Component
public class ReindexTaskHandler extends AbstractSearchIterativeTaskHandler<ObjectType, ReindexResultHandler> {

    public static final String HANDLER_URI = ModelPublicConstants.REINDEX_TASK_HANDLER_URI;

    // WARNING! This task handler is efficiently singleton!
 	// It is a spring bean and it is supposed to handle all search task instances
 	// Therefore it must not have task-specific fields. It can only contain fields specific to
 	// all tasks of a specified type

    private static final Trace LOGGER = TraceManager.getTrace(ReindexTaskHandler.class);

    public ReindexTaskHandler() {
        super("Reindex", OperationConstants.REINDEX);
        setLogFinishInfo(true);
        setPreserveStatistics(false);
    }

    @PostConstruct
    private void initialize() {
        taskManager.registerHandler(HANDLER_URI, this);
    }

	@Override
	protected ReindexResultHandler createHandler(TaskRunResult runResult, Task coordinatorTask, OperationResult opResult)
			throws SchemaException, SecurityViolationException {
		securityEnforcer.authorize(AuthorizationConstants.AUTZ_ALL_URL, null, null, null, null, null, opResult);
        return new ReindexResultHandler(coordinatorTask, ReindexTaskHandler.class.getName(),
				"reindex", "reindex", taskManager, repositoryService);
	}

	@Override
	protected boolean initializeRun(ReindexResultHandler handler,
			TaskRunResult runResult, Task task, OperationResult opResult) {
		return super.initializeRun(handler, runResult, task, opResult);
	}

    @Override
    protected Class<? extends ObjectType> getType(Task task) {
		return getTypeFromTask(task, ObjectType.class);
    }

    @Override
	protected ObjectQuery createQuery(ReindexResultHandler handler, TaskRunResult runResult, Task task, OperationResult opResult) throws SchemaException {
        ObjectQuery query = createQueryFromTask(handler, runResult, task, opResult);
        LOGGER.info("Using query:\n{}", query.debugDump());
        return query;
	}

    @Override
    protected boolean useRepositoryDirectly(ReindexResultHandler resultHandler, TaskRunResult runResult, Task coordinatorTask, OperationResult opResult) {
        return true;
    }

    @Override
    public String getCategoryName(Task task) {
        return TaskCategory.UTIL;
    }

    @Override
    public List<String> getCategoryNames() {
        return null;
    }
}
