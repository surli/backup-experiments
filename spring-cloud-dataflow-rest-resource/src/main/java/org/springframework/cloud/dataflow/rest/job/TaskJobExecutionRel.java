/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.dataflow.rest.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.util.Assert;

/**
 * Enumerates the relation a {@link TaskExecution} has with its associate Job Execution
 * Ids.
 *
 * @author Glenn Renfro
 */
public class TaskJobExecutionRel {

	private final TaskExecution taskExecution;

	private final List<Long> jobExecutionIds;

	/**
	 * Constructor that establishes the relationship between a {@link TaskExecution} and
	 * the Job Execution Ids of the jobs that were executed within it.
	 *
	 * @param taskExecution to be associated with the job execution ids.
	 * @param jobExecutionIds to be associated with the task execution.
	 */
	public TaskJobExecutionRel(TaskExecution taskExecution, List<Long> jobExecutionIds) {
		Assert.notNull(taskExecution, "taskExecution must not be null");
		this.taskExecution = taskExecution;
		if (jobExecutionIds == null) {
			this.jobExecutionIds = Collections.emptyList();
		}
		else {
			this.jobExecutionIds = Collections.unmodifiableList(new ArrayList<>(jobExecutionIds));
		}
	}

	/**
	 * @return the taskExecution for this relationship.
	 */
	public TaskExecution getTaskExecution() {
		return taskExecution;
	}

	/**
	 * @return the job execution ids that are associated with the {@link TaskExecution} in
	 * this relationship.
	 */
	public List<Long> getJobExecutionIds() {
		return jobExecutionIds;
	}
}
