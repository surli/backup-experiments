/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processor.util.pattern;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processor.util.pattern.PartialFunctions.AdjustRoute;

import java.util.function.BiFunction;

/**
 * <p>RollbackOnFailure can be used as a function context for process patterns such as {@link Put} to provide a configurable error handling.
 *
 * <p>
 *     RollbackOnFailure can add following characteristics to a processor:
 *     <li>When disabled, input FlowFiles caused an error will be routed to 'failure' or 'retry' relationship, based on the type of error.</li>
 *     <li>When enabled, input FlowFiles are kept in the input queue. A ProcessException is thrown to rollback the process session.</li>
 *     <li>It assumes anything happened during a processors onTrigger can rollback, if this is marked as transactional.</li>
 *     <li>If transactional and enabled, even if some FlowFiles are already processed, it rollbacks the session when error occurs.</li>
 *     <li>If not transactional and enabled, it only rollbacks the session when error occurs only if there was no progress.</li>
 * </p>
 *
 * <p>There are two approaches to apply RollbackOnFailure. One is using {@link ExceptionHandler#adjustError(BiFunction)},
 * and the other is implementing processor onTrigger using process patterns such as {@link Put#adjustRoute(AdjustRoute)}. </p>
 *
 * <p>It's also possible to use both approaches. ExceptionHandler can apply when an Exception is thrown immediately, while AdjustRoute respond later but requires less code.</p>
 */
public class RollbackOnFailure {

    private final boolean rollbackOnFailure;
    private final boolean transactional;

    private int processedCount = 0;

    /**
     * Constructor.
     * @param rollbackOnFailure Should be set by user via processor configuration.
     * @param transactional Specify whether a processor is transactional.
     *                      If not, it is important to call {@link #proceed()} after successful execution of processors task,
     *                      that indicates processor made an operation that can not be undone.
     */
    public RollbackOnFailure(boolean rollbackOnFailure, boolean transactional) {
        this.rollbackOnFailure = rollbackOnFailure;
        this.transactional = transactional;
    }

    public static final PropertyDescriptor ROLLBACK_ON_FAILURE = createRollbackOnFailureProperty("");

    public static  PropertyDescriptor createRollbackOnFailureProperty(String additionalDescription) {
        return new PropertyDescriptor.Builder()
                .name("rollback-on-failure")
                .displayName("Rollback On Failure")
                .description("Specify how to handle error." +
                        " By default (false), if an error occurs while processing a FlowFile, the FlowFile will be routed to" +
                        " 'failure' or 'retry' relationship based on error type, and processor can continue with next FlowFile." +
                        " Instead, you may want to rollback currently processed FlowFiles and stop further processing immediately." +
                        " In that case, you can do so by enabling this 'Rollback On Failure' property. " + additionalDescription)
                .allowableValues("true", "false")
                .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
                .defaultValue("false")
                .required(true)
                .build();
    }

    /**
     * Create a function to use with {@link ExceptionHandler} that adjust error type based on functional context.
     */
    public static <FCT extends RollbackOnFailure> BiFunction<FCT, ErrorTypes, ErrorTypes.Result> createAdjustError(final ComponentLog logger) {
        return (c, t) -> {

            ErrorTypes.Result adjusted = null;
            switch (t.destination()) {

                case ProcessException:
                    if (!c.canRollback()) {
                        // If Exception is thrown but the processor is not transactional and processed count > 0, adjust it to failure,
                        // in order to avoid making duplicates in target system, and also for succeeding input to be processed.
                        adjusted = new ErrorTypes.Result(ErrorTypes.Destination.Failure, t.penalty());
                    }
                    break;

                case Failure:
                case Retry:
                    if (c.isRollbackOnFailure() && c.canRollback()) {
                        // Anything other than ProcessException
                        adjusted = new ErrorTypes.Result(ErrorTypes.Destination.ProcessException, t.penalty());
                    }
            }

            if (adjusted != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Adjusted {} to {} based on context rollbackOnFailure={}, processedCount={}, transactional={}",
                            new Object[]{t, adjusted, c.isRollbackOnFailure(), c.getProcessedCount(), c.isTransactional()});
                }
                return adjusted;
            }

            return t.result();
        };
    }

    /**
     * Create an {@link AdjustRoute} function to use with process pattern such as {@link Put} that adjust routed FlowFiles based on context.
     */
    public static <FCT extends RollbackOnFailure> AdjustRoute<FCT> createAdjustRoute(Relationship ... failureRelationships) {
        return (context, session, fc, result) -> {
            if (fc.isRollbackOnFailure() && fc.canRollback()) {
                // Check if route contains failure relationship.
                for (Relationship failureRelationship : failureRelationships) {
                    if (result.contains(failureRelationship)) {
                        throw new ProcessException(String.format(
                                "A FlowFile is routed to %s. Rollback session based on context rollbackOnFailure=%s, processedCount=%d, transactional=%s",
                                failureRelationship.getName(), fc.isRollbackOnFailure(), fc.getProcessedCount(), fc.isTransactional()));
                    }
                }
            }
        };
    }

    public int proceed() {
        return ++processedCount;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public boolean isRollbackOnFailure() {
        return rollbackOnFailure;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public boolean canRollback() {
        return transactional || processedCount == 0;
    }
}
