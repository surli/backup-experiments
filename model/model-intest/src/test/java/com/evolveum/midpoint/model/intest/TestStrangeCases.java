/*
 * Copyright (c) 2010-2017 Evolveum
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
package com.evolveum.midpoint.model.intest;

import static org.testng.AssertJUnit.assertTrue;
import static com.evolveum.midpoint.test.util.TestUtil.assertSuccess;
import static com.evolveum.midpoint.test.IntegrationTestTools.display;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.query.builder.QueryBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.evolveum.icf.dummy.resource.BreakMode;
import com.evolveum.icf.dummy.resource.DummyAccount;
import com.evolveum.midpoint.prism.delta.ChangeType;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.ReferenceDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.ObjectDeltaOperation;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.schema.util.MiscSchemaUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.test.DummyResourceContoller;
import com.evolveum.midpoint.test.util.TestUtil;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DisplayableValue;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.PolicyViolationException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentPolicyEnforcementType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author semancik
 *
 */
@ContextConfiguration(locations = {"classpath:ctx-model-intest-test-main.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestStrangeCases extends AbstractInitializedModelIntegrationTest {
	
	private static final File TEST_DIR = new File("src/test/resources/strange");
	
	private static final File USER_DEGHOULASH_FILE = new File(TEST_DIR, "user-deghoulash.xml");
	private static final String USER_DEGHOULASH_OID = "c0c010c0-d34d-b33f-f00d-1d11dd11dd11";
	private static final String USER_DEGHOULASH_NAME = "deghoulash";

	private static final String RESOURCE_DUMMY_CIRCUS_NAME = "circus";
	private static final File RESOURCE_DUMMY_CIRCUS_FILE = new File(TEST_DIR, "resource-dummy-circus.xml");
	private static final String RESOURCE_DUMMY_CIRCUS_OID = "65d73d14-bafb-11e6-9de3-ff46daf6e769";
	
	private static final File ROLE_IDIOT_FILE = new File(TEST_DIR, "role-idiot.xml");
	private static final String ROLE_IDIOT_OID = "12345678-d34d-b33f-f00d-555555550001";

	private static final File ROLE_STUPID_FILE = new File(TEST_DIR, "role-stupid.xml");
	private static final String ROLE_STUPID_OID = "12345678-d34d-b33f-f00d-555555550002";
	
	private static final File ROLE_BAD_CONSTRUCTION_RESOURCE_REF_FILE = new File(TEST_DIR, "role-bad-construction-resource-ref.xml");
	private static final String ROLE_BAD_CONSTRUCTION_RESOURCE_REF_OID = "54084f2c-eba0-11e5-8278-03ea5d7058d9";
	
	private static final File ROLE_META_BAD_CONSTRUCTION_RESOURCE_REF_FILE = new File(TEST_DIR, "role-meta-bad-construction-resource-ref.xml");
	private static final String ROLE_META_BAD_CONSTRUCTION_RESOURCE_REF_OID = "90b931ae-eba8-11e5-977a-b73ba58cf18b";

	private static final File ROLE_TARGET_BAD_CONSTRUCTION_RESOURCE_REF_FILE = new File(TEST_DIR, "role-target-bad-construction-resource-ref.xml");
	private static final String ROLE_TARGET_BAD_CONSTRUCTION_RESOURCE_REF_OID = "e69b791a-eba8-11e5-80f5-33732b18f10a";

	private static final File ROLE_RECURSION_FILE = new File(TEST_DIR, "role-recursion.xml");
	private static final String ROLE_RECURSION_OID = "12345678-d34d-b33f-f00d-555555550003";

	private static final String NON_EXISTENT_ACCOUNT_OID = "f000f000-f000-f000-f000-f000f000f000";
	
	private static final String RESOURCE_NONEXISTENT_OID = "00000000-f000-f000-f000-f000f000f000";;

	private static final XMLGregorianCalendar USER_DEGHOULASH_FUNERAL_TIMESTAMP = 
								XmlTypeConverter.createXMLGregorianCalendar(1771, 1, 2, 11, 22, 33);
	
	private static final File TREASURE_ISLAND_FILE = new File(TEST_DIR, "treasure-island.txt");

	private static String treasureIsland;
	
	private String accountGuybrushOid;
	private String accountJackOid;

	private String accountJackRedOid;
	
	public TestStrangeCases() throws JAXBException {
		super();
	}
	
	@Override
	public void initSystem(Task initTask, OperationResult initResult)
			throws Exception {
		super.initSystem(initTask, initResult);
		
		initDummyResource(RESOURCE_DUMMY_CIRCUS_NAME, RESOURCE_DUMMY_CIRCUS_FILE, RESOURCE_DUMMY_CIRCUS_OID, initTask, initResult);
		
		dummyResourceCtlRed.addAccount(ACCOUNT_GUYBRUSH_DUMMY_USERNAME, "Guybrush Threepwood", "Monkey Island");
		
		repoAddObjectFromFile(ACCOUNT_GUYBRUSH_DUMMY_RED_FILE, initResult);
		
		treasureIsland = IOUtils.toString(new FileInputStream(TREASURE_ISLAND_FILE)).replace("\r\n", "\n");     // for Windows compatibility
		
		addObject(ROLE_IDIOT_FILE, initTask, initResult);
		addObject(ROLE_STUPID_FILE, initTask, initResult);
		addObject(ROLE_RECURSION_FILE, initTask, initResult);
		addObject(ROLE_BAD_CONSTRUCTION_RESOURCE_REF_FILE, initTask, initResult);
		addObject(ROLE_META_BAD_CONSTRUCTION_RESOURCE_REF_FILE, initTask, initResult);
		
//		DebugUtil.setDetailedDebugDump(true);
	}

	@Test
    public void test100ModifyUserGuybrushAddAccountDummyRedNoAttributesConflict() throws Exception {
		final String TEST_NAME = "test100ModifyUserGuybrushAddAccountDummyRedNoAttributesConflict";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.NONE);
        
        PrismObject<ShadowType> account = PrismTestUtil.parseObject(ACCOUNT_GUYBRUSH_DUMMY_RED_FILE);
        // Remove the attributes. This will allow outbound mapping to take place instead.
        account.removeContainer(ShadowType.F_ATTRIBUTES);
        
        ObjectDelta<UserType> userDelta = ObjectDelta.createEmptyModifyDelta(UserType.class, USER_GUYBRUSH_OID, prismContext);
        PrismReferenceValue accountRefVal = new PrismReferenceValue();
		accountRefVal.setObject(account);
		ReferenceDelta accountDelta = ReferenceDelta.createModificationAdd(UserType.F_LINK_REF, getUserDefinition(), accountRefVal);
		userDelta.addModification(accountDelta);
		Collection<ObjectDelta<? extends ObjectType>> deltas = (Collection)MiscUtil.createCollection(userDelta);
		
		dummyAuditService.clear();
        
		try {
			
			// WHEN
			modelService.executeChanges(deltas, null, task, getCheckingProgressListenerCollection(), result);
			
			AssertJUnit.fail("Unexpected executeChanges success");
		} catch (ObjectAlreadyExistsException e) {
			// This is expected
			display("Expected exception", e);
		}
				
		// Check accountRef
		PrismObject<UserType> userGuybrush = modelService.getObject(UserType.class, USER_GUYBRUSH_OID, null, task, result);
        UserType userGuybrushType = userGuybrush.asObjectable();
        assertEquals("Unexpected number of accountRefs", 1, userGuybrushType.getLinkRef().size());
        ObjectReferenceType accountRefType = userGuybrushType.getLinkRef().get(0);
        String accountOid = accountRefType.getOid();
        assertFalse("No accountRef oid", StringUtils.isBlank(accountOid));
        PrismReferenceValue accountRefValue = accountRefType.asReferenceValue();
        assertEquals("OID mismatch in accountRefValue", accountOid, accountRefValue.getOid());
        assertNull("Unexpected object in accountRefValue", accountRefValue.getObject());
        
		// Check shadow
        PrismObject<ShadowType> accountShadow = repositoryService.getObject(ShadowType.class, accountOid, null, result);
        assertDummyAccountShadowRepo(accountShadow, accountOid, ACCOUNT_GUYBRUSH_DUMMY_USERNAME);
        
        // Check account
        PrismObject<ShadowType> accountModel = modelService.getObject(ShadowType.class, accountOid, null, task, result);
        assertDummyAccountShadowModel(accountModel, accountOid, ACCOUNT_GUYBRUSH_DUMMY_USERNAME, "Guybrush Threepwood");
        
        // Check account in dummy resource
        assertDefaultDummyAccount(ACCOUNT_GUYBRUSH_DUMMY_USERNAME, "Guybrush Threepwood", true);
        
        result.computeStatus();
        display("executeChanges result", result);
        TestUtil.assertFailure("executeChanges result", result);
        
        // Check audit
        display("Audit", dummyAuditService);
        dummyAuditService.assertRecords(2);
        dummyAuditService.assertSimpleRecordSanity();
        dummyAuditService.assertAnyRequestDeltas();
        Collection<ObjectDeltaOperation<? extends ObjectType>> auditExecution0Deltas = dummyAuditService.getExecutionDeltas(0);
        assertEquals("Wrong number of execution deltas", 0, auditExecution0Deltas.size());
        dummyAuditService.assertExecutionOutcome(OperationResultStatus.FATAL_ERROR);
        
	}

	@Test
    public void test180DeleteHalfAssignmentFromUser() throws Exception {
		String TEST_NAME = "test180DeleteHalfAssignmentFromUser";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
        
        PrismObject<UserType> userOtis = createUser("otis", "Otis");
        fillinUserAssignmentAccountConstruction(userOtis, RESOURCE_DUMMY_OID);
		
		display("Half-assigned user", userOtis);
		
		// Remember the assignment so we know what to remove
		AssignmentType assignmentType = userOtis.asObjectable().getAssignment().iterator().next();
		
		// Add to repo to avoid processing of the assignment
		String userOtisOid = repositoryService.addObject(userOtis, null, result);
        
        ObjectDelta<UserType> userDelta = ObjectDelta.createModificationDeleteContainer(UserType.class, 
        		userOtisOid, UserType.F_ASSIGNMENT, prismContext, assignmentType.asPrismContainerValue().clone());
        Collection<ObjectDelta<? extends ObjectType>> deltas = MiscSchemaUtil.createCollection(userDelta);
                
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
		modelService.executeChanges(deltas, null, task, getCheckingProgressListenerCollection(), result);
		
		// THEN
		result.computeStatus();
        TestUtil.assertSuccess("executeChanges result", result);
        
		PrismObject<UserType> userOtisAfter = getUser(userOtisOid);
		assertNotNull("Otis is gone!", userOtisAfter);
		// Check accountRef
        assertUserNoAccountRefs(userOtisAfter);
        
        // Check if dummy resource account is gone
        assertNoDummyAccount("otis");
        
        // Check audit
        display("Audit", dummyAuditService);
        dummyAuditService.assertRecords(2);
        dummyAuditService.assertSimpleRecordSanity();
        dummyAuditService.assertAnyRequestDeltas();
        dummyAuditService.assertExecutionDeltas(1);
        dummyAuditService.assertHasDelta(ChangeType.MODIFY, UserType.class);
        dummyAuditService.assertExecutionSuccess();
	}
	
	@Test
    public void test190DeleteHalfAssignedUser() throws Exception {
		String TEST_NAME = "test190DeleteHalfAssignedUser";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
        
        PrismObject<UserType> userNavigator = createUser("navigator", "Head of the Navigator");
        fillinUserAssignmentAccountConstruction(userNavigator, RESOURCE_DUMMY_OID);
		
		display("Half-assigned user", userNavigator);
		
		// Add to repo to avoid processing of the assignment
		String userNavigatorOid = repositoryService.addObject(userNavigator, null, result);
        
        ObjectDelta<UserType> userDelta = ObjectDelta.createDeleteDelta(UserType.class, userNavigatorOid, prismContext);
        Collection<ObjectDelta<? extends ObjectType>> deltas = MiscSchemaUtil.createCollection(userDelta);
                
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
		modelService.executeChanges(deltas, null, task, getCheckingProgressListenerCollection(), result);
		
		// THEN
		TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
        TestUtil.assertSuccess("executeChanges result", result);
        
		try {
			getUser(userNavigatorOid);
			AssertJUnit.fail("navigator is still alive!");
		} catch (ObjectNotFoundException ex) {
			// This is OK
		}
        
        // Check if dummy resource account is gone
        assertNoDummyAccount("navigator");
        
        // Check audit
        display("Audit", dummyAuditService);
        dummyAuditService.assertRecords(2);
        dummyAuditService.assertSimpleRecordSanity();
        dummyAuditService.assertAnyRequestDeltas();
        dummyAuditService.assertExecutionDeltas(1);
        dummyAuditService.assertHasDelta(ChangeType.DELETE, UserType.class);
        dummyAuditService.assertExecutionSuccess();
	}
	
	/**
	 * User with linkRef that points nowhere.
	 * MID-2134
	 */
	@Test
    public void test200ModifyUserJackBrokenAccountRefAndPolyString() throws Exception {
		final String TEST_NAME = "test200ModifyUserJackBrokenAccountRefAndPolyString";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
        
        addBrokenAccountRef(USER_JACK_OID);
        // Make sure that the polystring does not have correct norm value
        PolyString fullNamePolyString = new PolyString("Magnificent Captain Jack Sparrow", null);
                        
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
        modifyUserReplace(USER_JACK_OID, UserType.F_FULL_NAME, task, result, 
        		fullNamePolyString);
		
		// THEN
        TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
        TestUtil.assertSuccess("executeChanges result", result, 2);
        
		PrismObject<UserType> userJack = getUser(USER_JACK_OID);
		display("User after change execution", userJack);
		assertUserJack(userJack, "Magnificent Captain Jack Sparrow");
        assertAccounts(USER_JACK_OID, 0);
                
        // Check audit
        display("Audit", dummyAuditService);
        dummyAuditService.assertRecords(2);
        dummyAuditService.assertSimpleRecordSanity();
        dummyAuditService.assertAnyRequestDeltas();
        dummyAuditService.assertExecutionDeltas(2);
        dummyAuditService.assertHasDelta(ChangeType.MODIFY, UserType.class);
        dummyAuditService.assertExecutionOutcome(OperationResultStatus.HANDLED_ERROR);
	}
	
	/**
	 * Not much to see here. Just making sure that add account goes smoothly
	 * after previous test. Also preparing setup for following tests.
	 * The account is simply added, not assigned. This makes it quite fragile.
	 * This is the way how we like it (in the tests).
	 */
	@Test
    public void test210ModifyUserAddAccount() throws Exception {
    	final String TEST_NAME = "test210ModifyUserAddAccount";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.RELATIVE);
        
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
        modifyUserAddAccount(USER_JACK_OID, ACCOUNT_JACK_DUMMY_FILE, task, result);
		
		// THEN
        TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
        TestUtil.assertSuccess(result);
        
		// Check accountRef
		PrismObject<UserType> userJack = modelService.getObject(UserType.class, USER_JACK_OID, null, task, result);
        accountJackOid = getSingleLinkOid(userJack);
        
        // Check account in dummy resource
        assertDefaultDummyAccount("jack", "Jack Sparrow", true);
	}
	
	/**
	 * Not much to see here. Just preparing setup for following tests.
	 * The account is simply added, not assigned. This makes it quite fragile.
	 * This is the way how we like it (in the tests).
	 */
	@Test
    public void test212ModifyUserAddAccountRed() throws Exception {
    	final String TEST_NAME = "test212ModifyUserAddAccountRed";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.RELATIVE);
        
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
        modifyUserAddAccount(USER_JACK_OID, ACCOUNT_JACK_DUMMY_RED_FILE, task, result);
		
		// THEN
        TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
        TestUtil.assertSuccess(result);
        
		// Check accountRef
		PrismObject<UserType> userJack = modelService.getObject(UserType.class, USER_JACK_OID, null, task, result);
		assertLinks(userJack, 2);
		accountJackRedOid = getLinkRefOid(userJack, RESOURCE_DUMMY_RED_OID);
		assertNotNull(accountJackRedOid);
        
        assertDefaultDummyAccount("jack", "Jack Sparrow", true);
        assertDummyAccount(RESOURCE_DUMMY_RED_NAME, "jack", "Magnificent Captain Jack Sparrow", true);
	}
	
	/**
	 * Cause schema violation on the account during a provisioning operation. This should fail
	 * the operation, but other operations should proceed and the account should definitelly NOT
	 * be unlinked.
	 * MID-2134
	 */
	@Test
    public void test212ModifyUserJackBrokenSchemaViolationPolyString() throws Exception {
		final String TEST_NAME = "test212ModifyUserJackBrokenSchemaViolationPolyString";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        dummyAuditService.clear();
        
        getDummyResource().setModifyBreakMode(BreakMode.SCHEMA);
                        
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
        modifyUserReplace(USER_JACK_OID, UserType.F_FULL_NAME, task, result, 
        		new PolyString("Cpt. Jack Sparrow", null));
		
		// THEN
        TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
		display("Result", result);
		TestUtil.assertPartialError(result);
        
		PrismObject<UserType> userJack = getUser(USER_JACK_OID);
		display("User after change execution", userJack);
		assertUserJack(userJack, "Cpt. Jack Sparrow");
        
		assertLinks(userJack, 2);
		String accountJackRedOidAfter = getLinkRefOid(userJack, RESOURCE_DUMMY_RED_OID);
		assertNotNull(accountJackRedOidAfter);
        
		// The change was not propagated here because of schema violation error
		assertDefaultDummyAccount("jack", "Jack Sparrow", true);
		
		// The change should be propagated here normally
        assertDummyAccount(RESOURCE_DUMMY_RED_NAME, "jack", "Cpt. Jack Sparrow", true);
	}

	/**
	 * Cause schema violation on the account during a provisioning operation. This should fail
	 * the operation, but other operations should proceed and the account should definitelly NOT
	 * be unlinked.
	 * MID-2134
	 */
	@Test
    public void test214ModifyUserJackBrokenPassword() throws Exception {
		final String TEST_NAME = "test214ModifyUserJackBrokenPassword";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        dummyAuditService.clear();
        
        getDummyResource().setModifyBreakMode(BreakMode.SCHEMA);
                        
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
        modifyUserChangePassword(USER_JACK_OID, "whereStheRUM", task, result);
		
		// THEN
        TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
		display("Result", result);
		TestUtil.assertPartialError(result);
        
		PrismObject<UserType> userJack = getUser(USER_JACK_OID);
		display("User after change execution", userJack);
		assertUserJack(userJack, "Cpt. Jack Sparrow");
		assertEncryptedUserPassword(userJack, "whereStheRUM");
        
		assertLinks(userJack, 2);
		String accountJackRedOidAfter = getLinkRefOid(userJack, RESOURCE_DUMMY_RED_OID);
		assertNotNull(accountJackRedOidAfter);
        
		// The change was not propagated here because of schema violation error
		assertDefaultDummyAccount("jack", "Jack Sparrow", true);
		
		// The change should be propagated here normally
        assertDummyAccount(RESOURCE_DUMMY_RED_NAME, "jack", "Cpt. Jack Sparrow", true);
	}
	
	/**
	 * Cause modification that will be mapped to the account and that will cause
	 * conflict (AlreadyExistsException). Make sure that midpoint does not end up
	 * in endless loop.
	 * MID-3451
	 */
	@Test
    public void test220ModifyUserJackBrokenConflict() throws Exception {
		final String TEST_NAME = "test220ModifyUserJackBrokenConflict";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        dummyAuditService.clear();
        
        getDummyResource().setModifyBreakMode(BreakMode.CONFLICT);
                        
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
        modifyUserReplace(USER_JACK_OID, UserType.F_LOCALITY, task, result, 
        		PrismTestUtil.createPolyString("High seas"));
		
		// THEN
        TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
		display("Result", result);
		TestUtil.assertPartialError(result);
        
		PrismObject<UserType> userJack = getUser(USER_JACK_OID);
		display("User after change execution", userJack);
		PrismAsserts.assertPropertyValue(userJack, UserType.F_LOCALITY, 
				PrismTestUtil.createPolyString("High seas"));
        
		assertLinks(userJack, 2);
		String accountJackRedOidAfter = getLinkRefOid(userJack, RESOURCE_DUMMY_RED_OID);
		assertNotNull(accountJackRedOidAfter);
        
		// The change was not propagated here because of schema violation error
		assertDefaultDummyAccount("jack", "Jack Sparrow", true);
		assertDefaultDummyAccountAttribute("jack", 
				DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, "Caribbean");
		
		// The change should be propagated here normally
        assertDummyAccount(RESOURCE_DUMMY_RED_NAME, "jack", "Cpt. Jack Sparrow", true);
        assertDummyAccountAttribute(RESOURCE_DUMMY_RED_NAME, "jack", 
        		DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, "High seas");
	}

	// Lets test various extension magic and border cases now. This is maybe quite hight in the architecture for
	// this test, but we want to make sure that none of the underlying components will screw the things up.
	
	@Test
    public void test300ExtensionSanity() throws Exception {
		final String TEST_NAME = "test300ExtensionSanity";
        TestUtil.displayTestTile(this, TEST_NAME);
        
        PrismObjectDefinition<UserType> userDef = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismContainerDefinition<Containerable> extensionContainerDef = userDef.findContainerDefinition(UserType.F_EXTENSION);
        PrismAsserts.assertPropertyDefinition(extensionContainerDef, PIRACY_SHIP, DOMUtil.XSD_STRING, 1, 1);
        PrismAsserts.assertIndexed(extensionContainerDef, PIRACY_SHIP, true);
        PrismAsserts.assertPropertyDefinition(extensionContainerDef, PIRACY_TALES, DOMUtil.XSD_STRING, 0, 1);
        PrismAsserts.assertIndexed(extensionContainerDef, PIRACY_TALES, false);
        PrismAsserts.assertPropertyDefinition(extensionContainerDef, PIRACY_WEAPON, DOMUtil.XSD_STRING, 0, -1);
        PrismAsserts.assertIndexed(extensionContainerDef, PIRACY_WEAPON, true);
        PrismAsserts.assertPropertyDefinition(extensionContainerDef, PIRACY_LOOT, DOMUtil.XSD_INT, 0, 1);
        PrismAsserts.assertIndexed(extensionContainerDef, PIRACY_LOOT, true);
        PrismAsserts.assertPropertyDefinition(extensionContainerDef, PIRACY_BAD_LUCK, DOMUtil.XSD_LONG, 0, -1);
        PrismAsserts.assertIndexed(extensionContainerDef, PIRACY_BAD_LUCK, null);
        PrismAsserts.assertPropertyDefinition(extensionContainerDef, PIRACY_FUNERAL_TIMESTAMP, DOMUtil.XSD_DATETIME, 0, 1);
        PrismAsserts.assertIndexed(extensionContainerDef, PIRACY_FUNERAL_TIMESTAMP, true);
	}
	
	@Test
    public void test301AddUserDeGhoulash() throws Exception {
		final String TEST_NAME = "test301AddUserDeGhoulash";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
        
        PrismObject<UserType> user = PrismTestUtil.parseObject(USER_DEGHOULASH_FILE);
        ObjectDelta<UserType> userAddDelta = ObjectDelta.createAddDelta(user);
        Collection<ObjectDelta<? extends ObjectType>> deltas = MiscSchemaUtil.createCollection(userAddDelta);
                        
		// WHEN
        modelService.executeChanges(deltas, null, task, getCheckingProgressListenerCollection(), result);
		
		// THEN
		result.computeStatus();
        TestUtil.assertSuccess("executeChanges result", result);
        
		PrismObject<UserType> userDeGhoulash = getUser(USER_DEGHOULASH_OID);
		display("User after change execution", userDeGhoulash);
		assertUser(userDeGhoulash, USER_DEGHOULASH_OID, "deghoulash", "Charles DeGhoulash", "Charles", "DeGhoulash");
        assertAccounts(USER_DEGHOULASH_OID, 0);
                
        // Check audit
        display("Audit", dummyAuditService);
        dummyAuditService.assertRecords(2);
        dummyAuditService.assertSimpleRecordSanity();
        dummyAuditService.assertAnyRequestDeltas();
        dummyAuditService.assertExecutionDeltas(1);
        dummyAuditService.assertHasDelta(ChangeType.ADD, UserType.class);
        dummyAuditService.assertExecutionSuccess();
        
        assertBasicDeGhoulashExtension(userDeGhoulash);
	}
	
	@Test
    public void test310SearchDeGhoulashByShip() throws Exception {
		final String TEST_NAME = "test310SearchDeGhoulashByShip";
        searchDeGhoulash(TEST_NAME, PIRACY_SHIP, "The Undead Pot");
	}

	// There is no test311SearchDeGhoulashByTales
	// We cannot search by "tales". This is non-indexed string.

	@Test
    public void test312SearchDeGhoulashByWeaponSpoon() throws Exception {
		final String TEST_NAME = "test312SearchDeGhoulashByWeaponSpoon";
        searchDeGhoulash(TEST_NAME, PIRACY_WEAPON, "spoon");
	}

	@Test
    public void test313SearchDeGhoulashByWeaponFork() throws Exception {
		final String TEST_NAME = "test313SearchDeGhoulashByWeaponFork";
        searchDeGhoulash(TEST_NAME, PIRACY_WEAPON, "fork");
	}

	@Test
    public void test314SearchDeGhoulashByLoot() throws Exception {
		final String TEST_NAME = "test314SearchDeGhoulashByLoot";
        searchDeGhoulash(TEST_NAME, PIRACY_LOOT, 424242);
	}

	@Test
    public void test315SearchDeGhoulashByBadLuck13() throws Exception {
		final String TEST_NAME = "test315SearchDeGhoulashByBadLuck13";
        searchDeGhoulash(TEST_NAME, PIRACY_BAD_LUCK, 13L);
	}

	// The "badLuck" property is non-indexed. But it is long, therefore it is still searchable
	@Test
    public void test316SearchDeGhoulashByBadLuck28561() throws Exception {
		final String TEST_NAME = "test316SearchDeGhoulashByBadLuck28561";
        searchDeGhoulash(TEST_NAME, PIRACY_BAD_LUCK, 28561L);
	}
	
	@Test
    public void test317SearchDeGhoulashByFuneralTimestamp() throws Exception {
		final String TEST_NAME = "test317SearchDeGhoulashByFuneralTimestamp";
        searchDeGhoulash(TEST_NAME, PIRACY_FUNERAL_TIMESTAMP, USER_DEGHOULASH_FUNERAL_TIMESTAMP);
	}

	
    private <T> void searchDeGhoulash(String testName, QName propName, T propValue) throws Exception {
        TestUtil.displayTestTile(this, testName);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + testName);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
                     
        // Simple query
        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_EXTENSION, propName).eq(propValue)
                .build();
		// WHEN, THEN
		searchDeGhoulash(testName, query, task, result);
		
		// Complex query, combine with a name. This results in join down in the database
        query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_NAME).eq(USER_DEGHOULASH_NAME)
                .and().item(UserType.F_EXTENSION, propName).eq(propValue)
                .build();
		// WHEN, THEN
		searchDeGhoulash(testName, query, task, result);
	}
    
    private <T> void searchDeGhoulash(String testName, ObjectQuery query, Task task, OperationResult result) throws Exception {
		// WHEN
        List<PrismObject<UserType>> users = modelService.searchObjects(UserType.class, query, null, task, result);
		
		// THEN
		result.computeStatus();
        TestUtil.assertSuccess("executeChanges result", result);
        assertFalse("No user found", users.isEmpty());
        assertEquals("Wrong number of users found", 1, users.size());
        PrismObject<UserType> userDeGhoulash = users.iterator().next();
		display("Found user", userDeGhoulash);
		assertUser(userDeGhoulash, USER_DEGHOULASH_OID, "deghoulash", "Charles DeGhoulash", "Charles", "DeGhoulash");
                        
		assertBasicDeGhoulashExtension(userDeGhoulash);
	}
    
    private void assertBasicDeGhoulashExtension(PrismObject<UserType> userDeGhoulash) {
    	assertExtension(userDeGhoulash, PIRACY_SHIP, "The Undead Pot");
        assertExtension(userDeGhoulash, PIRACY_TALES, treasureIsland);
        assertExtension(userDeGhoulash, PIRACY_WEAPON, "fork", "spoon");
        assertExtension(userDeGhoulash, PIRACY_LOOT, 424242);
        assertExtension(userDeGhoulash, PIRACY_BAD_LUCK, 13L, 169L, 2197L, 28561L, 371293L, 131313131313131313L);
        assertExtension(userDeGhoulash, PIRACY_FUNERAL_TIMESTAMP, USER_DEGHOULASH_FUNERAL_TIMESTAMP);
    }
    
    /**
     * Idiot and Stupid are cyclic roles. The assignment should fail.
     */
	@Test
    public void test330AssignDeGhoulashIdiot() throws Exception {
		final String TEST_NAME = "test330AssignDeGhoulashIdiot";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
                                
        try {
			// WHEN
	        assignRole(USER_DEGHOULASH_OID, ROLE_IDIOT_OID, task, result);
	        
	        AssertJUnit.fail("Unexpected success");
        } catch (PolicyViolationException e) {
        	// This is expected
        	display("Expected exception", e);
        }
		
		// THEN
		result.computeStatus();
        TestUtil.assertFailure(result);
        
		PrismObject<UserType> userDeGhoulash = getUser(USER_DEGHOULASH_OID);
		display("User after change execution", userDeGhoulash);
		assertUser(userDeGhoulash, USER_DEGHOULASH_OID, "deghoulash", "Charles DeGhoulash", "Charles", "DeGhoulash");
		assertAssignedNoRole(userDeGhoulash);                
	}

    /**
     * Recursion role points to itself. The assignment should fail.
     */
	@Test
    public void test332AssignDeGhoulashRecursion() throws Exception {
		final String TEST_NAME = "test332AssignDeGhoulashRecursion";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
                                
        try {
			// WHEN
	        assignRole(USER_DEGHOULASH_OID, ROLE_RECURSION_OID, task, result);
	        
	        AssertJUnit.fail("Unexpected success");
        } catch (PolicyViolationException e) {
        	// This is expected
        	display("Expected exception", e);
        }
		
		// THEN
		result.computeStatus();
        TestUtil.assertFailure(result);
        
		PrismObject<UserType> userDeGhoulash = getUser(USER_DEGHOULASH_OID);
		display("User after change execution", userDeGhoulash);
		assertUser(userDeGhoulash, USER_DEGHOULASH_OID, "deghoulash", "Charles DeGhoulash", "Charles", "DeGhoulash");
		assertAssignedNoRole(userDeGhoulash);                
	}
	
	@Test
    public void test340AssignDeGhoulashConstructionNonExistentResource() throws Exception {
		final String TEST_NAME = "test340AssignDeGhoulashConstructionNonExistentResource";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
            
        // WHEN
        // We have loose referential consistency. Even if the target resource is not present
        // the assignment should be added. The error is indicated in the result.
    	assignAccount(USER_DEGHOULASH_OID, RESOURCE_NONEXISTENT_OID, null, task, result);
		
		// THEN
		result.computeStatus();
		display("Result", result);
        TestUtil.assertFailure(result);
        
		PrismObject<UserType> userDeGhoulash = getUser(USER_DEGHOULASH_OID);
		display("User after change execution", userDeGhoulash);
		assertUser(userDeGhoulash, USER_DEGHOULASH_OID, "deghoulash", "Charles DeGhoulash", "Charles", "DeGhoulash");
		assertAssignments(userDeGhoulash, 1);
	}
	
	@Test
    public void test349UnAssignDeGhoulashConstructionNonExistentResource() throws Exception {
		final String TEST_NAME = "test349UnAssignDeGhoulashConstructionNonExistentResource";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
            
        // WHEN
    	unassignAccount(USER_DEGHOULASH_OID, RESOURCE_NONEXISTENT_OID, null, task, result);
		
		// THEN
		result.computeStatus();
		display("Result", result);
        TestUtil.assertFailure(result);
        
		PrismObject<UserType> userDeGhoulash = getUser(USER_DEGHOULASH_OID);
		display("User after change execution", userDeGhoulash);
		assertUser(userDeGhoulash, USER_DEGHOULASH_OID, "deghoulash", "Charles DeGhoulash", "Charles", "DeGhoulash");
		assertAssignments(userDeGhoulash, 0);
	}
	
	@Test
    public void test350AssignDeGhoulashRoleBadConstructionResourceRef() throws Exception {
		final String TEST_NAME = "test350AssignDeGhoulashRoleBadConstructionResourceRef";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
                                
		// WHEN
        assignRole(USER_DEGHOULASH_OID, ROLE_BAD_CONSTRUCTION_RESOURCE_REF_OID, task, result);
	        		
		// THEN
		result.computeStatus();
		display("result", result);
        TestUtil.assertPartialError(result);
        String message = result.getMessage();
        TestUtil.assertMessageContains(message, "role:"+ROLE_BAD_CONSTRUCTION_RESOURCE_REF_OID);
        TestUtil.assertMessageContains(message, "Bad resourceRef in construction");
        TestUtil.assertMessageContains(message, "this-oid-does-not-exist");
        
		PrismObject<UserType> userDeGhoulash = getUser(USER_DEGHOULASH_OID);
		display("User after change execution", userDeGhoulash);
		assertUser(userDeGhoulash, USER_DEGHOULASH_OID, "deghoulash", "Charles DeGhoulash", "Charles", "DeGhoulash");
		assertAssignedRole(userDeGhoulash, ROLE_BAD_CONSTRUCTION_RESOURCE_REF_OID);
	}
	
	@Test
    public void test351UnAssignDeGhoulashRoleBadConstructionResourceRef() throws Exception {
		final String TEST_NAME = "test351UnAssignDeGhoulashRoleBadConstructionResourceRef";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
                                
		// WHEN
        unassignRole(USER_DEGHOULASH_OID, ROLE_BAD_CONSTRUCTION_RESOURCE_REF_OID, task, result);
	        		
		// THEN
		result.computeStatus();
		display("result", result);
        TestUtil.assertPartialError(result);
        String message = result.getMessage();
        TestUtil.assertMessageContains(message, "role:"+ROLE_BAD_CONSTRUCTION_RESOURCE_REF_OID);
        TestUtil.assertMessageContains(message, "Bad resourceRef in construction");
        TestUtil.assertMessageContains(message, "this-oid-does-not-exist");
        
		PrismObject<UserType> userDeGhoulash = getUser(USER_DEGHOULASH_OID);
		display("User after change execution", userDeGhoulash);
		assertUser(userDeGhoulash, USER_DEGHOULASH_OID, "deghoulash", "Charles DeGhoulash", "Charles", "DeGhoulash");
		assertAssignedNoRole(userDeGhoulash);
	}

	@Test
    public void test360AddRoleTargetBadConstructionResourceRef() throws Exception {
		final String TEST_NAME = "test360AddRoleTargetBadConstructionResourceRef";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();

		// WHEN
    	addObject(ROLE_TARGET_BAD_CONSTRUCTION_RESOURCE_REF_FILE, task, result);
	        		
		// THEN
		result.computeStatus();
		display("result", result);
        TestUtil.assertPartialError(result);
        String message = result.getMessage();
        TestUtil.assertMessageContains(message, "role:"+ROLE_META_BAD_CONSTRUCTION_RESOURCE_REF_OID);
        TestUtil.assertMessageContains(message, "Bad resourceRef in construction metarole");
        TestUtil.assertMessageContains(message, "this-oid-does-not-exist");     
	}

    @Test
    public void test400ImportJackMockTask() throws Exception {
		final String TEST_NAME = "test400ImportJackMockTask";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
        
        // WHEN
        TestUtil.displayWhen(TEST_NAME);
        importObjectFromFile(TASK_MOCK_JACK_FILE);
        
        // THEN
        TestUtil.displayThen(TEST_NAME);
        result.computeStatus();
        assertSuccess(result);
        
        waitForTaskFinish(TASK_MOCK_JACK_OID, false);
    }
    
    @Test
    public void test401ListTasks() throws Exception {
		final String TEST_NAME = "test401ListTasks";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
        
        // WHEN
        TestUtil.displayWhen(TEST_NAME);
        List<PrismObject<TaskType>> objects = modelService.searchObjects(TaskType.class, null, null, task, result);
        
        // THEN
        TestUtil.displayThen(TEST_NAME);
        result.computeStatus();
        assertSuccess(result);
        
        display("Tasks", objects);
        assertEquals("Unexpected number of tasks", 1, objects.size());
        boolean found = false;
        for (PrismObject<TaskType> object: objects) {
        	if (object.getOid().equals(TASK_MOCK_JACK_OID)) {
        		found = true;
        	}
        }
        assertTrue("Mock task not found (model)", found);
        
//        ClusterStatusInformation clusterStatusInformation = taskManager.getRunningTasksClusterwide(result);
//        display("Cluster status", clusterStatusInformation);
//        TaskInfo jackTaskInfo = null;
//        Set<TaskInfo> taskInfos = clusterStatusInformation.getTasks();
//        for (TaskInfo taskInfo: taskInfos) {
//        	if (taskInfo.getOid().equals(TASK_MOCK_JACK_OID)) {
//        		jackTaskInfo = taskInfo;
//        	}
//        }
//        assertNotNull("Mock task not found (taskManager)", jackTaskInfo);
        
        // Make sure that the tasks still runs
        waitForTaskFinish(TASK_MOCK_JACK_OID, false);
        
    }
    
    /**
     * Delete user jack. See that Jack's tasks are still there (although they may be broken)
     */
    @Test
    public void test410DeleteJack() throws Exception {
		final String TEST_NAME = "test410DeleteJack";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        assumeAssignmentPolicy(AssignmentPolicyEnforcementType.FULL);
        dummyAuditService.clear();
        
        // WHEN
        TestUtil.displayWhen(TEST_NAME);
        deleteObject(UserType.class, USER_JACK_OID, task, result);
        
        // THEN
        TestUtil.displayThen(TEST_NAME);
        result.computeStatus();
        assertSuccess(result);
        
        // Make sure the user is gone
        assertNoObject(UserType.class, USER_JACK_OID, task, result);
        
        List<PrismObject<TaskType>> objects = modelService.searchObjects(TaskType.class, null, null, task, result);
        display("Tasks", objects);
        assertEquals("Unexpected number of tastsk", 1, objects.size());
        PrismObject<TaskType> jackTask = null;
        for (PrismObject<TaskType> object: objects) {
        	if (object.getOid().equals(TASK_MOCK_JACK_OID)) {
        		jackTask = object;
        	}
        }
        assertNotNull("Mock task not found (model)", jackTask);
        display("Jack's task (model)", jackTask);
        
//        ClusterStatusInformation clusterStatusInformation = taskManager.getRunningTasksClusterwide(result);
//        display("Cluster status", clusterStatusInformation);
//        TaskInfo jackTaskInfo = null;
//        Set<TaskInfo> taskInfos = clusterStatusInformation.getTasks();
//        for (TaskInfo taskInfo: taskInfos) {
//        	if (taskInfo.getOid().equals(TASK_MOCK_JACK_OID)) {
//        		jackTaskInfo = taskInfo;
//        	}
//        }
//        assertNotNull("Mock task not found (taskManager)", jackTaskInfo);
//        display("Jack's task (taskManager)", jackTaskInfo);
        
        // TODO: check task status
        
    }
    
    @Test
    public void test500EnumerationExtension() throws Exception {
		final String TEST_NAME = "test500EnumerationExtension";
        TestUtil.displayTestTile(this, TEST_NAME);

        PrismObjectDefinition<UserType> userDef = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismPropertyDefinition<String> markDef = userDef.findPropertyDefinition(new ItemPath(UserType.F_EXTENSION, PIRACY_MARK));
        
        // WHEN
        TestUtil.assertSetEquals("Wrong allowedValues in mark", MiscUtil.getValuesFromDisplayableValues(markDef.getAllowedValues()), 
        		"pegLeg","noEye","hook","tatoo","scar","bravery");
        
        for (DisplayableValue<String> disp: markDef.getAllowedValues()) {
        	if (disp.getValue().equals("pegLeg")) {
        		assertEquals("Wrong pegLeg label", "Peg Leg", disp.getLabel());
        	}
        }
    }
    
    @Test
    public void test502EnumerationStoreGood() throws Exception {
		final String TEST_NAME = "test502EnumerationStoreGood";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        
        PrismObjectDefinition<UserType> userDef = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismPropertyDefinition<String> markDef = userDef.findPropertyDefinition(new ItemPath(UserType.F_EXTENSION, PIRACY_MARK));
        
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        dummyAuditService.clear();
        
        // WHEN
        modifyObjectReplaceProperty(UserType.class, USER_GUYBRUSH_OID, new ItemPath(UserType.F_EXTENSION, PIRACY_MARK), 
        		task, result, "bravery");
        
        // THEN
        TestUtil.displayThen(TEST_NAME);
        result.computeStatus();
        assertSuccess(result);
        
        PrismObject<UserType> user = getUser(USER_GUYBRUSH_OID);
        PrismProperty<String> markProp = user.findProperty(new ItemPath(UserType.F_EXTENSION, PIRACY_MARK));
        assertEquals("Bad mark", "bravery", markProp.getRealValue());
    }
    
    /**
     * Guybrush has stored mark "bravery". Change schema so this value becomes illegal.
     * They try to read it.
     */
    @Test // MID-2260
    public void test510EnumerationGetBad() throws Exception {
		final String TEST_NAME = "test510EnumerationGetBad";
        TestUtil.displayTestTile(this, TEST_NAME);

        PrismObjectDefinition<UserType> userDef = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismPropertyDefinition<String> markDef = userDef.findPropertyDefinition(new ItemPath(UserType.F_EXTENSION, PIRACY_MARK));
        Iterator<? extends DisplayableValue<String>> iterator = markDef.getAllowedValues().iterator();
		DisplayableValue<String> braveryValue = null;
        while (iterator.hasNext()) {
        	DisplayableValue<String> disp = iterator.next();
        	if (disp.getValue().equals("bravery")) {
				braveryValue = disp;
        		iterator.remove();
        	}
        }

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        dummyAuditService.clear();
        
        // WHEN
        PrismObject<UserType> user = modelService.getObject(UserType.class, USER_GUYBRUSH_OID, null, task, result);
        
        // THEN
        TestUtil.displayThen(TEST_NAME);
        result.computeStatus();
        assertSuccess(result);
        
        PrismProperty<String> markProp = user.findProperty(new ItemPath(UserType.F_EXTENSION, PIRACY_MARK));
        assertEquals("Bad mark", null, markProp.getRealValue());

		((Collection) markDef.getAllowedValues()).add(braveryValue);		// because of the following test
    }
    
    /**
     * Store value in extension/ship. Then remove extension/ship definition from the schema.
     * The next read should NOT fail.
     */
    @Test
    public void test520ShipReadBad() throws Exception {
		final String TEST_NAME = "test520ShipReadBad";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        dummyAuditService.clear();
        
        modifyObjectReplaceProperty(UserType.class, USER_GUYBRUSH_OID, new ItemPath(UserType.F_EXTENSION, PIRACY_SHIP), 
        		task, result, "The Pink Lady");
        result.computeStatus();
        assertSuccess(result);
        
        PrismObjectDefinition<UserType> userDef = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(UserType.class);
        PrismContainerDefinition<?> extensionDefinition = userDef.getExtensionDefinition();
        List<? extends ItemDefinition> extensionDefs = extensionDefinition.getComplexTypeDefinition().getDefinitions();
		for (ItemDefinition itemDefinition : extensionDefs) {
			if (itemDefinition.getName().equals(PIRACY_SHIP)) {
				//iterator.remove();	// not possible as the collection is unmodifiable
				((ItemDefinitionImpl) itemDefinition).setName(new QName(NS_PIRACY, "ship-broken"));
			}
		}
        
        // WHEN
        modelService.getObject(UserType.class, USER_GUYBRUSH_OID, null, task, result);
        
        // THEN
        TestUtil.displayThen(TEST_NAME);
        result.computeStatus();
        assertSuccess(result);
    }
    
    /**
     * Circus resource has a circular dependency. It should fail, but it should
     * fail with a proper error.
     * MID-3522
     */
    @Test
    public void test550AssignCircus() throws Exception {
		final String TEST_NAME = "test550AssignCircus";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        
        Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();
        
        try {
	        // WHEN
	        assignAccount(USER_GUYBRUSH_OID, RESOURCE_DUMMY_CIRCUS_OID, null, task, result);
	        
	        assertNotReached();
        } catch (PolicyViolationException e) {
        	// THEN
            TestUtil.displayThen(TEST_NAME);
            result.computeStatus();
            TestUtil.assertFailure(result);
        }
        
    }
    
    @Test
    public void test600AddUserGuybrushAssignAccount() throws Exception {
		final String TEST_NAME="test600AddUserGuybrushAssignAccount";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        
        PrismObject<UserType> userBefore = getUser(USER_GUYBRUSH_OID);
		display("User before", userBefore);
        
        Collection<ObjectDelta<? extends ObjectType>> deltas = new ArrayList<ObjectDelta<? extends ObjectType>>();
        ObjectDelta<UserType> accountAssignmentUserDelta = createAccountAssignmentUserDelta(USER_GUYBRUSH_OID, RESOURCE_DUMMY_OID, null, true);
        deltas.add(accountAssignmentUserDelta);
        
		// WHEN
        TestUtil.displayWhen(TEST_NAME);
		modelService.executeChanges(deltas, null, task, getCheckingProgressListenerCollection(), result);
		
		// THEN
		TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
        TestUtil.assertSuccess(result);
        
		PrismObject<UserType> userAfter = getUser(USER_GUYBRUSH_OID);
		display("User after change execution", userAfter);
        accountGuybrushOid = getSingleLinkOid(userAfter);
        
		// Check shadow
        PrismObject<ShadowType> accountShadow = repositoryService.getObject(ShadowType.class, accountGuybrushOid, null, result);
        assertDummyAccountShadowRepo(accountShadow, accountGuybrushOid, USER_GUYBRUSH_USERNAME);
        
        // Check account
        PrismObject<ShadowType> accountModel = modelService.getObject(ShadowType.class, accountGuybrushOid, null, task, result);
        assertDummyAccountShadowModel(accountModel, accountGuybrushOid, USER_GUYBRUSH_USERNAME, USER_GUYBRUSH_FULL_NAME);
        
        // Check account in dummy resource
        assertDefaultDummyAccount(USER_GUYBRUSH_USERNAME, USER_GUYBRUSH_FULL_NAME, true);
    }
    
    /**
     * Set attribute that is not in the schema directly into dummy resource.
     * Get that account. Make sure that the operations does not die.
     */
    @Test(enabled=false) // MID-2880
    public void test610GetAccountGuybrushRogueAttribute() throws Exception {
    	final String TEST_NAME="test600AddUserGuybrushAssignAccount";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = taskManager.createTaskInstance(TestStrangeCases.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        
        getDummyResource().setEnforceSchema(false);
    	DummyAccount dummyAccount = getDummyAccount(null, USER_GUYBRUSH_USERNAME);
    	dummyAccount.addAttributeValues("rogue", "habakuk");
    	getDummyResource().setEnforceSchema(true);
    	
    	// WHEN
    	TestUtil.displayWhen(TEST_NAME);
		PrismObject<ShadowType> shadow = modelService.getObject(ShadowType.class, accountGuybrushOid, null, task, result);
		
		// THEN
		TestUtil.displayThen(TEST_NAME);
		result.computeStatus();
        TestUtil.assertSuccess(result);
		
        display("Shadow after", shadow);
        assertDummyAccountShadowModel(shadow, accountGuybrushOid, USER_GUYBRUSH_USERNAME, USER_GUYBRUSH_FULL_NAME);
        
        assertDummyAccountAttribute(null, USER_GUYBRUSH_USERNAME, "rogue", "habakuk");
    }

    
	private <O extends ObjectType, T> void assertExtension(PrismObject<O> object, QName propName, T... expectedValues) {
		PrismContainer<Containerable> extensionContainer = object.findContainer(ObjectType.F_EXTENSION);
		assertNotNull("No extension container in "+object, extensionContainer);
		PrismProperty<T> extensionProperty = extensionContainer.findProperty(propName);
		assertNotNull("No extension property "+propName+" in "+object, extensionProperty);
		PrismAsserts.assertPropertyValues("Values of extension property "+propName, extensionProperty.getValues(), expectedValues);
	}

	/** 
	 * Break the user in the repo by inserting accountRef that points nowhere. 
	 */
	private void addBrokenAccountRef(String userOid) throws ObjectNotFoundException, SchemaException, ObjectAlreadyExistsException {
		OperationResult result = new OperationResult(TestStrangeCases.class.getName() + ".addBrokenAccountRef");
		
		Collection<? extends ItemDelta> modifications = ReferenceDelta.createModificationAddCollection(UserType.class, 
				UserType.F_LINK_REF, prismContext, NON_EXISTENT_ACCOUNT_OID);
		repositoryService.modifyObject(UserType.class, userOid, modifications , result);
		
		result.computeStatus();
		TestUtil.assertSuccess(result);
	}

}
