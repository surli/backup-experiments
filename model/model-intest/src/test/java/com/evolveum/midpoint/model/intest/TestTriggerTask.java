/*
 * Copyright (c) 2013 Evolveum
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertNotNull;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.evolveum.midpoint.model.impl.trigger.TriggerHandlerRegistry;
import com.evolveum.midpoint.model.intest.AbstractInitializedModelIntegrationTest;
import com.evolveum.midpoint.model.intest.util.MockTriggerHandler;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.test.util.TestUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author Radovan Semancik
 *
 * PMed: In theory, the assertions counting # of invocations could fail even if trigger task handler works well
 * - if the scanner task would run more than once. If that occurs in reality, we'll deal with it.
 *
 */
@ContextConfiguration(locations = {"classpath:ctx-model-intest-test-main.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestTriggerTask extends AbstractInitializedModelIntegrationTest {

	private static final XMLGregorianCalendar LONG_LONG_TIME_AGO = XmlTypeConverter.createXMLGregorianCalendar(1111, 1, 1, 12, 00, 00);

	private MockTriggerHandler testTriggerHandler;
	
	private XMLGregorianCalendar drakeValidFrom;
	private XMLGregorianCalendar drakeValidTo;
	
	@Autowired(required=true)
	private TriggerHandlerRegistry triggerHandlerRegistry;
	
	@Override
	public void initSystem(Task initTask, OperationResult initResult) throws Exception {
		// TODO Auto-generated method stub
		super.initSystem(initTask, initResult);
		
		testTriggerHandler = new MockTriggerHandler();
		
		triggerHandlerRegistry.register(MockTriggerHandler.HANDLER_URI, testTriggerHandler);
	}

	@Test
    public void test100ImportScannerTask() throws Exception {
		final String TEST_NAME = "test100ImportScannerTask";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = createTask(TestTriggerTask.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        
        // Make sure there is an object with a trigger set to a long time ago.
        // That trigger should be invoked on first run.
        addTrigger(USER_JACK_OID, LONG_LONG_TIME_AGO, MockTriggerHandler.HANDLER_URI);
        
        XMLGregorianCalendar startCal = clock.currentTimeXMLGregorianCalendar();
        
		/// WHEN
        TestUtil.displayWhen(TEST_NAME);
        importObjectFromFile(TASK_TRIGGER_SCANNER_FILE);
		
        waitForTaskStart(TASK_TRIGGER_SCANNER_OID, false);
        waitForTaskFinish(TASK_TRIGGER_SCANNER_OID, true);
        
        // THEN
        TestUtil.displayThen(TEST_NAME);
        XMLGregorianCalendar endCal = clock.currentTimeXMLGregorianCalendar();
        assertLastRecomputeTimestamp(TASK_TRIGGER_SCANNER_OID, startCal, endCal);
        
        assertNotNull("Trigger was not called", testTriggerHandler.getLastObject());
		assertEquals("Trigger was called incorrect number of times", 1, testTriggerHandler.getInvocationCount());
        assertNoTrigger(UserType.class, USER_JACK_OID);
        
        assertLastRecomputeTimestamp(TASK_TRIGGER_SCANNER_OID, startCal, endCal);
	}
	
	@Test
    public void test105NoTrigger() throws Exception {
		final String TEST_NAME = "test105NoTrigger";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = createTask(TestTriggerTask.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        testTriggerHandler.reset();
        
        XMLGregorianCalendar startCal = clock.currentTimeXMLGregorianCalendar();
                
		/// WHEN
        TestUtil.displayWhen(TEST_NAME);
        waitForTaskNextRunAssertSuccess(TASK_TRIGGER_SCANNER_OID, true);
		
        // THEN
        TestUtil.displayThen(TEST_NAME);
        
        // THEN
        XMLGregorianCalendar endCal = clock.currentTimeXMLGregorianCalendar();

        assertNull("Trigger was called while not expecting it", testTriggerHandler.getLastObject());
        assertNoTrigger(UserType.class, USER_JACK_OID);

        assertLastRecomputeTimestamp(TASK_TRIGGER_SCANNER_OID, startCal, endCal);
	}
	
	@Test
    public void test110TriggerCalledAgain() throws Exception {
		final String TEST_NAME = "test110TriggerCalledAgain";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = createTask(TestTriggerTask.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        testTriggerHandler.reset();
        
        XMLGregorianCalendar startCal = clock.currentTimeXMLGregorianCalendar();
        
        addTrigger(USER_JACK_OID, startCal, MockTriggerHandler.HANDLER_URI);
                
		/// WHEN
        TestUtil.displayWhen(TEST_NAME);
        waitForTaskNextRunAssertSuccess(TASK_TRIGGER_SCANNER_OID, true);
		
        // THEN
        TestUtil.displayThen(TEST_NAME);
        
        // THEN
        XMLGregorianCalendar endCal = clock.currentTimeXMLGregorianCalendar();

        assertNotNull("Trigger was not called", testTriggerHandler.getLastObject());
		assertEquals("Trigger was called incorrect number of times", 1, testTriggerHandler.getInvocationCount());
        assertNoTrigger(UserType.class, USER_JACK_OID);

        assertLastRecomputeTimestamp(TASK_TRIGGER_SCANNER_OID, startCal, endCal);
	}

	@Test
	public void test120TwoTriggers() throws Exception {
		final String TEST_NAME = "test120TwoTriggers";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = createTask(TestTriggerTask.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();
		testTriggerHandler.reset();

		XMLGregorianCalendar startCal = clock.currentTimeXMLGregorianCalendar();
		addTrigger(USER_JACK_OID, startCal, MockTriggerHandler.HANDLER_URI);

		XMLGregorianCalendar startCalPlus5ms = XmlTypeConverter.createXMLGregorianCalendar(startCal);
		startCalPlus5ms.add(XmlTypeConverter.createDuration(5L));
		addTrigger(USER_JACK_OID, startCalPlus5ms, MockTriggerHandler.HANDLER_URI);

		/// WHEN
		TestUtil.displayWhen(TEST_NAME);
		waitForTaskNextRunAssertSuccess(TASK_TRIGGER_SCANNER_OID, true);

		// THEN
		TestUtil.displayThen(TEST_NAME);

		// THEN
		XMLGregorianCalendar endCal = clock.currentTimeXMLGregorianCalendar();

		assertNotNull("Trigger was not called", testTriggerHandler.getLastObject());
		assertEquals("Trigger was called wrong number of times", 1, testTriggerHandler.getInvocationCount());
		assertNoTrigger(UserType.class, USER_JACK_OID);

		assertLastRecomputeTimestamp(TASK_TRIGGER_SCANNER_OID, startCal, endCal);
	}


	@Test
    public void test150NoTriggerAgain() throws Exception {
		final String TEST_NAME = "test115NoTriggerAgain";
        TestUtil.displayTestTile(this, TEST_NAME);

        // GIVEN
        Task task = createTask(TestTriggerTask.class.getName() + "." + TEST_NAME);
        OperationResult result = task.getResult();
        testTriggerHandler.reset();
        
        XMLGregorianCalendar startCal = clock.currentTimeXMLGregorianCalendar();
                
		/// WHEN
        TestUtil.displayWhen(TEST_NAME);
        waitForTaskNextRunAssertSuccess(TASK_TRIGGER_SCANNER_OID, true);
		
        // THEN
        TestUtil.displayThen(TEST_NAME);
        
        // THEN
        XMLGregorianCalendar endCal = clock.currentTimeXMLGregorianCalendar();

        assertNull("Trigger was called while not expecting it", testTriggerHandler.getLastObject());
        assertNoTrigger(UserType.class, USER_JACK_OID);

        assertLastRecomputeTimestamp(TASK_TRIGGER_SCANNER_OID, startCal, endCal);
	}
	
}
