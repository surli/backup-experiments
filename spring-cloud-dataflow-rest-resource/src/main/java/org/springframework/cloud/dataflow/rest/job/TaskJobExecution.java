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

import org.springframework.batch.core.JobExecution;
import org.springframework.util.Assert;

/**
 * The relation a {@link JobExecution} has with its associated task execution id.
 *
 * @author Glenn Renfro
 */
public class TaskJobExecution {
	private final long taskId;

	private final boolean isTaskDefined;

	private final JobExecution jobExecution;

	public TaskJobExecution(long taskId, JobExecution jobExecution, boolean isTaskDefined) {
		Assert.notNull(jobExecution, "jobExecution must not be null");
		this.taskId = taskId;
		this.jobExecution = jobExecution;
		this.isTaskDefined = isTaskDefined;
	}

	/**
	 * @return the Task Id that is associated with the {@link JobExecution}.
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * @return the {@link JobExecution} that is associated with the task id.
	 */
	public JobExecution getJobExecution() {
		return jobExecution;
	}

	/**
	 * @return true if the Task Definition for the task id exists in the task repository
	 * else returns false.
	 */
	public boolean isTaskDefined() {
		return isTaskDefined;
	}
}
