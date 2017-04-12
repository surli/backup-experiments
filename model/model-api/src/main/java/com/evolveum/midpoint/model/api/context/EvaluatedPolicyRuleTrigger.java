/*
 * Copyright (c) 2016-2017 Evolveum
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
package com.evolveum.midpoint.model.api.context;

import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.schema.util.PolicyRuleTypeUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractPolicyConstraintType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EvaluatedPolicyRuleTriggerType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PolicyConstraintKindType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Description of a situation that caused a trigger of the policy rule.
 * 
 * @author semancik
 */
public class EvaluatedPolicyRuleTrigger<CT extends AbstractPolicyConstraintType> implements DebugDumpable {
	
	@NotNull private final PolicyConstraintKindType constraintKind;
	@NotNull private final CT constraint;
	private final String message;

	public EvaluatedPolicyRuleTrigger(@NotNull PolicyConstraintKindType constraintKind, @NotNull CT constraint, String message) {
		this.constraintKind = constraintKind;
		this.constraint = constraint;
		this.message = message;
	}

	/**
	 * The kind of constraint that caused the trigger.
	 */
	@NotNull
	public PolicyConstraintKindType getConstraintKind() {
		return constraintKind;
	}

	@NotNull
	public AbstractPolicyConstraintType getConstraint() {
		return constraint;
	}

	/**
	 * Human-readable message associated with this trigger. The message
	 * explain why the rule was triggered. It can be used
	 * in the logs, as an error message, in the audit trail
	 * and so on.
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof EvaluatedPolicyRuleTrigger))
			return false;
		EvaluatedPolicyRuleTrigger that = (EvaluatedPolicyRuleTrigger) o;
		return constraintKind == that.constraintKind &&
				Objects.equals(constraint, that.constraint) &&
				Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(constraintKind, constraint, message);
	}

	@Override
	public String toString() {
		return "EvaluatedPolicyRuleTrigger(" + constraintKind + ": " + message + ")";
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.debugDumpLabelLn(sb, getClass().getSimpleName(), indent);
		debugDumpCommon(sb, indent + 1);
		debugDumpSpecific(sb, indent + 1);
		return sb.toString();
	}

	private void debugDumpCommon(StringBuilder sb, int indent) {
		DebugUtil.debugDumpWithLabelToStringLn(sb, "constraintKind", constraintKind, indent);
		DebugUtil.debugDumpWithLabelToStringLn(sb, "constraint", constraint, indent);
		DebugUtil.debugDumpWithLabelLn(sb, "message", message, indent);
	}

	protected void debugDumpSpecific(StringBuilder sb, int indent) {
	}

	public String toDiagShortcut() {
		return PolicyRuleTypeUtil.toDiagShortcut(constraintKind);
	}

	public EvaluatedPolicyRuleTriggerType toEvaluatedPolicyRuleTriggerType(EvaluatedPolicyRule owningRule) {
		EvaluatedPolicyRuleTriggerType rv = new EvaluatedPolicyRuleTriggerType();
		fillCommonContent(rv, owningRule);
		return rv;
	}

	protected void fillCommonContent(EvaluatedPolicyRuleTriggerType tt, EvaluatedPolicyRule owningRule) {
		tt.setRuleName(owningRule.getName());
		tt.setConstraintKind(constraintKind);
		tt.setConstraint(constraint);
		tt.setMessage(message);
		if (owningRule.getAssignmentPath() != null) {
			tt.setAssignmentPath(owningRule.getAssignmentPath().toAssignmentPathType());
		}
		ObjectType directOwner = owningRule.getDirectOwner();
		if (directOwner != null) {
			tt.setDirectOwnerRef(ObjectTypeUtil.createObjectRef(directOwner));
			tt.setDirectOwnerDisplayName(ObjectTypeUtil.getDisplayName(directOwner));
		}
	}
}
