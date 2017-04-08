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

package com.evolveum.midpoint.model.impl.lens.projector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.evolveum.midpoint.common.policy.StringPolicyUtils;
import com.evolveum.midpoint.model.impl.ModelObjectResolver;
import com.evolveum.midpoint.model.impl.lens.LensContext;
import com.evolveum.midpoint.model.impl.lens.LensFocusContext;
import com.evolveum.midpoint.model.impl.lens.LensObjectDeltaOperation;
import com.evolveum.midpoint.model.impl.lens.LensProjectionContext;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.PrismReference;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.crypto.EncryptionException;
import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.prism.delta.ChangeType;
import com.evolveum.midpoint.prism.delta.ContainerDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.delta.ReferenceDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.PolicyViolationException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CharacterClassType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LimitationsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordHistoryEntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordLifeTimeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.StringLimitType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.StringPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValuePolicyType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;


@Component
public class PasswordPolicyProcessor {
	
	private static final String DOT_CLASS = PasswordPolicyProcessor.class.getName() + ".";
	private static final String OPERATION_PASSWORD_VALIDATION = DOT_CLASS + "passwordValidation";
	
	private static final Trace LOGGER = TraceManager.getTrace(PasswordPolicyProcessor.class);
	
	@Autowired(required=true)
	private PrismContext prismContext;
	
	@Autowired(required = true)
	Protector protector;
	
	@Autowired(required = true)
	ModelObjectResolver resolver;	
	
	<F extends FocusType> void processPasswordPolicy(LensFocusContext<F> focusContext, 
			LensContext<F> context, XMLGregorianCalendar now, Task task, OperationResult result)
			throws PolicyViolationException, SchemaException {
		
		if (!UserType.class.isAssignableFrom(focusContext.getObjectTypeClass())) {
			LOGGER.trace("Skipping processing password policies because focus is not user");
			return;
		}
		
//		PrismProperty<PasswordType> password = getPassword(focusContext);
		ObjectDelta userDelta = focusContext.getDelta();

		if (userDelta == null) {
			LOGGER.trace("Skipping processing password policies. User delta not specified.");
			return;
		}
		
		if (userDelta.isDelete()) {
			LOGGER.trace("Skipping processing password policies. User will be deleted.");
			return;
		}

		PrismProperty<ProtectedStringType> passwordValueProperty = null;
		boolean isPasswordChange = false;
		PrismObject<F> user;
		if (ChangeType.ADD == userDelta.getChangeType()) {
			user = focusContext.getDelta().getObjectToAdd();
			if (user != null) {
				passwordValueProperty = user.findProperty(SchemaConstants.PATH_PASSWORD_VALUE);
			}
			if (passwordValueProperty == null){
				if (wasExecuted(userDelta, focusContext)){
					LOGGER.trace("Skipping processing password policies. User addition was already executed.");
					return;
				}
			}
		} else if (ChangeType.MODIFY == userDelta.getChangeType()) {
			PropertyDelta<ProtectedStringType> passwordValueDelta;
			passwordValueDelta = userDelta.findPropertyDelta(SchemaConstants.PATH_PASSWORD_VALUE);
			if (passwordValueDelta == null) {
				LOGGER.trace("Skipping processing password policies. User delta does not contain password change.");
				return;
			}
			if (userDelta.getChangeType() == ChangeType.MODIFY) {
				if (passwordValueDelta.isAdd()) {
					passwordValueProperty = (PrismProperty<ProtectedStringType>) passwordValueDelta.getItemNewMatchingPath(null);
					isPasswordChange = true;
				} else if (passwordValueDelta.isDelete()) {
					passwordValueProperty = null;
				} else {
					passwordValueProperty = (PrismProperty<ProtectedStringType>) passwordValueDelta.getItemNewMatchingPath(null);
					isPasswordChange = true;
				}
			} else {
				passwordValueProperty = (PrismProperty<ProtectedStringType>) passwordValueDelta.getItemNewMatchingPath(null);
			}
		}
		
		ValuePolicyType passwordPolicy;
		if (focusContext.getOrgPasswordPolicy() == null) {
			passwordPolicy = determineValuePolicy(userDelta, focusContext.getObjectAny(), context, task, result);
			focusContext.setOrgPasswordPolicy(passwordPolicy);
		} else {
			passwordPolicy = focusContext.getOrgPasswordPolicy();
		}
		
		processPasswordPolicy(passwordPolicy, focusContext.getObjectOld(), passwordValueProperty, result);
		
		if (passwordValueProperty != null && isPasswordChange) {
			processPasswordHistoryDeltas(focusContext, context, now, task, result);
		}

	}
	
	private <F extends FocusType> void processPasswordPolicy(ValuePolicyType passwordPolicy, PrismObject<F> focus, PrismProperty<ProtectedStringType> passwordProperty, OperationResult result)
			throws PolicyViolationException, SchemaException {

		if (passwordPolicy == null) {
			LOGGER.trace("Skipping processing password policies. Password policy not specified.");
			return;
		}

        String passwordValue = determinePasswordValue(passwordProperty);
        PasswordType currentPasswordType = determineCurrentPassword(focus);
       
        boolean isValid = validatePassword(passwordValue, currentPasswordType, passwordPolicy, result);

		if (!isValid) {
			result.computeStatus();
			throw new PolicyViolationException("Provided password does not satisfy password policies. " + result.getMessage());
		}
	}

	private <F extends FocusType> PasswordType determineCurrentPassword(PrismObject<F> focus) {
		if (focus == null) {
			return null;
		}
		PasswordType currentPasswordType = null;

		if (focus.getCompileTimeClass().equals(UserType.class)) {
			CredentialsType credentials = ((UserType)focus.asObjectable()).getCredentials();
			if (credentials != null) {
				currentPasswordType = credentials.getPassword();
			}        	
        }
		return currentPasswordType;
	}

	private <F extends FocusType> boolean wasExecuted(ObjectDelta<UserType> userDelta, LensFocusContext<F> focusContext){
		
		for (LensObjectDeltaOperation<F> executedDeltaOperation : focusContext.getExecutedDeltas()){
			ObjectDelta<F> executedDelta = executedDeltaOperation.getObjectDelta();
			if (!executedDelta.isAdd()){
				continue;
			} else if (executedDelta.getObjectToAdd() != null && executedDelta.getObjectTypeClass().equals(UserType.class)){
				return true;
			}
		}
		
		return false;
	}
	
	//TODO: maybe some caching of orgs?????
	protected <T extends ObjectType, F extends FocusType> ValuePolicyType determineValuePolicy(ObjectDelta<F> userDelta, PrismObject<T> object, LensContext<F> context, Task task, OperationResult result) throws SchemaException{
		//check the modification of organization first
		ValuePolicyType valuePolicy = determineValuePolicy(userDelta, task, result);
		
		//if null, check the existing organization
		if (valuePolicy == null){
			valuePolicy = determineValuePolicy(object, task, result);
		}
		
		//if still null, just use global policy
		if (valuePolicy == null){
			valuePolicy = context.getEffectivePasswordPolicy();
		}
		
		if (valuePolicy != null){
			LOGGER.trace("Value policy {} will be user to check password.", valuePolicy.getName().getOrig());
		}
		
		return valuePolicy;
	}
	
	protected <F extends FocusType> ValuePolicyType determineValuePolicy(ObjectDelta<F> userDelta, Task task, OperationResult result)
			throws SchemaException {
		if (userDelta == null) {
			return null;
		}
		ReferenceDelta orgDelta = userDelta.findReferenceModification(UserType.F_PARENT_ORG_REF);

		LOGGER.trace("Determining password policy from org delta.");
		if (orgDelta == null) {
			return null;
		}

		PrismReferenceValue orgRefValue = orgDelta.getAnyValue();
		if (orgRefValue == null) {		// delta may be of type "replace to null"
			return null;
		}

		ValuePolicyType passwordPolicy = null;
		try {
			PrismObject<OrgType> org = resolver.resolve(orgRefValue,
					"resolving parent org ref", null, null, result);
			OrgType orgType = org.asObjectable();
			ObjectReferenceType ref = orgType.getPasswordPolicyRef();
			if (ref != null) {
				LOGGER.trace("Org {} has specified password policy.", orgType);
				passwordPolicy = resolver.resolve(ref, ValuePolicyType.class, null,
						"resolving password policy for organization", task, result);
				LOGGER.trace("Resolved password policy {}", passwordPolicy);
			}

			if (passwordPolicy == null) {
				passwordPolicy = determineValuePolicy(org, task, result);
			}

		} catch (ObjectNotFoundException e) {
			throw new IllegalStateException(e);
		}

		return passwordPolicy;
	}
	
	private ValuePolicyType determineValuePolicy(PrismObject object, Task task, OperationResult result)
			throws SchemaException {
		LOGGER.trace("Determining password policies from object: {}", ObjectTypeUtil.toShortString(object));
		PrismReference orgRef = object.findReference(ObjectType.F_PARENT_ORG_REF);
		if (orgRef == null) {
			return null;
		}
		List<PrismReferenceValue> orgRefValues = orgRef.getValues();
		ValuePolicyType resultingValuePolicy = null;
		List<PrismObject<OrgType>> orgs = new ArrayList<PrismObject<OrgType>>();
		try {
			for (PrismReferenceValue orgRefValue : orgRefValues) {
				if (orgRefValue != null) {

					PrismObject<OrgType> org = resolver.resolve(orgRefValue, "resolving parent org ref", null, null, result);
					orgs.add(org);
					ValuePolicyType valuePolicy = resolvePolicy(org, task, result);

					if (valuePolicy != null) {
						if (resultingValuePolicy == null) {
							resultingValuePolicy = valuePolicy;
						} else if (!StringUtils.equals(valuePolicy.getOid(), resultingValuePolicy.getOid())) {
							throw new IllegalStateException(
									"Found more than one policy while trying to validate user's password. Please check your configuration");
						}
					}
				}
			}
		} catch (ObjectNotFoundException ex) {
			throw new IllegalStateException(ex);
		}
		// go deeper
		if (resultingValuePolicy == null) {
			for (PrismObject<OrgType> orgType : orgs) {
				resultingValuePolicy = determineValuePolicy(orgType, task, result);
				if (resultingValuePolicy != null) {
					return resultingValuePolicy;
				}
			}
		}
		return resultingValuePolicy;
	}
	
	private ValuePolicyType resolvePolicy(PrismObject<OrgType> org, Task task, OperationResult result)
			throws SchemaException {
		try {
			OrgType orgType = org.asObjectable();
			ObjectReferenceType ref = orgType.getPasswordPolicyRef();
			if (ref == null) {
				return null;
			}

			return resolver.resolve(ref, ValuePolicyType.class, null,
					"resolving password policy for organization", task, result);

		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IllegalStateException(e);
		}

	}
	
	<F extends ObjectType> void processPasswordPolicy(LensProjectionContext projectionContext, 
			LensContext<F> context, Task task, OperationResult result) throws SchemaException, PolicyViolationException{
		
		ObjectDelta accountDelta = projectionContext.getDelta();
		
		if (accountDelta == null){
			LOGGER.trace("Skipping processing password policies. Shadow delta not specified.");
			return;
		}
		
		if (ChangeType.DELETE == accountDelta.getChangeType()){
			return;
		}
		
		PrismObject<ShadowType> accountShadow;
		PrismProperty<ProtectedStringType> password = null;
		if (ChangeType.ADD == accountDelta.getChangeType()){
			accountShadow = accountDelta.getObjectToAdd();
			if (accountShadow != null){
				password = accountShadow.findProperty(SchemaConstants.PATH_PASSWORD_VALUE);
			}
		}
		if (ChangeType.MODIFY == accountDelta.getChangeType() || password == null) {
			PropertyDelta<ProtectedStringType> passwordValueDelta =
					accountDelta.findPropertyDelta(SchemaConstants.PATH_PASSWORD_VALUE);
			// Modification sanity check
			if (accountDelta.getChangeType() == ChangeType.MODIFY && passwordValueDelta != null
					&& (passwordValueDelta.isAdd() || passwordValueDelta.isDelete())) {
				throw new SchemaException("Shadow password value cannot be added or deleted, it can only be replaced");
			}
			if (passwordValueDelta == null) {
				LOGGER.trace("Skipping processing password policies. Shadow delta does not contain password change.");
				return;
			}
			password = (PrismProperty<ProtectedStringType>) passwordValueDelta.getItemNewMatchingPath(null);
		}

		ValuePolicyType passwordPolicy;
		if (isCheckOrgPolicy(context)){
			passwordPolicy = determineValuePolicy(context.getFocusContext().getObjectAny(), task, result);
			context.getFocusContext().setOrgPasswordPolicy(passwordPolicy);
		} else {
			passwordPolicy = projectionContext.getEffectivePasswordPolicy();
		}
		
		processPasswordPolicy(passwordPolicy, null, password, result);
	}
	
	private <F extends ObjectType> boolean isCheckOrgPolicy(LensContext<F> context) throws SchemaException{
		LensFocusContext focusCtx = context.getFocusContext();
		if (focusCtx == null) {
			return false;			// TODO - ok?
		}

		if (focusCtx.getDelta() != null){
			if (focusCtx.getDelta().isAdd()){
				return false;
			}
			
			if (focusCtx.getDelta().isModify() && focusCtx.getDelta().hasItemDelta(SchemaConstants.PATH_PASSWORD_VALUE)){
				return false;
			}
		}
		
		if (focusCtx.getOrgPasswordPolicy() != null){
			return false;
		}
		
		return true;
	}


	/**
	 * Check provided password against provided policy
	 * 
	 * @param newPassword
	 *            - password to check
	 * @param pp
	 *            - Password policy used
	 * @return - Operation result of this validation
	 */
	public boolean validatePassword(String newPassword, PasswordType currentPasswordType, ValuePolicyType pp, OperationResult parentResult) {

		Validate.notNull(pp, "Password policy must not be null.");

		OperationResult result = parentResult.createSubresult(OPERATION_PASSWORD_VALIDATION);
		result.addParam("policyName", pp.getName());
		normalize(pp);

		if (newPassword == null && pp.getMinOccurs() != null
				&& XsdTypeMapper.multiplicityToInteger(pp.getMinOccurs()) == 0) {
			// No password is allowed
			result.recordSuccess();
			return true;
		}

		if (newPassword == null) {
			newPassword = "";
		}

		LimitationsType lims = pp.getStringPolicy().getLimitations();

		StringBuilder message = new StringBuilder();

		testMinimalLength(newPassword, lims, result, message);
		testMaximalLength(newPassword, lims, result, message);

		testMinimalUniqueCharacters(newPassword, lims, result, message);
		testPasswordHistoryEntries(newPassword, currentPasswordType, pp, result, message);
		

		if (lims.getLimit() == null || lims.getLimit().isEmpty()) {
			if (message.toString() == null || message.toString().isEmpty()) {
				result.computeStatus();
			} else {
				result.computeStatus(message.toString());

			}

			return result.isAcceptable();
		}

		// check limitation
		HashSet<String> validChars = null;
		HashSet<String> allValidChars = new HashSet<>();
		List<String> passwd = StringPolicyUtils.stringTokenizer(newPassword);
		for (StringLimitType stringLimitationType : lims.getLimit()) {
			OperationResult limitResult = new OperationResult(
					"Tested limitation: " + stringLimitationType.getDescription());

			validChars = getValidCharacters(stringLimitationType.getCharacterClass(), pp);
			int count = countValidCharacters(validChars, passwd);
			allValidChars.addAll(validChars);
			testMinimalOccurence(stringLimitationType, count, limitResult, message);
			testMaximalOccurence(stringLimitationType, count, limitResult, message);
			testMustBeFirst(stringLimitationType, count, limitResult, message, newPassword, validChars);

			limitResult.computeStatus();
			result.addSubresult(limitResult);
		}
		testInvalidCharacters(passwd, allValidChars, result, message);

		if (message.toString() == null || message.toString().isEmpty()) {
			result.computeStatus();
		} else {
			result.computeStatus(message.toString());

		}

		return result.isAcceptable();
	}
	
	/**
	 * add defined default values
	 */
	private void normalize(ValuePolicyType pp) {
		if (null == pp) {
			throw new IllegalArgumentException("Password policy cannot be null");
		}

		if (null == pp.getStringPolicy()) {
			StringPolicyType sp = new StringPolicyType();
			pp.setStringPolicy(StringPolicyUtils.normalize(sp));
		} else {
			pp.setStringPolicy(StringPolicyUtils.normalize(pp.getStringPolicy()));
		}

		if (null == pp.getLifetime()) {
			PasswordLifeTimeType lt = new PasswordLifeTimeType();
			lt.setExpiration(-1);
			lt.setWarnBeforeExpiration(0);
			lt.setLockAfterExpiration(0);
			lt.setMinPasswordAge(0);
			lt.setPasswordHistoryLength(0);
		}
		return;
	}

	private void testPasswordHistoryEntries(String newPassword, PasswordType currentPasswordType,
			ValuePolicyType pp, OperationResult result, StringBuilder message) {

		if (currentPasswordType == null) {
			return;
		}
		
		PasswordLifeTimeType lifetime = pp.getLifetime();
		if (lifetime == null) {
			return;
		}
		
		Integer passwordHistoryLength = lifetime.getPasswordHistoryLength();
		if (passwordHistoryLength == null || passwordHistoryLength == 0) {
			return;
		}
		
		if (passwordEquals(newPassword, currentPasswordType.getValue())) {
			appendHistoryViolationMessage(result, message);
			return;
		}
		
		List<PasswordHistoryEntryType> sortedHistoryList = getSortedHistoryList(
				currentPasswordType.asPrismContainerValue().findContainer(PasswordType.F_HISTORY_ENTRY), false);
		int i = 1;
		for (PasswordHistoryEntryType historyEntry: sortedHistoryList) {
			if (i >= passwordHistoryLength) {
				// success (history has more entries than needed)
				return;
			}
			if (passwordEquals(newPassword, historyEntry.getValue())) {
				LOGGER.trace("Password history entry #{} matched (changed {})", i, historyEntry.getChangeTimestamp());
				appendHistoryViolationMessage(result, message);
				return;
			}
			i++;
		}
		
		// success
	}

	private void appendHistoryViolationMessage(OperationResult result, StringBuilder message) {
		String msg = "Password couldn't be changed to the same value. Please select another password.";
		result.addSubresult(new OperationResult("Check if password does not contain invalid characters",
				OperationResultStatus.FATAL_ERROR, msg));
		message.append(msg);
		message.append("\n");
	}

	private void testInvalidCharacters(List<String> password, HashSet<String> validChars,
			OperationResult result, StringBuilder message) {

		// Check if there is no invalid character
		StringBuilder invalidCharacters = new StringBuilder();
		for (String s : password) {
			if (!validChars.contains(s)) {
				// memorize all invalid characters
				invalidCharacters.append(s);
			}
		}
		if (invalidCharacters.length() > 0) {
			String msg = "Characters [ " + invalidCharacters + " ] are not allowed in password";
			result.addSubresult(new OperationResult("Check if password does not contain invalid characters",
					OperationResultStatus.FATAL_ERROR, msg));
			message.append(msg);
			message.append("\n");
		}
		// else {
		// ret.addSubresult(new OperationResult("Check if password does not
		// contain invalid characters OK.",
		// OperationResultStatus.SUCCESS, "PASSED"));
		// }

	}

	private void testMustBeFirst(StringLimitType stringLimitationType, int count,
			OperationResult limitResult, StringBuilder message, String password, Set<String> validChars) {
		// test if first character is valid
		if (stringLimitationType.isMustBeFirst() == null) {
			stringLimitationType.setMustBeFirst(false);
		}
		// we check mustBeFirst only for non-empty passwords
		if (StringUtils.isNotEmpty(password) && stringLimitationType.isMustBeFirst()
				&& !validChars.contains(password.substring(0, 1))) {
			String msg = "First character is not from allowed set. Allowed set: " + validChars.toString();
			limitResult.addSubresult(
					new OperationResult("Check valid first char", OperationResultStatus.FATAL_ERROR, msg));
			message.append(msg);
			message.append("\n");
		}
		// else {
		// limitResult.addSubresult(new OperationResult("Check valid first char
		// in password OK.",
		// OperationResultStatus.SUCCESS, "PASSED"));
		// }

	}

	private void testMaximalOccurence(StringLimitType stringLimitationType, int count,
			OperationResult limitResult, StringBuilder message) {
		// Test maximal occurrence
		if (stringLimitationType.getMaxOccurs() != null) {

			if (stringLimitationType.getMaxOccurs() < count) {
				String msg = "Required maximal occurrence (" + stringLimitationType.getMaxOccurs()
						+ ") of characters (" + stringLimitationType.getDescription()
						+ ") in password was exceeded (occurrence of characters in password " + count + ").";
				limitResult.addSubresult(new OperationResult("Check maximal occurrence of characters",
						OperationResultStatus.FATAL_ERROR, msg));
				message.append(msg);
				message.append("\n");
			}
			// else {
			// limitResult.addSubresult(new OperationResult(
			// "Check maximal occurrence of characters in password OK.",
			// OperationResultStatus.SUCCESS,
			// "PASSED"));
			// }
		}

	}

	private void testMinimalOccurence(StringLimitType stringLimitation, int count,
			OperationResult result, StringBuilder message) {
		// Test minimal occurrence
		if (stringLimitation.getMinOccurs() == null) {
			stringLimitation.setMinOccurs(0);
		}
		if (stringLimitation.getMinOccurs() > count) {
			String msg = "Required minimal occurrence (" + stringLimitation.getMinOccurs()
					+ ") of characters (" + stringLimitation.getDescription()
					+ ") in password is not met (occurrence of characters in password " + count + ").";
			result.addSubresult(new OperationResult("Check minimal occurrence of characters",
					OperationResultStatus.FATAL_ERROR, msg));
			message.append(msg);
			message.append("\n");
		}
	}

	private int countValidCharacters(Set<String> validChars, List<String> password) {
		int count = 0;
		for (String s : password) {
			if (validChars.contains(s)) {
				count++;
			}
		}
		return count;
	}

	private HashSet<String> getValidCharacters(CharacterClassType characterClassType,
			ValuePolicyType passwordPolicy) {
		if (null != characterClassType.getValue()) {
			return new HashSet<String>(StringPolicyUtils.stringTokenizer(characterClassType.getValue()));
		} else {
			return new HashSet<String>(StringPolicyUtils.stringTokenizer(StringPolicyUtils
					.collectCharacterClass(passwordPolicy.getStringPolicy().getCharacterClass(),
							characterClassType.getRef())));
		}
	}

	private void testMinimalUniqueCharacters(String password, LimitationsType limitations,
			OperationResult result, StringBuilder message) {
		// Test uniqueness criteria
		HashSet<String> tmp = new HashSet<String>(StringPolicyUtils.stringTokenizer(password));
		if (limitations.getMinUniqueChars() != null) {
			if (limitations.getMinUniqueChars() > tmp.size()) {
				String msg = "Required minimal count of unique characters (" + limitations.getMinUniqueChars()
						+ ") in password are not met (unique characters in password " + tmp.size() + ")";
				result.addSubresult(new OperationResult("Check minimal count of unique chars",
						OperationResultStatus.FATAL_ERROR, msg));
				message.append(msg);
				message.append("\n");
			}

		}
	}

	private void testMinimalLength(String password, LimitationsType limitations,
			OperationResult result, StringBuilder message) {
		// Test minimal length
		if (limitations.getMinLength() == null) {
			limitations.setMinLength(0);
		}
		if (limitations.getMinLength() > password.length()) {
			String msg = "Required minimal size (" + limitations.getMinLength()
					+ ") of password is not met (password length: " + password.length() + ")";
			result.addSubresult(new OperationResult("Check global minimal length",
					OperationResultStatus.FATAL_ERROR, msg));
			message.append(msg);
			message.append("\n");
		}
	}

	private void testMaximalLength(String password, LimitationsType limitations,
			OperationResult result, StringBuilder message) {
		// Test maximal length
		if (limitations.getMaxLength() != null) {
			if (limitations.getMaxLength() < password.length()) {
				String msg = "Required maximal size (" + limitations.getMaxLength()
						+ ") of password was exceeded (password length: " + password.length() + ").";
				result.addSubresult(new OperationResult("Check global maximal length",
						OperationResultStatus.FATAL_ERROR, msg));
				message.append(msg);
				message.append("\n");
			}
		}
	}

	private boolean passwordEquals(String newPassword, ProtectedStringType currentPassword) {
		return determinePasswordValue(currentPassword).equals(newPassword);
	}


    // On missing password this returns empty string (""). It is then up to password policy whether it allows empty passwords or not.
	private String determinePasswordValue(PrismProperty<ProtectedStringType> password) {
		if (password == null || password.getValue(ProtectedStringType.class) == null) {
			return null;
		}

		ProtectedStringType passValue = password.getRealValue();

		return determinePasswordValue(passValue);
	}
	
	private String determinePasswordValue(ProtectedStringType passValue) {
		if (passValue == null) {
			return null;
		}

		String passwordStr = passValue.getClearValue();

		if (passwordStr == null && passValue.getEncryptedDataType () != null) {
			// TODO: is this appropriate handling???
			try {
				passwordStr = protector.decryptString(passValue);
			} catch (EncryptionException ex) {
				throw new SystemException("Failed to process password for user: " , ex);
			}
		}

		return passwordStr;
	}


	public <F extends FocusType> void processPasswordHistoryDeltas(LensFocusContext<F> focusContext,
			LensContext<F> context, XMLGregorianCalendar now, Task task, OperationResult result)
					throws SchemaException {
		PrismObject<F> focus = focusContext.getObjectOld();
		Validate.notNull(focus, "Focus object must not be null");
		if (focus.getCompileTimeClass().equals(UserType.class)) {
			PrismContainer<PasswordType> password = focus
					.findContainer(new ItemPath(UserType.F_CREDENTIALS, CredentialsType.F_PASSWORD));
			if (password == null || password.isEmpty()) {
				return;
			}
			PrismContainer<PasswordHistoryEntryType> historyEntries = password
					.findOrCreateContainer(PasswordType.F_HISTORY_ENTRY);

			int maxPasswordsToSave = getMaxPasswordsToSave(context.getFocusContext(), context, task, result);
			
			List<PasswordHistoryEntryType> historyEntryValues = getSortedHistoryList(historyEntries, true);
			int newHistoryEntries = 0;
			if (maxPasswordsToSave > 0) {
				newHistoryEntries = createAddHistoryDelta(context, password, now);
			}
			createDeleteHistoryDeltasIfNeeded(historyEntryValues, maxPasswordsToSave, newHistoryEntries, context, task, result);
		}
	}
	
	private <F extends FocusType> int getMaxPasswordsToSave(LensFocusContext<F> focusContext,
			LensContext<F> context, Task task, OperationResult result) throws SchemaException {
		ValuePolicyType passwordPolicy;
		if (focusContext.getOrgPasswordPolicy() == null) {
			passwordPolicy = determineValuePolicy(focusContext.getDelta(),
					focusContext.getObjectAny(), context, task, result);
			focusContext.setOrgPasswordPolicy(passwordPolicy);
		} else {
			passwordPolicy = focusContext.getOrgPasswordPolicy();
		}

		if (passwordPolicy == null) {
			return 0;
		}
		
		if (passwordPolicy.getLifetime() == null) {
			return 0;
		}

		Integer passwordHistoryLength = passwordPolicy.getLifetime().getPasswordHistoryLength();
		if (passwordHistoryLength == null) {
			return 0;
		}

		if (passwordHistoryLength <= 1) {
			return 0;
		}
		
		// One less than the history. The "first" history entry is the current password itself.
		return passwordHistoryLength - 1;
	}
	

	private <F extends FocusType> int createAddHistoryDelta(LensContext<F> context,
			PrismContainer<PasswordType> password, XMLGregorianCalendar now) throws SchemaException {
		PrismContainerValue<PasswordType> passwordValue = password.getValue();
		PasswordType passwordRealValue = passwordValue.asContainerable();
		
		PrismContainerDefinition<PasswordHistoryEntryType> historyEntryDefinition = password.getDefinition().findContainerDefinition(PasswordType.F_HISTORY_ENTRY);
		PrismContainer<PasswordHistoryEntryType> historyEntry = historyEntryDefinition.instantiate();
		
		PrismContainerValue<PasswordHistoryEntryType> hisotryEntryValue = historyEntry.createNewValue();
		
		PasswordHistoryEntryType entryType = hisotryEntryValue.asContainerable();
		entryType.setValue(passwordRealValue.getValue());
		entryType.setMetadata(passwordRealValue.getMetadata());
		entryType.setChangeTimestamp(now);
	
		ContainerDelta<PasswordHistoryEntryType> addHisotryDelta = ContainerDelta
				.createModificationAdd(new ItemPath(UserType.F_CREDENTIALS, CredentialsType.F_PASSWORD, PasswordType.F_HISTORY_ENTRY), UserType.class, prismContext, entryType.clone());
		context.getFocusContext().swallowToSecondaryDelta(addHisotryDelta);
		
		return 1;

	}

	private <F extends FocusType> void createDeleteHistoryDeltasIfNeeded(
			List<PasswordHistoryEntryType> historyEntryValues, int maxPasswordsToSave, int newHistoryEntries, LensContext<F> context, Task task,
			OperationResult result) throws SchemaException {
		
		if (historyEntryValues.size() == 0) {
			return;
		}

		// We need to delete one more entry than intuitively expected - because we are computing from the history entries 
		// in the old object. In the new object there will be one new history entry for the changed password.
		int numberOfHistoryEntriesToDelete = historyEntryValues.size() - maxPasswordsToSave + newHistoryEntries;
		
		for (int i = 0; i < numberOfHistoryEntriesToDelete; i++) {
			LOGGER.info("PPPPPPPPPPP i={}, numberOfHistoryEntriesToDelete={}, maxPasswordsToSave={}", i, numberOfHistoryEntriesToDelete, maxPasswordsToSave);
			ContainerDelta<PasswordHistoryEntryType> deleteHistoryDelta = ContainerDelta
					.createModificationDelete(
							new ItemPath(UserType.F_CREDENTIALS, CredentialsType.F_PASSWORD,
									PasswordType.F_HISTORY_ENTRY),
							UserType.class, prismContext,
							historyEntryValues.get(i).clone());
			context.getFocusContext().swallowToSecondaryDelta(deleteHistoryDelta);
		}

	}

	private List<PasswordHistoryEntryType> getSortedHistoryList(PrismContainer<PasswordHistoryEntryType> historyEntries, boolean ascending) {
		if (historyEntries == null || historyEntries.isEmpty()) {
			return new ArrayList<>();
		}
		List<PasswordHistoryEntryType> historyEntryValues = (List<PasswordHistoryEntryType>) historyEntries.getRealValues();

		Collections.sort(historyEntryValues, (o1, o2) -> {
				XMLGregorianCalendar changeTimestampFirst = o1.getChangeTimestamp();
				XMLGregorianCalendar changeTimestampSecond = o2.getChangeTimestamp();

				if (ascending) {
					return changeTimestampFirst.compare(changeTimestampSecond);
				} else {
					return changeTimestampSecond.compare(changeTimestampFirst);
				}
			});
		return historyEntryValues;
	}


}
