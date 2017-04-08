/*
 * Copyright (c) 2010-2015 Evolveum
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

package com.evolveum.midpoint.wf.impl.processes.itemApproval;

import com.evolveum.midpoint.model.common.expression.Expression;
import com.evolveum.midpoint.model.common.expression.ExpressionEvaluationContext;
import com.evolveum.midpoint.model.common.expression.ExpressionFactory;
import com.evolveum.midpoint.model.common.expression.ExpressionVariables;
import com.evolveum.midpoint.model.impl.expr.ModelExpressionThreadLocalHolder;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PrismValueDeltaSetTriple;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.wf.impl.processes.common.*;
import com.evolveum.midpoint.wf.impl.util.MiscDataUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.lang.Validate;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import java.util.*;

import static com.evolveum.midpoint.schema.constants.SchemaConstants.C_OBJECT;
import static com.evolveum.midpoint.schema.constants.SchemaConstants.C_REQUESTER;
import static com.evolveum.midpoint.schema.constants.SchemaConstants.C_TARGET;
import static com.evolveum.midpoint.wf.impl.processes.common.SpringApplicationContextHolder.*;

/**
 * @author mederly
 */
public class InitializeLoopThroughApproversInLevel implements JavaDelegate {

    private static final Trace LOGGER = TraceManager.getTrace(InitializeLoopThroughApproversInLevel.class);

    private ExpressionFactory expressionFactory;

    public void execute(DelegateExecution execution) {

        LOGGER.trace("Executing the delegate; execution = {}", execution);

        OperationResult result = new OperationResult("dummy");
        Task wfTask = ActivitiUtil.getTask(execution, result);
		Task opTask = getTaskManager().createTaskInstance();

        ExpressionVariables expressionVariables = null;

        ApprovalLevelImpl level = (ApprovalLevelImpl) execution.getVariable(ProcessVariableNames.LEVEL);
        Validate.notNull(level, "Variable " + ProcessVariableNames.LEVEL + " is undefined");
        level.setPrismContext(getPrismContext());

        List<Decision> decisionList = new ArrayList<Decision>();
        boolean preApproved = false;

        if (level.getAutomaticallyApproved() != null) {
            try {
                opTask.setChannel(wfTask.getChannel());
                expressionVariables = getDefaultVariables(execution, wfTask, result);
                preApproved = evaluateBooleanExpression(level.getAutomaticallyApproved(), expressionVariables, execution, opTask, result);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Pre-approved = " + preApproved + " for level " + level);
                }
            } catch (Exception e) {     // todo
                throw new SystemException("Couldn't evaluate auto-approval expression", e);
            }
        }

        Set<LightweightObjectRef> approverRefs = new HashSet<LightweightObjectRef>();

        if (!preApproved) {
            approverRefs.addAll(level.getApproverRefs());

            if (!level.getApproverExpressions().isEmpty()) {
                try {
                    expressionVariables = getDefaultVariablesIfNeeded(expressionVariables, execution, wfTask, result);
                    approverRefs.addAll(evaluateExpressions(level.getApproverExpressions(), expressionVariables, execution, opTask, result));
                } catch (Exception e) {     // todo
                    throw new SystemException("Couldn't evaluate approvers expressions", e);
                }
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Approvers at the level " + level + " are: " + approverRefs);
            }
            if (approverRefs.isEmpty()) {
                LOGGER.warn("No approvers at the level '" + level.getName() + "' for process " + execution.getVariable(CommonProcessVariableNames.VARIABLE_PROCESS_INSTANCE_NAME) + " (id " + execution.getProcessInstanceId() + ")");
            }
        }

        Boolean stop;
        if (approverRefs.isEmpty() || preApproved) {
            stop = Boolean.TRUE;
        } else {
            stop = Boolean.FALSE;
        }

        execution.setVariableLocal(ProcessVariableNames.DECISIONS_IN_LEVEL, decisionList);
        execution.setVariableLocal(ProcessVariableNames.APPROVERS_IN_LEVEL, new ArrayList<>(approverRefs));
        execution.setVariableLocal(ProcessVariableNames.LOOP_APPROVERS_IN_LEVEL_STOP, stop);
    }

    private Collection<? extends LightweightObjectRef> evaluateExpressions(List<ExpressionType> approverExpressionList, 
    		ExpressionVariables expressionVariables, DelegateExecution execution, Task task, OperationResult result) throws ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
        List<LightweightObjectRef> retval = new ArrayList<>();
        for (ExpressionType approverExpression : approverExpressionList) {
            retval.addAll(evaluateExpression(approverExpression, expressionVariables, execution, task, result));
        }
        return retval;
    }

    private Collection<LightweightObjectRef> evaluateExpression(ExpressionType approverExpression, ExpressionVariables expressionVariables, 
    		DelegateExecution execution, Task task, OperationResult result) throws ObjectNotFoundException, SchemaException, ExpressionEvaluationException {

        if (expressionFactory == null) {
            expressionFactory = getExpressionFactory();
        }

        PrismContext prismContext = expressionFactory.getPrismContext();
        QName approverOidName = new QName(SchemaConstants.NS_C, "approverOid");
        PrismPropertyDefinition<String> approverOidDef = new PrismPropertyDefinitionImpl(approverOidName, DOMUtil.XSD_STRING, prismContext);
        Expression<PrismPropertyValue<String>,PrismPropertyDefinition<String>> expression = expressionFactory.makeExpression(approverExpression, approverOidDef, "approverExpression", task, result);
        ExpressionEvaluationContext params = new ExpressionEvaluationContext(null, expressionVariables, "approverExpression", task, result);
        PrismValueDeltaSetTriple<PrismPropertyValue<String>> exprResult = ModelExpressionThreadLocalHolder.evaluateExpressionInContext(expression, params, task, result);

        List<LightweightObjectRef> retval = new ArrayList<LightweightObjectRef>();
        for (PrismPropertyValue<String> item : exprResult.getZeroSet()) {
            LightweightObjectRef ort = new LightweightObjectRefImpl(item.getValue());
            retval.add(ort);
        }
        return retval;

    }

    private boolean evaluateBooleanExpression(ExpressionType expressionType, ExpressionVariables expressionVariables, 
    		DelegateExecution execution, Task task, OperationResult result) throws ObjectNotFoundException, SchemaException, ExpressionEvaluationException {

        if (expressionFactory == null) {
            expressionFactory = getExpressionFactory();
        }

        PrismContext prismContext = expressionFactory.getPrismContext();
        QName resultName = new QName(SchemaConstants.NS_C, "result");
        PrismPropertyDefinition<Boolean> resultDef = new PrismPropertyDefinitionImpl(resultName, DOMUtil.XSD_BOOLEAN, prismContext);
        Expression<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> expression = expressionFactory.makeExpression(expressionType, resultDef, "automatic approval expression", task, result);
        ExpressionEvaluationContext params = new ExpressionEvaluationContext(null, expressionVariables, 
        		"automatic approval expression", task, result);

		PrismValueDeltaSetTriple<PrismPropertyValue<Boolean>> exprResultTriple =
				ModelExpressionThreadLocalHolder.evaluateExpressionInContext(expression, params, task, result);

        Collection<PrismPropertyValue<Boolean>> exprResult = exprResultTriple.getZeroSet();
        if (exprResult.size() == 0) {
            return false;
        } else if (exprResult.size() > 1) {
            throw new IllegalStateException("Auto-approval expression should return exactly one boolean value; it returned " + exprResult.size() + " ones");
        }
        Boolean boolResult = exprResult.iterator().next().getValue();
        return boolResult != null ? boolResult : false;
    }

	private ExpressionFactory getExpressionFactory() {
        LOGGER.trace("Getting expressionFactory");
        ExpressionFactory ef = getApplicationContext().getBean("expressionFactory", ExpressionFactory.class);
        if (ef == null) {
            throw new IllegalStateException("expressionFactory bean cannot be found");
        }
        return ef;
    }


    private ExpressionVariables getDefaultVariablesIfNeeded(ExpressionVariables variables, DelegateExecution execution, Task wfTask, OperationResult result) throws SchemaException, ObjectNotFoundException {
        if (variables != null) {
            return variables;
        } else {
            return getDefaultVariables(execution, wfTask, result);
        }
    }

    private ExpressionVariables getDefaultVariables(DelegateExecution execution, Task wfTask, OperationResult result) throws SchemaException, ObjectNotFoundException {

        RepositoryService repositoryService = getCacheRepositoryService();
        MiscDataUtil miscDataUtil = getMiscDataUtil();

        ExpressionVariables variables = new ExpressionVariables();

		ObjectReferenceType requesterRef = wfTask.getWorkflowContext().getRequesterRef();
		variables.addVariableDefinition(C_REQUESTER, miscDataUtil.resolveObjectReference(requesterRef, result));

		ObjectReferenceType objectRef = wfTask.getWorkflowContext().getObjectRef();
		variables.addVariableDefinition(C_OBJECT, miscDataUtil.resolveObjectReference(objectRef, result));

		ObjectReferenceType targetRef = wfTask.getWorkflowContext().getTargetRef();		// might be null
		variables.addVariableDefinition(C_TARGET, miscDataUtil.resolveObjectReference(targetRef, result));

		ObjectDelta objectDelta = null;
        try {
            objectDelta = miscDataUtil.getFocusPrimaryDelta(wfTask.getWorkflowContext(), true);
        } catch (JAXBException e) {
            throw new SchemaException("Couldn't get object delta: " + e.getMessage(), e);
        }
        variables.addVariableDefinition(SchemaConstants.T_OBJECT_DELTA, objectDelta);

        // todo other variables?

        return variables;
    }


}