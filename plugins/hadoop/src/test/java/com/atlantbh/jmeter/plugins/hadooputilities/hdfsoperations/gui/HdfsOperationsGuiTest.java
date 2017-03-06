/*
 * Copyright 2013 undera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlantbh.jmeter.plugins.hadooputilities.hdfsoperations.gui;

import com.atlantbh.jmeter.plugins.hadooputilities.hdfsoperations.HdfsOperations;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.testelement.TestElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HdfsOperationsGuiTest {

    public HdfsOperationsGuiTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        TestJMeterUtils.createJmeterEnv();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of init method, of class HdfsOperationsGui.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        HdfsOperationsGui instance = new HdfsOperationsGui();
        instance.init();
    }

    /**
     * Test of clearGui method, of class HdfsOperationsGui.
     */
    @Test
    public void testClearGui() {
        System.out.println("clearGui");
        HdfsOperationsGui instance = new HdfsOperationsGui();
        instance.clearGui();
    }

    /**
     * Test of createTestElement method, of class HdfsOperationsGui.
     */
    @Test
    public void testCreateTestElement() {
        System.out.println("createTestElement");
        HdfsOperationsGui instance = new HdfsOperationsGui();
        TestElement result = instance.createTestElement();
        assertNotNull(result);
    }

    /**
     * Test of getLabelResource method, of class HdfsOperationsGui.
     */
    @Test
    public void testGetLabelResource() {
        System.out.println("getLabelResource");
        HdfsOperationsGui instance = new HdfsOperationsGui();
        String expResult = "";
        String result = instance.getLabelResource();
    }

    /**
     * Test of getStaticLabel method, of class HdfsOperationsGui.
     */
    @Test
    public void testGetStaticLabel() {
        System.out.println("getStaticLabel");
        HdfsOperationsGui instance = new HdfsOperationsGui();
        String expResult = "";
        String result = instance.getStaticLabel();
    }

    /**
     * Test of modifyTestElement method, of class HdfsOperationsGui.
     */
    @Test
    public void testModifyTestElement() {
        System.out.println("modifyTestElement");
        TestElement element = new HdfsOperations();
        HdfsOperationsGui instance = new HdfsOperationsGui();
        instance.modifyTestElement(element);
        // TODO review the generated test code and remove the default call to fail.

    }

    /**
     * Test of configure method, of class HdfsOperationsGui.
     */
    @Test
    public void testConfigure() {
        System.out.println("configure");
        TestElement element = new HdfsOperations();
        HdfsOperationsGui instance = new HdfsOperationsGui();
        instance.configure(element);
        // TODO review the generated test code and remove the default call to fail.

    }
}
