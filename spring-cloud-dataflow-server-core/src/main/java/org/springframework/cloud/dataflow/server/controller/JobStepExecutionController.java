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

package org.springframework.cloud.dataflow.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.service.NoSuchStepExecutionException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.dataflow.rest.resource.StepExecutionResource;
import org.springframework.cloud.dataflow.server.job.support.StepExecutionResourceBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Glenn Renfro
 */
@RestController
@RequestMapping("/jobs/executions/{jobExecutionId}/steps")
@ExposesResourceFor(StepExecutionResource.class)
public class JobStepExecutionController {

	private final JobService jobService;

	private final Assembler stepAssembler = new Assembler();

	/**
	 * Creates a {@code JobStepExecutionsController} that retrieves Job Step Execution
	 * information from a the {@link JobService}
	 *
	 * @param jobService the service this controller will use for retrieving job step
	 * execution information.
	 */
	@Autowired
	public JobStepExecutionController(JobService jobService) {
		Assert.notNull(jobService, "repository must not be null");
		this.jobService = jobService;
	}

	/**
	 * List all step executions.
	 *
	 * @param id the {@link JobExecution}.
	 * @param pageable the pagination information.
	 * @param assembler the resource assembler for step executions.
	 * @return Collection of {@link StepExecutionResource} for the given jobExecutionId.
	 * @throws NoSuchJobExecutionException if the job execution for the id specified does
	 * not exist.
	 */
	@RequestMapping(value = { "" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public PagedResources<StepExecutionResource> stepExecutions(@PathVariable("jobExecutionId") long id,
			Pageable pageable, PagedResourcesAssembler<StepExecution> assembler) throws NoSuchJobExecutionException {
		List<StepExecution> result;
		result = new ArrayList<>(jobService.getStepExecutions(id));
		Page<StepExecution> page = new PageImpl<>(result, pageable, result.size());
		return assembler.toResource(page, stepAssembler);
	}

	/**
	 * Retrieve a specific {@link StepExecutionResource}.
	 *
	 * @param id the {@link JobExecution} id.
	 * @param stepId the {@link StepExecution} id.
	 * @return Collection of {@link StepExecutionResource} for the given jobExecutionId.
	 * @throws NoSuchStepExecutionException if the stepId specified does not exist.
	 * @throws NoSuchJobExecutionException if the job execution for the id specified does
	 * not exist.
	 */
	@RequestMapping(value = { "/{stepExecutionId}" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public StepExecutionResource getStepExecution(@PathVariable("jobExecutionId") Long id,
			@PathVariable("stepExecutionId") Long stepId)
			throws NoSuchStepExecutionException, NoSuchJobExecutionException {
		return stepAssembler.toResource(jobService.getStepExecution(id, stepId));
	}

	/**
	 * {@link org.springframework.hateoas.ResourceAssembler} implementation that converts
	 * {@link StepExecution}s to {@link StepExecutionResource}s.
	 */
	private static class Assembler extends ResourceAssemblerSupport<StepExecution, StepExecutionResource> {

		public Assembler() {
			super(JobStepExecutionController.class, StepExecutionResource.class);
		}

		@Override
		public StepExecutionResource toResource(StepExecution stepExecution) {
			return createResourceWithId(stepExecution.getId(), stepExecution, stepExecution.getJobExecution().getId());
		}

		@Override
		public StepExecutionResource instantiateResource(StepExecution stepExecution) {
			return StepExecutionResourceBuilder.toResource(stepExecution);
		}
	}
}
