/*
 * Copyright (c) 2010-2016 Evolveum
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

package com.evolveum.midpoint.wf.impl.policy;

import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.WorkItemType;

/**
 * @author mederly
 */
public class ExpectedWorkItem {
	final String assigneeOid;
	final String targetOid;
	final ExpectedTask task;

	public ExpectedWorkItem(String assigneeOid, String targetOid, ExpectedTask task) {
		this.assigneeOid = assigneeOid;
		this.targetOid = targetOid;
		this.task = task;
	}

	public boolean matches(WorkItemType actualWorkItem) {
		if (!assigneeOid.equals(actualWorkItem.getAssigneeRef().getOid())) {
			return false;
		}
		if (targetOid != null && !targetOid.equals(actualWorkItem.getTargetRef().getOid())) {
			return false;
		}
		PrismReferenceValue actualTaskRef = actualWorkItem.getTaskRef().asReferenceValue();
		TaskType actualTask = (TaskType) actualTaskRef.getObject().asObjectable();
		return task.processName.equals(actualTask.getWorkflowContext().getProcessInstanceName());
	}

	@Override
	public String toString() {
		return "ExpectedWorkItem{" +
				"assigneeOid='" + assigneeOid + '\'' +
				", targetOid='" + targetOid + '\'' +
				", task=" + task +
				'}';
	}
}
