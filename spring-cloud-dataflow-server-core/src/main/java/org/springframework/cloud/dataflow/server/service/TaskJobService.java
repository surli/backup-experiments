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

package org.springframework.cloud.dataflow.server.service;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.cloud.dataflow.rest.job.JobInstanceExecutions;
import org.springframework.cloud.dataflow.rest.job.TaskJobExecution;
import org.springframework.cloud.dataflow.server.job.support.JobNotRestartableException;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.data.domain.Pageable;

/**
 * Repository that retrieves Tasks and JobExecutions/Instances and the associations
 * between them.
 *
 * @author Glenn Renfro.
 * @author Gunnar Hillert
 */
public interface TaskJobService {

	/**
	 * Retrieves Pageable list of {@link JobExecution}s from the JobRepository and matches
	 * the data with a task id.
	 *
	 * @param pageable enumerates the data to be returned.
	 * @return List containing {@link TaskJobExecution}s.
	 * @throws NoSuchJobExecutionException in the event that a job execution id specified
	 * is not present when looking up stepExecutions for the result.
	 */
	List<TaskJobExecution> listJobExecutions(Pageable pageable) throws NoSuchJobExecutionException;

	/**
	 * Retrieves Pageable list of {@link JobExecution} from the JobRepository with a
	 * specific jobName and matches the data with a task id.
	 *
	 * @param pageable enumerates the data to be returned.
	 * @param jobName the name of the job for which to search.
	 * @return List containing {@link TaskJobExecution}s.
	 * @throws NoSuchJobException if the job with the given name does not exist.
	 */
	List<TaskJobExecution> listJobExecutionsForJob(Pageable pageable, String jobName) throws NoSuchJobException;

	/**
	 * Retrieves a JobExecution from the JobRepository and matches it with a task id.
	 *
	 * @param id the id of the {@link JobExecution}
	 * @return the {@link TaskJobExecution}s associated with the id.
	 * @throws NoSuchJobExecutionException if the specified job execution for the id does
	 * not exist.
	 */
	TaskJobExecution getJobExecution(long id) throws NoSuchJobExecutionException;

	/**
	 * Retrieves Pageable list of {@link JobInstanceExecutions} from the JobRepository
	 * with a specific jobName and matches the data with the associated JobExecutions.
	 *
	 * @param pageable enumerates the data to be returned.
	 * @param jobName the name of the job for which to search.
	 * @return List containing {@link JobInstanceExecutions}.
	 * @throws NoSuchJobException if the job for the jobName specified does not exist.
	 */
	List<JobInstanceExecutions> listTaskJobInstancesForJobName(Pageable pageable, String jobName)
			throws NoSuchJobException;

	/**
	 * Retrieves a {@link JobInstance} from the JobRepository and matches it with the
	 * associated {@link JobExecution}s.
	 *
	 * @param id the id of the {@link JobInstance}
	 * @return the {@link JobInstanceExecutions} associated with the id.
	 * @throws NoSuchJobInstanceException if job instance id does not exist.
	 * @throws NoSuchJobException if the job for the job instance does not exist.
	 */
	JobInstanceExecutions getJobInstance(long id) throws NoSuchJobInstanceException, NoSuchJobException;

	/**
	 * Retrieves the total number of job instances for a job name.
	 *
	 * @param jobName the name of the job instance.
	 * @return the number of job instances associated with the jobName.
	 * @throws NoSuchJobException if the job for jobName specified does not exist.
	 */
	int countJobInstances(String jobName) throws NoSuchJobException;

	/**
	 * Retrieves the total number of the job executions.
	 *
	 * @return the total number of job executions.
	 */
	int countJobExecutions();

	/**
	 * Retrieves the total number {@link JobExecution} that match a specific job name.
	 *
	 * @param jobName the job name to search.
	 * @return the number of {@link JobExecution}s that match the job name.
	 * @throws NoSuchJobException if the job for the jobName does not exist.
	 */
	int countJobExecutionsForJob(String jobName) throws NoSuchJobException;

	/**
	 * Restarts a {@link JobExecution} IF the respective {@link JobExecution} is actually
	 * deemed restartable. Otherwise a {@link JobNotRestartableException} is being thrown.
	 *
	 * @param jobExecutionId The id of the JobExecution to restart.
	 * @throws NoSuchJobExecutionException if the JobExecution for the provided id does
	 * not exist.
	 */
	void restartJobExecution(long jobExecutionId) throws NoSuchJobExecutionException;

	/**
	 * Requests a {@link JobExecution} to stop.
	 * <p>
	 * Please remember, that calling this method only requests a job execution to stop
	 * processing. This method does not guarantee a {@link JobExecution} to stop. It is
	 * the responsibility of the implementor of the {@link Job} to react to that request.
	 * Furthermore, this method does not interfere with the associated
	 * {@link TaskExecution}.
	 *
	 * @param jobExecutionId The id of the {@link JobExecution} to stop
	 * @throws NoSuchJobExecutionException if no job execution exists for the
	 * jobExecutionId.
	 * @throws JobExecutionNotRunningException if a stop is requested on a job that is not
	 * running.
	 * @see org.springframework.batch.admin.service.JobService#stop(Long)
	 */
	void stopJobExecution(long jobExecutionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException;
}
