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

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;

import java.util.List;

/**
 * This class contains various partial functions those are reusable among process patterns.
 */
public class PartialFunctions {

    @FunctionalInterface
    public interface InitConnection<FC, C> {
        C apply(ProcessContext context, ProcessSession session, FC functionContext) throws ProcessException;
    }

    @FunctionalInterface
    public interface FetchFlowFiles<FC> {
        List<FlowFile> apply(ProcessContext context, ProcessSession session, FC functionContext, RoutingResult result) throws ProcessException;
    }

    @FunctionalInterface
    public interface OnCompleted<FC, C> {
        void apply(ProcessContext context, ProcessSession session, FC functionContext, C connection) throws ProcessException;
    }

    @FunctionalInterface
    public interface OnFailed<FC, C> {
        void apply(ProcessContext context, ProcessSession session, FC functionContext, C connection, Exception e) throws ProcessException;
    }

    @FunctionalInterface
    public interface Cleanup<FC, C> {
        void apply(ProcessContext context, ProcessSession session, FC functionContext, C connection) throws ProcessException;
    }

    @FunctionalInterface
    public interface FlowFileGroup {
        List<FlowFile> getFlowFiles();
    }

    @FunctionalInterface
    public interface AdjustRoute<FC> {
        void apply(ProcessContext context, ProcessSession session, FC functionContext, RoutingResult result) throws ProcessException;
    }

    @FunctionalInterface
    public interface TransferFlowFiles<FC> {
        void apply(ProcessContext context, ProcessSession session, FC functionContext, RoutingResult result) throws ProcessException;

        default TransferFlowFiles<FC> andThen(TransferFlowFiles<FC> after) {
            return (context, session, functionContext, result) -> {
                apply(context, session, functionContext, result);
                after.apply(context, session, functionContext, result);
            };
        }
    }

    public static <FCT> PartialFunctions.FetchFlowFiles<FCT> fetchSingleFlowFile() {
        return (context, session, functionContext, result) -> session.get(1);
    }

    public static <FCT> PartialFunctions.TransferFlowFiles<FCT> transferRoutedFlowFiles() {
        return (context, session, functionContext, result)
                -> result.getRoutedFlowFiles().forEach(((relationship, routedFlowFiles)
                -> session.transfer(routedFlowFiles, relationship)));
    }

}
