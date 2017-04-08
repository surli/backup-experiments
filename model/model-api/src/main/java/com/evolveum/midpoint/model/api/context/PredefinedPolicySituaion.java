/**
 * Copyright (c) 2016 Evolveum
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

import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PolicyConstraintKindType;

/**
 * @author semancik
 *
 */
public enum PredefinedPolicySituaion {
	
	EXCLUSION_VIOLATION(SchemaConstants.MODEL_POLICY_SITUATION_EXCLUSION_VIOLATION, PolicyConstraintKindType.EXCLUSION),
	
	UNDERASSIGNED(SchemaConstants.MODEL_POLICY_SITUATION_UNDERASSIGNED, PolicyConstraintKindType.MIN_ASSIGNEES),
	
	OVERASSIGNED(SchemaConstants.MODEL_POLICY_SITUATION_OVERASSIGNED, PolicyConstraintKindType.MAX_ASSIGNEES),
	
	MODIFIED(SchemaConstants.MODEL_POLICY_SITUATION_MODIFIED, PolicyConstraintKindType.MODIFICATION),
	
	ASSIGNED(SchemaConstants.MODEL_POLICY_SITUATION_ASSIGNED, PolicyConstraintKindType.ASSIGNMENT);
	
	private String url;
	private PolicyConstraintKindType constraintKind;
	
	private PredefinedPolicySituaion(String url, PolicyConstraintKindType constraintKind) {
		this.url = url;
		this.constraintKind = constraintKind;
	}

	public String getUrl() {
		return url;
	}

	public PolicyConstraintKindType getConstraintKind() {
		return constraintKind;
	}

	public static PredefinedPolicySituaion get(PolicyConstraintKindType constraintKind) {
		for (PredefinedPolicySituaion val: PredefinedPolicySituaion.values()) {
			if (val.getConstraintKind() == constraintKind) {
				return val;
			}
		}
		return null;
	}

}
