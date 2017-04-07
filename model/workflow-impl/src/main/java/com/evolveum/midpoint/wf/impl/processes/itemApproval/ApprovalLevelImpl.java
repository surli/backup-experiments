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

package com.evolveum.midpoint.wf.impl.processes.itemApproval;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.wf.impl.processes.common.LightweightObjectRef;
import com.evolveum.midpoint.wf.impl.processes.common.LightweightObjectRefImpl;
import com.evolveum.midpoint.wf.impl.util.SerializationSafeContainer;
import com.evolveum.midpoint.wf.impl.util.SingleItemSerializationSafeContainerImpl;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ApprovalLevelType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ExpressionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LevelEvaluationStrategyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;

import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mederly
 */
public class ApprovalLevelImpl implements ApprovalLevel, Serializable {

    private static final long serialVersionUID = 1989606281279792045L;

	private final int order;
    private String name;
    private String description;
    private List<LightweightObjectRefImpl> approverRefs = new ArrayList<>();
    private List<SerializationSafeContainer<ExpressionType>> approverExpressions = new ArrayList<>();
    private LevelEvaluationStrategyType evaluationStrategy;
    private SerializationSafeContainer<ExpressionType> automaticallyApproved;

    private transient PrismContext prismContext;

    public ApprovalLevelImpl(ApprovalLevelType levelType, PrismContext prismContext,
                             RelationResolver relationResolver) {

        Validate.notNull(prismContext, "prismContext must not be null");

		this.order = levelType.getOrder() != null ? levelType.getOrder() : 0;
        this.name = levelType.getName();
        this.description = levelType.getDescription();

        setPrismContext(prismContext);

	    levelType.getApproverRef().forEach(this::addApproverRef);
	    relationResolver.getApprovers(levelType.getApproverRelation()).forEach(this::addApproverRef);
	    levelType.getApproverExpression().forEach(this::addApproverExpression);
        this.evaluationStrategy = levelType.getEvaluationStrategy();
        setAutomaticallyApproved(levelType.getAutomaticallyApproved());
    }

    //default: all approvers in one level, evaluation strategy = allMustApprove
    public ApprovalLevelImpl(List<ObjectReferenceType> approverRefList, List<ExpressionType> approverExpressionList, ExpressionType automaticallyApproved, PrismContext prismContext) {

        Validate.notNull(prismContext, "prismContext must not be null");

        setPrismContext(prismContext);

		order = 0;

        if (approverRefList != null) {
	        approverRefList.forEach(this::addApproverRef);
        }
        if (approverExpressionList != null) {
	        approverExpressionList.forEach(this::addApproverExpression);
        }
        setEvaluationStrategy(LevelEvaluationStrategyType.ALL_MUST_AGREE);
        setAutomaticallyApproved(automaticallyApproved);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<? extends LightweightObjectRef> getApproverRefs() {
        return approverRefs;
    }

    @Override
    public List<ExpressionType> getApproverExpressions() {
        List<ExpressionType> retval = new ArrayList<>();
        for (SerializationSafeContainer<ExpressionType> approverExpression : approverExpressions) {
            if (prismContext != null && approverExpression.getPrismContext() == null) {
                approverExpression.setPrismContext(prismContext);
            }
            retval.add(approverExpression.getValue());
        }
        return Collections.unmodifiableList(retval);
    }

    @Override
    public LevelEvaluationStrategyType getEvaluationStrategy() {
        return evaluationStrategy;
    }

    public void setEvaluationStrategy(LevelEvaluationStrategyType evaluationStrategy) {
        this.evaluationStrategy = evaluationStrategy;
    }

    @Override
    public ExpressionType getAutomaticallyApproved() {
        if (automaticallyApproved == null) {
            return null;
        }
        if (prismContext != null && automaticallyApproved.getPrismContext() == null) {
            automaticallyApproved.setPrismContext(prismContext);
        }
        return automaticallyApproved.getValue();
    }

    public void setAutomaticallyApproved(ExpressionType automaticallyApproved) {
        this.automaticallyApproved = new SingleItemSerializationSafeContainerImpl<>(automaticallyApproved, prismContext);
    }

	public int getOrder() {
		return order;
	}

	@Override
    public PrismContext getPrismContext() {
        return prismContext;
    }

    @Override
    public void setPrismContext(PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    @Override
    public ApprovalLevelType toApprovalLevelType(PrismContext prismContext) {
        ApprovalLevelType levelType = new ApprovalLevelType();          // is this ok?
		levelType.setOrder(getOrder());
        levelType.setName(getName());
        levelType.setDescription(getDescription());
        levelType.setAutomaticallyApproved(getAutomaticallyApproved());
        levelType.setEvaluationStrategy(getEvaluationStrategy());
        for (LightweightObjectRef approverRef : approverRefs) {
            levelType.getApproverRef().add(approverRef.toObjectReferenceType());
        }
        for (SerializationSafeContainer<ExpressionType> approverExpression : approverExpressions) {
            approverExpression.setPrismContext(prismContext);
            levelType.getApproverExpression().add(approverExpression.getValue());
        }
        return levelType;
    }

    public void addApproverRef(ObjectReferenceType approverRef) {
        approverRefs.add(new LightweightObjectRefImpl(approverRef));
    }

    public void addApproverExpression(ExpressionType expressionType) {
        approverExpressions.add(new SingleItemSerializationSafeContainerImpl<ExpressionType>(expressionType, prismContext));
    }

    @Override
    public String toString() {
        return "ApprovalLevelImpl{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", evaluationStrategy=" + evaluationStrategy +
                ", automaticallyApproved=" + automaticallyApproved +
                ", approverRefs=" + approverRefs +
                ", approverExpressions=" + approverExpressions +
                '}';
    }

	public boolean isEmpty() {
        return approverRefs.isEmpty() && approverExpressions.isEmpty();
	}
}
