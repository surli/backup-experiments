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

package com.evolveum.midpoint.wf.api;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.WorkItemEventCauseInformationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.WorkItemNotificationActionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.WorkItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.datatype.Duration;

/**
 * An interface through which external observers can be notified about work item related events.
 * Used e.g. for implementing workflow-related notifications.
 *
 * A tricky question is how to let the observer know how to deal with the process instance state
 * (e.g. how to construct a notification). Currently, the observer has to use the class of
 * the instance state prism object. It is up to the process implementer to provide appropriate
 * information through ChangeProcessor.externalizeInstanceState() method.
 *
 * EXPERIMENTAL. This interface may change in near future.
 *
 * @author mederly
 */
public interface WorkItemListener {

    /**
     * This method is called by wf module when a work item is created.
	 */
    void onWorkItemCreation(ObjectReferenceType assignee, @NotNull WorkItemType workItem,
			Task wfTask, OperationResult result);

    /**
     * This method is called by wf module when a work item is completed.
	 */
    void onWorkItemDeletion(ObjectReferenceType assignee, @NotNull WorkItemType workItem,
			@Nullable WorkItemOperationInfo operationInfo, @Nullable WorkItemOperationSourceInfo sourceInfo,
			Task wfTask, OperationResult result);

    void onWorkItemCustomEvent(ObjectReferenceType assignee, @NotNull WorkItemType workItem,
			@NotNull WorkItemNotificationActionType notificationAction, @Nullable WorkItemEventCauseInformationType cause, Task wfTask,
			OperationResult result);

	/**
	 * EXPERIMENTAL
	 */
	void onWorkItemAllocationChangeCurrentActors(@NotNull WorkItemType workItem,
			@NotNull WorkItemAllocationChangeOperationInfo operationInfo,
			@Nullable WorkItemOperationSourceInfo sourceInfo,
			Duration timeBefore, Task task, OperationResult result);

	void onWorkItemAllocationChangeNewActors(@NotNull WorkItemType workItem, @NotNull WorkItemAllocationChangeOperationInfo operationInfo,
			@Nullable WorkItemOperationSourceInfo sourceInfo, Task task, OperationResult result);
}
