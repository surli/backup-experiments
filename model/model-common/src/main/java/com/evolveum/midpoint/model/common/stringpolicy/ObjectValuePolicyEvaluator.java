/**
 * Copyright (c) 2017 Evolveum
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
package com.evolveum.midpoint.model.common.stringpolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.crypto.EncryptionException;
import com.evolveum.midpoint.prism.crypto.Protector;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathSegment;
import com.evolveum.midpoint.prism.path.NameItemPathSegment;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.prism.xml.XsdTypeMapper;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.security.api.SecurityUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.PolicyViolationException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractCredentialType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MetadataType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NonceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordCredentialsPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordHistoryEntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SecurityPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValuePolicyType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;

/**
 * Evaluator that validates the value of any object property. The validation means a checks whether
 * the value is a valid for that property. It usually applies to credentials such as passwords.
 * But it can be used also for other properties.
 * 
 * This class may also generate value fitting for that property.
 * 
 * TODO: generalize to all object types, not just user
 * In that case we may need to move this class to the model-impl
 * User template will be probably needed for this
 * 
 * @author semancik
 *
 */
public class ObjectValuePolicyEvaluator {
	
	private static final Trace LOGGER = TraceManager.getTrace(ObjectValuePolicyEvaluator.class);

	public static final String OPERATION_VALIDATE_VALUE = ObjectValuePolicyEvaluator.class + ".validateValue";

	private Protector protector;
	
	private ValuePolicyProcessor valuePolicyProcessor;
	
	private SecurityPolicyType securityPolicy;
	
	private XMLGregorianCalendar now;
	
	private ItemPath valueItemPath;
	
	private PrismObject<UserType> object;
	
	// We need to get old credential as a configuration. We cannot determine it
	// from the "object". E.g. in case of addition the object is the new object that
	// is just being added. The password will conflict with itself.
	private AbstractCredentialType oldCredentialType;
	
	private String shortDesc;
	
	private Task task;
	
	// state
	
	private boolean prepared = false;
	private QName credentialQName = null;
	private CredentialPolicyType credentialPolicy;
	private ValuePolicyType valuePolicy;
	
	
	public Protector getProtector() {
		return protector;
	}

	public void setProtector(Protector protector) {
		this.protector = protector;
	}

	public ValuePolicyProcessor getValuePolicyProcessor() {
		return valuePolicyProcessor;
	}

	public void setValuePolicyProcessor(ValuePolicyProcessor valuePolicyProcessor) {
		this.valuePolicyProcessor = valuePolicyProcessor;
	}

	public SecurityPolicyType getSecurityPolicy() {
		return securityPolicy;
	}

	public void setSecurityPolicy(SecurityPolicyType securityPolicy) {
		this.securityPolicy = securityPolicy;
	}

	public XMLGregorianCalendar getNow() {
		return now;
	}

	public void setNow(XMLGregorianCalendar now) {
		this.now = now;
	}

	public ItemPath getValueItemPath() {
		return valueItemPath;
	}

	public void setValueItemPath(ItemPath valueItemPath) {
		this.valueItemPath = valueItemPath;
	}

	public PrismObject<UserType> getObject() {
		return object;
	}

	public void setObject(PrismObject<UserType> object) {
		this.object = object;
	}

	public AbstractCredentialType getOldCredentialType() {
		return oldCredentialType;
	}

	public void setOldCredentialType(AbstractCredentialType oldCredentialType) {
		this.oldCredentialType = oldCredentialType;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public OperationResult validateProtectedStringValue(ProtectedStringType value) throws SchemaException, ObjectNotFoundException, ExpressionEvaluationException {
		String clearValue = getClearValue(value);
		return validateStringValue(clearValue);
	}
	
	public OperationResult validateStringValue(String clearValue) throws SchemaException, ObjectNotFoundException, ExpressionEvaluationException {
		OperationResult result = new OperationResult(OPERATION_VALIDATE_VALUE);
		// TODO: later we need to replace the string message with something more structured.
		// something that can be localized
		StringBuilder messageBuilder = new StringBuilder();
		
		prepare();
		
		validateMinAge(messageBuilder, result);
		validateHistory(clearValue, messageBuilder, result);
		validateStringPolicy(clearValue, messageBuilder, result);
		
		String message = messageBuilder.toString();
		if (message.isEmpty()) {
			result.computeStatus();
		} else {
			result.computeStatus(message);
		}
		return result;
	}
	
	private void prepare() throws SchemaException {
		if (!prepared) {
			preparePassword();
			prepareNonce();
			prepareValuePolicy();
			prepared = true;
		}
	}
	
	private void prepareValuePolicy() {
		if (credentialPolicy != null) {
			ObjectReferenceType valuePolicyRef = credentialPolicy.getValuePolicyRef();
			if (valuePolicyRef != null) {
				PrismObject<ValuePolicyType> valuePolicyObj = valuePolicyRef.asReferenceValue().getObject();
				if (valuePolicyObj != null) {
					valuePolicy = valuePolicyObj.asObjectable();
				}
			}
		}
		// TODO: check value policy from the schema (definition)
	}

	private void preparePassword() {
		if (!QNameUtil.match(UserType.F_CREDENTIALS, valueItemPath.getFirstName())) {
			return;
		}
		ItemPathSegment secondPathSegment = valueItemPath.getSegments().get(1);
		if (!(secondPathSegment instanceof NameItemPathSegment)) {
			return;
		}
		credentialQName = ((NameItemPathSegment)secondPathSegment).getName();
		if (!QNameUtil.match(CredentialsType.F_PASSWORD, credentialQName)) {
			return;
		}
		
		credentialPolicy = SecurityUtil.getEffectivePasswordCredentialsPolicy(securityPolicy);
	}
	
	private void prepareNonce() throws SchemaException {
		if (!QNameUtil.match(CredentialsType.F_NONCE, credentialQName)) {
			return;
		}
		
		credentialPolicy = SecurityUtil.getEffectiveNonceCredentialsPolicy(securityPolicy);
	}

	private void validateMinAge(StringBuilder messageBuilder, OperationResult result) {
		if (oldCredentialType == null) {
			return;
		}
		Duration minAge = getMinAge();
		if (minAge == null) {
			return;
		}
		MetadataType currentCredentialMetadata = oldCredentialType.getMetadata();
		if (currentCredentialMetadata == null) {
			return;
		}
		XMLGregorianCalendar lastChangeTimestamp = currentCredentialMetadata.getModifyTimestamp();
		if (lastChangeTimestamp == null) {
			lastChangeTimestamp = currentCredentialMetadata.getCreateTimestamp();
		}
		if (lastChangeTimestamp == null) {
			return;
		}
		
		XMLGregorianCalendar changeAllowedTimestamp = XmlTypeConverter.addDuration(lastChangeTimestamp, minAge);
		if (changeAllowedTimestamp.compare(now) == DatatypeConstants.GREATER) {
			LOGGER.trace("Password minAge violated. lastChange={}, minAge={}, now={}", lastChangeTimestamp, minAge, now);
			String msg = shortDesc + " could not be changed because password minimal age was not yet reached.";
			result.addSubresult(new OperationResult("Password minimal age",
					OperationResultStatus.FATAL_ERROR, msg));
			messageBuilder.append(msg);
			messageBuilder.append("\n");
		}
	}

	private void validateStringPolicy(String clearValue, StringBuilder messageBuilder, OperationResult result) throws SchemaException, ObjectNotFoundException, ExpressionEvaluationException {
		
		if (clearValue == null) {
			int minOccurs = getMinOccurs();
			if (minOccurs == 0) {
				return;
			} else {
				String msg = shortDesc + " must have a value.";
				result.addSubresult(new OperationResult("minOccurs", OperationResultStatus.FATAL_ERROR, msg));
				messageBuilder.append(msg);
				messageBuilder.append("\n");
				return;
			}
		}
		
		if (valuePolicy == null) {
			LOGGER.trace("Skipping validating {} value. Value policy not specified.", shortDesc);
			return;
		}
		
		valuePolicyProcessor.validateValue(clearValue, valuePolicy, object, messageBuilder, 
				"user " + shortDesc + " value policy validation", task, result);
	}
	
	private void validateHistory(String clearValue, StringBuilder messageBuilder, OperationResult result) throws SchemaException {
		
		if (!QNameUtil.match(CredentialsType.F_PASSWORD, credentialQName)) {
			LOGGER.trace("Skipping validating {} history, only passowrd history is supported", shortDesc);
			return;
		}
		
		int historyLegth = getHistoryLength();
		if (historyLegth == 0) {
			LOGGER.trace("Skipping validating {} history, because history length is set to zero", shortDesc);
			return;
		}
		
		PasswordType currentPasswordType = (PasswordType)oldCredentialType;
		if (currentPasswordType == null) {
			LOGGER.trace("Skipping validating {} history, because it is empty", shortDesc);
			return;
		}
		
		ProtectedStringType newPasswordPs = new ProtectedStringType();
		newPasswordPs.setClearValue(clearValue);
		
		if (passwordEquals(newPasswordPs, currentPasswordType.getValue())) {
			LOGGER.trace("{} matched current value", shortDesc);
			appendHistoryViolationMessage(messageBuilder, result);
			return;
		}
		
		List<PasswordHistoryEntryType> sortedHistoryList = getSortedHistoryList(
				currentPasswordType.asPrismContainerValue().findContainer(PasswordType.F_HISTORY_ENTRY), false);
		int i = 1;
		for (PasswordHistoryEntryType historyEntry: sortedHistoryList) {
			if (i >= historyLegth) {
				// success (history has more entries than needed)
				return;
			}
			if (passwordEquals(newPasswordPs, historyEntry.getValue())) {
				LOGGER.trace("Password history entry #{} matched (changed {})", i, historyEntry.getChangeTimestamp());
				appendHistoryViolationMessage(messageBuilder, result);
				return;
			}
			i++;
		}		
	}
	
	private int getHistoryLength() {
		return SecurityUtil.getCredentialHistoryLength(credentialPolicy);
	}
	
	private Duration getMinAge() {
		if (credentialPolicy == null) {
			return null;
		}
		return credentialPolicy.getMinAge();
	}
	
	private int getMinOccurs() {
		if (credentialPolicy == null) {
			return 0;
		}
		String minOccurs = credentialPolicy.getMinOccurs();
		if (minOccurs == null) {
			return 0;
		}
		return XsdTypeMapper.multiplicityToInteger(minOccurs);
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
	
	private void appendHistoryViolationMessage(StringBuilder messageBuilder, OperationResult result) {
		String msg = "Password couldn't be changed because it was recently used.";
		result.addSubresult(new OperationResult("history", OperationResultStatus.FATAL_ERROR, msg));
		messageBuilder.append(msg);
		messageBuilder.append("\n");
	}
	
	private String getClearValue(ProtectedStringType protectedString) {
		if (protectedString == null) {
			return null;
		}

		String passwordStr = protectedString.getClearValue();

		if (passwordStr == null && protectedString.isEncrypted()) {
			try {
				passwordStr = protector.decryptString(protectedString);
			} catch (EncryptionException e) {
				throw new SystemException("Failed to deprypt " + shortDesc + ": " , e);
			}
		}

		return passwordStr;
	}
	
	private boolean passwordEquals(ProtectedStringType newPasswordPs, ProtectedStringType currentPassword) throws SchemaException {
		if (currentPassword == null) {
			return newPasswordPs == null;
		}
		try {
			return protector.compare(newPasswordPs, currentPassword);
		} catch (EncryptionException e) {
			throw new SystemException("Failed to compare " + shortDesc + ": " , e);
		}
	}
}
