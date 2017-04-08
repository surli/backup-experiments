/*
 * Copyright (c) 2010-2016 Evolveum
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
package com.evolveum.midpoint.model.impl.lens;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.model.api.context.AssignmentPathSegment;
import com.evolveum.midpoint.model.api.context.EvaluationOrder;
import com.evolveum.midpoint.model.api.util.DeputyUtils;
import com.evolveum.midpoint.model.common.expression.ItemDeltaItem;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrderConstraintsType;

/**
 * @author semancik
 *
 */
public class AssignmentPathSegmentImpl implements AssignmentPathSegment {
	
	private ItemDeltaItem<PrismContainerValue<AssignmentType>,PrismContainerDefinition<AssignmentType>> assignmentIdi;
	private QName relation;
	private ObjectType target;
	private ObjectType source;
	private boolean validityOverride = false;
	private EvaluationOrder evaluationOrder;
	private ObjectType varThisObject;
	private Boolean isMatchingOrder = null;
	private Boolean isMatchingOrderPlusOne = null;
	private boolean processMembership = false;
	private final boolean isAssignment;
	
	AssignmentPathSegmentImpl(ItemDeltaItem<PrismContainerValue<AssignmentType>,PrismContainerDefinition<AssignmentType>> assignmentIdi,
	                          boolean isAssignment) {
		super();
		this.assignmentIdi = assignmentIdi;
		this.isAssignment = isAssignment;
	}

	@Override
	public boolean isAssignment() {
		return isAssignment;
	}

	public ItemDeltaItem<PrismContainerValue<AssignmentType>,PrismContainerDefinition<AssignmentType>> getAssignmentIdi() {
		return assignmentIdi;
	}

	@Override
	public AssignmentType getAssignment() {
		if (assignmentIdi == null || assignmentIdi.getItemNew() == null || assignmentIdi.getItemNew().isEmpty()) {
			return null;
		}
		return ((PrismContainer<AssignmentType>) assignmentIdi.getItemNew()).getValue().asContainerable();
	}

	public void setAssignmentIdi(ItemDeltaItem<PrismContainerValue<AssignmentType>,PrismContainerDefinition<AssignmentType>> assignmentIdi) {
		this.assignmentIdi = assignmentIdi;
	}

	@Override
	public QName getRelation() {
		return relation;
	}

	public void setRelation(QName relation) {
		this.relation = relation;
	}

	@Override
	public ObjectType getTarget() {
		return target;
	}

	public void setTarget(ObjectType target) {
		this.target = target;
	}
	
	@Override
	public ObjectType getSource() {
		return source;
	}

	public void setSource(ObjectType source) {
		this.source = source;
	}
	
	public boolean isValidityOverride() {
		return validityOverride;
	}

	public void setValidityOverride(boolean validityOverride) {
		this.validityOverride = validityOverride;
	}

	@Override
	public EvaluationOrder getEvaluationOrder() {
		return evaluationOrder;
	}

	public void setEvaluationOrder(EvaluationOrder evaluationOrder) {
		this.evaluationOrder = evaluationOrder;
	}

	@Override
	public ObjectType getOrderOneObject() {
		return varThisObject;
	}

	public void setOrderOneObject(ObjectType varThisObject) {
		this.varThisObject = varThisObject;
	}
	
	public boolean isProcessMembership() {
		return processMembership;
	}

	public void setProcessMembership(boolean processMembership) {
		this.processMembership = processMembership;
	}

	public boolean isMatchingOrder() {
		if (isMatchingOrder == null) {
			isMatchingOrder = computeMatchingOrder(0);
		}
		return isMatchingOrder;
	}
	
	public boolean isMatchingOrderPlusOne() {
		if (isMatchingOrderPlusOne == null) {
			isMatchingOrderPlusOne = computeMatchingOrder(1);
		}
		return isMatchingOrderPlusOne;
	}

	private boolean computeMatchingOrder(int offset) {
		AssignmentType assignmentType = getAssignment();
		if (assignmentType.getOrder() == null && assignmentType.getOrderConstraint().isEmpty()) {
			// compatibility
			return evaluationOrder.getSummaryOrder() - offset == 1;
		}
		if (assignmentType.getOrder() != null) {
			if (evaluationOrder.getSummaryOrder() - offset != assignmentType.getOrder()) {
				return false;
			}
		}
		for (OrderConstraintsType orderConstraint: assignmentType.getOrderConstraint()) {
			if (!isMatchingConstraint(orderConstraint, offset)) {
				return false;
			}
		}
		return true;
	}

	private boolean isMatchingConstraint(OrderConstraintsType orderConstraint, int offset) {
		int evaluationOrderInt = evaluationOrder.getMatchingRelationOrder(orderConstraint.getRelation()) - offset;
		if (orderConstraint.getOrder() != null) {
			return orderConstraint.getOrder() == evaluationOrderInt;
		} else {
			int orderMin = 1;
			int orderMax = 1;
			if (orderConstraint.getOrderMin() != null) {
				orderMin = XsdTypeMapper.multiplicityToInteger(orderConstraint.getOrderMin());
			}
			if (orderConstraint.getOrderMax() != null) {
				orderMax = XsdTypeMapper.multiplicityToInteger(orderConstraint.getOrderMax());
			}
			return XsdTypeMapper.isMatchingMultiplicity(evaluationOrderInt, orderMin, orderMax);
		}
	}
	
	@Override
	public boolean isDelegation() {
		return DeputyUtils.isDelegationRelation(relation);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assignmentIdi == null) ? 0 : assignmentIdi.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssignmentPathSegmentImpl other = (AssignmentPathSegmentImpl) obj;
		if (assignmentIdi == null) {
			if (other.assignmentIdi != null)
				return false;
		} else if (!assignmentIdi.equals(other.assignmentIdi))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("AssignmentPathSegment(");
		sb.append(evaluationOrder);
		if (isMatchingOrder == Boolean.TRUE) {
			sb.append("(match)");
		};
		sb.append(": ");
		sb.append(source).append(" ");
		PrismContainer<AssignmentType> assignment = (PrismContainer<AssignmentType>) assignmentIdi.getAnyItem();
		if (assignment != null) {
			AssignmentType assignmentType = assignment.getValue().asContainerable();
			if (assignmentType.getConstruction() != null) {
				sb.append("Constr '"+assignmentType.getConstruction().getDescription()+"' ");
			}
		}
		if (target != null) {
			sb.append("-[");
			if (relation != null) {
				sb.append(relation.getLocalPart());
			}
			sb.append("]-> ").append(target);
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.debugDumpLabel(sb, "AssignmentPathSegment", indent);
		sb.append("\n");
		DebugUtil.debugDumpWithLabelLn(sb, "isMatchingOrder", isMatchingOrder, indent + 1);
		DebugUtil.debugDumpWithLabelLn(sb, "processMembership", processMembership, indent + 1);
		DebugUtil.debugDumpWithLabelLn(sb, "validityOverride", validityOverride, indent + 1);
		DebugUtil.debugDumpWithLabelLn(sb, "evaluationOrder", (DebugDumpable)evaluationOrder, indent + 1);
		DebugUtil.debugDumpWithLabelLn(sb, "assignment", assignmentIdi.toString(), indent + 1);
		DebugUtil.debugDumpWithLabelLn(sb, "relation", relation, indent + 1);
		DebugUtil.debugDumpWithLabelLn(sb, "target", target==null?"null":target.toString(), indent + 1);
		DebugUtil.debugDumpWithLabelLn(sb, "source", source==null?"null":source.toString(), indent + 1);
		DebugUtil.debugDumpWithLabel(sb, "varThisObject", varThisObject==null?"null":varThisObject.toString(), indent + 1);
		return sb.toString();
	}
}
