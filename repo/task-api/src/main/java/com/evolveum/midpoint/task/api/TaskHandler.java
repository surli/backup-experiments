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
package com.evolveum.midpoint.task.api;

import java.util.List;

/**
 * @author Radovan Semancik
 *
 */
public interface TaskHandler {
	
	public TaskRunResult run(Task task);
	
	public Long heartbeat(Task task);
	
	// TODO: fix signature
	public void refreshStatus(Task task);

    /**
     * Returns a category name for a given task. In most cases, the name would be independent of concrete task.
     * @param task a task, whose category is to be determined; if getCategoryNames() returns null, this method
     *             has to accept null value as this parameter, and return the (one) category name that it gives
     *             to all tasks
     * @return a user-understandable name, like "LiveSync" or "Workflow"
     */
    public String getCategoryName(Task task);

    /**
     * Returns names of task categories provided by this handler. Usually it will be one-item list.
     * @return a list of category names; may be null - in that case the category info is given by getCategoryName(null)
     */
    public List<String> getCategoryNames();

}
