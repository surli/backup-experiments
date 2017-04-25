/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.runtime.checkpoint;

import org.apache.flink.migration.runtime.checkpoint.StateAssignmentOperation;
import org.apache.flink.runtime.executiongraph.ExecutionJobVertex;
import org.apache.flink.runtime.state.KeyGroupRange;
import org.apache.flink.runtime.state.KeyGroupRangeAssignment;
import org.apache.flink.runtime.state.OperatorStateHandle;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class contains a number of utility methods used by the {@link StateAssignmentOperation} and
 * {@link StateAssignmentOperationV2}.
 */
public class StateAssignmentOperationUtils {

	/**
	 * Groups the available set of key groups into key group partitions. A key group partition is
	 * the set of key groups which is assigned to the same task. Each set of the returned list
	 * constitutes a key group partition.
	 * <p>
	 * <b>IMPORTANT</b>: The assignment of key groups to partitions has to be in sync with the
	 * KeyGroupStreamPartitioner.
	 *
	 * @param numberKeyGroups Number of available key groups (indexed from 0 to numberKeyGroups - 1)
	 * @param parallelism     Parallelism to generate the key group partitioning for
	 * @return List of key group partitions
	 */
	public static List<KeyGroupRange> createKeyGroupPartitions(int numberKeyGroups, int parallelism) {
		Preconditions.checkArgument(numberKeyGroups >= parallelism);
		List<KeyGroupRange> result = new ArrayList<>(parallelism);

		for (int i = 0; i < parallelism; ++i) {
			result.add(KeyGroupRangeAssignment.computeKeyGroupRangeForOperatorIndex(numberKeyGroups, parallelism, i));
		}
		return result;
	}

	/**
	 * Verifies conditions in regards to parallelism and maxParallelism that must be met when restoring state.
	 *
	 * @param taskState state to restore
	 * @param executionJobVertex task for which the state should be restored
	 * @param logger logger to use
	 */
	public static void checkParallelismPreconditions(TaskState taskState, ExecutionJobVertex executionJobVertex, Logger logger) {
		//----------------------------------------max parallelism preconditions-------------------------------------

		// check that the number of key groups have not changed or if we need to override it to satisfy the restored state
		if (taskState.getMaxParallelism() != executionJobVertex.getMaxParallelism()) {

			if (!executionJobVertex.isMaxParallelismConfigured()) {
				// if the max parallelism was not explicitly specified by the user, we derive it from the state

				logger.debug("Overriding maximum parallelism for JobVertex {} from {} to {}",
					executionJobVertex.getJobVertexId(), executionJobVertex.getMaxParallelism(), taskState.getMaxParallelism());

				executionJobVertex.setMaxParallelism(taskState.getMaxParallelism());
			} else {
				// if the max parallelism was explicitly specified, we complain on mismatch
				throw new IllegalStateException("The maximum parallelism (" +
					taskState.getMaxParallelism() + ") with which the latest " +
					"checkpoint of the execution job vertex " + executionJobVertex +
					" has been taken and the current maximum parallelism (" +
					executionJobVertex.getMaxParallelism() + ") changed. This " +
					"is currently not supported.");
			}
		}

		//----------------------------------------parallelism preconditions-----------------------------------------

		final int oldParallelism = taskState.getParallelism();
		final int newParallelism = executionJobVertex.getParallelism();

		if (taskState.hasNonPartitionedState() && (oldParallelism != newParallelism)) {
			throw new IllegalStateException("Cannot restore the latest checkpoint because " +
				"the operator " + executionJobVertex.getJobVertexId() + " has non-partitioned " +
				"state and its parallelism changed. The operator " + executionJobVertex.getJobVertexId() +
				" has parallelism " + newParallelism + " whereas the corresponding " +
				"state object has a parallelism of " + oldParallelism);
		}
	}

	/**
	 * Repartitions the given operator state using the given {@link OperatorStateRepartitioner} with respect to the new
	 * parallelism.
	 *
	 * @param opStateRepartitioner partitioner to use
	 * @param chainOpParallelStates state to repartition
	 * @param oldParallelism parallelism with which the state is currently partitioned
	 * @param newParallelism parallelism with which the state should be partitioned
	 *
	 * @return repartitioned state
	 */
	public static List<Collection<OperatorStateHandle>> applyRepartitioner(
		OperatorStateRepartitioner opStateRepartitioner,
		List<OperatorStateHandle> chainOpParallelStates,
		int oldParallelism,
		int newParallelism) {

		if (chainOpParallelStates == null) {
			return null;
		}

		//We only redistribute if the parallelism of the operator changed from previous executions
		if (newParallelism != oldParallelism) {

			return opStateRepartitioner.repartitionState(
				chainOpParallelStates,
				newParallelism);
		} else {
			List<Collection<OperatorStateHandle>> repackStream = new ArrayList<>(newParallelism);
			for (OperatorStateHandle operatorStateHandle : chainOpParallelStates) {

				Map<String, OperatorStateHandle.StateMetaInfo> partitionOffsets =
					operatorStateHandle.getStateNameToPartitionOffsets();

				for (OperatorStateHandle.StateMetaInfo metaInfo : partitionOffsets.values()) {

					// if we find any broadcast state, we cannot take the shortcut and need to go through repartitioning
					if (OperatorStateHandle.Mode.BROADCAST.equals(metaInfo.getDistributionMode())) {
						return opStateRepartitioner.repartitionState(
							chainOpParallelStates,
							newParallelism);
					}
				}

				repackStream.add(Collections.singletonList(operatorStateHandle));
			}
			return repackStream;
		}
	}

	private StateAssignmentOperationUtils() {
	}
}
