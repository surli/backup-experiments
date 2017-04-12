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

package com.evolveum.midpoint.schema;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.PrismPropertyDefinitionImpl;
import com.evolveum.midpoint.prism.delta.ChangeType;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.constants.MidPointConstants;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ActivationStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ActivationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FailedOperationTypeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MetadataType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;

import javax.xml.namespace.QName;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author lazyman
 */
public class TestDiffEquals {

    private static final String NS_TEST_RI = "http://midpoint.evolveum.com/xml/ns/test/ri-1";

	@BeforeSuite
    public void setup() throws SchemaException, SAXException, IOException {
        PrettyPrinter.setDefaultNamespacePrefix(MidPointConstants.NS_MIDPOINT_PUBLIC_PREFIX);
        PrismTestUtil.resetPrismContext(MidPointPrismContextFactory.FACTORY);
    }

    @Test
    public void testUserSimplePropertyDiff() throws SchemaException {
    	System.out.println("\n\n===[ testUserSimplePropertyDiff ]===\n");
        UserType userType1 = new UserType();
        userType1.setName(PrismTestUtil.createPolyStringType("test name"));
        UserType userType2 = new UserType();
        userType2.setName(PrismTestUtil.createPolyStringType("test name"));
        PrismTestUtil.getPrismContext().adopt(userType1);
        PrismTestUtil.getPrismContext().adopt(userType2);

        ObjectDelta delta = userType1.asPrismObject().diff(userType2.asPrismObject());
        assertNotNull(delta);
        assertEquals(0, delta.getModifications().size());

        userType2.setDescription(null);

        delta = userType1.asPrismObject().diff(userType2.asPrismObject());
        assertNotNull(delta);
        assertEquals("Delta should be empty, nothing changed.", 0, delta.getModifications().size());
    }

    @Test
    public void testUserListSimpleDiff() throws SchemaException {
    	System.out.println("\n\n===[ testUserListSimpleDiff ]===\n");
        UserType u1 = new UserType();
        u1.setName(PrismTestUtil.createPolyStringType("test name"));
        UserType u2 = new UserType();
        u2.setName(PrismTestUtil.createPolyStringType("test name"));
        PrismTestUtil.getPrismContext().adopt(u1);
        PrismTestUtil.getPrismContext().adopt(u2);

        ObjectDelta delta = u1.asPrismObject().diff(u2.asPrismObject());
        assertNotNull(delta);
        assertEquals(0, delta.getModifications().size());

        u2.getAdditionalName();

        delta = u1.asPrismObject().diff(u2.asPrismObject());
        assertNotNull(delta);
        assertEquals("Delta should be empty, nothing changed.", 0, delta.getModifications().size());
    }

    @Test
    public void testAssignmentEquals() throws Exception {
    	System.out.println("\n\n===[ testAssignmentEquals ]===\n");
    	PrismContext prismContext = PrismTestUtil.getPrismContext();
    	
        AssignmentType a1a = new AssignmentType();
        prismContext.adopt(a1a);
        a1a.setDescription("descr1");

        AssignmentType a2 = new AssignmentType();
        prismContext.adopt(a2);
        a2.setDescription("descr2");

        AssignmentType a1b = new AssignmentType();
        prismContext.adopt(a1b);
        a1b.setDescription("descr1");
        
        AssignmentType a1m = new AssignmentType();
        prismContext.adopt(a1m);
        a1m.setDescription("descr1");
        MetadataType metadata1m = new MetadataType();
        metadata1m.setCreateTimestamp(XmlTypeConverter.createXMLGregorianCalendar(System.currentTimeMillis()));
		a1m.setMetadata(metadata1m);
		
		AssignmentType a1e = new AssignmentType();
        prismContext.adopt(a1e);
        a1e.setDescription("descr1");
        ActivationType activation1e = new ActivationType();
        activation1e.setEffectiveStatus(ActivationStatusType.ENABLED);
        a1e.setActivation(activation1e);
        
        // WHEN
        assertFalse(a1a.equals(a2));
        assertFalse(a1b.equals(a2));
        assertFalse(a1m.equals(a2));
        assertFalse(a1e.equals(a2));
        assertFalse(a2.equals(a1a));
        assertFalse(a2.equals(a1b));
        assertFalse(a2.equals(a1m));
        assertFalse(a2.equals(a1e));
        
        assertTrue(a1a.equals(a1a));
        assertTrue(a1b.equals(a1b));
        assertTrue(a1m.equals(a1m));
        assertTrue(a1e.equals(a1e));
        assertTrue(a2.equals(a2));
        
        assertTrue(a1a.equals(a1b));
        assertTrue(a1b.equals(a1a));
        assertTrue(a1a.equals(a1m));
        assertTrue(a1b.equals(a1m));
        assertTrue(a1m.equals(a1a));
        assertTrue(a1m.equals(a1b));
        assertTrue(a1m.equals(a1e));
        assertTrue(a1a.equals(a1e));
        assertTrue(a1b.equals(a1e));
        assertTrue(a1e.equals(a1a));
        assertTrue(a1e.equals(a1b));
        assertTrue(a1e.equals(a1m));
    }
    
    @Test
    public void testAssignmentEquivalent() throws Exception {
    	System.out.println("\n\n===[ testAssignmentEquivalent ]===\n");
    	PrismContext prismContext = PrismTestUtil.getPrismContext();

        AssignmentType a1 = new AssignmentType(prismContext);
        ActivationType a1a = new ActivationType(prismContext);
        a1a.setValidFrom(XmlTypeConverter.createXMLGregorianCalendar(new Date()));
        a1a.setEffectiveStatus(ActivationStatusType.ENABLED);
		a1.setActivation(a1a);

        AssignmentType a2 = new AssignmentType(prismContext);
		ActivationType a2a = new ActivationType(prismContext);
		a2a.setEffectiveStatus(ActivationStatusType.ENABLED);
		a2.setActivation(a2a);

        // WHEN
        assertFalse(a1.equals(a2));
        assertFalse(a1.asPrismContainerValue().equivalent(a2.asPrismContainerValue()));			// a bit redundant

		assertFalse(a2.equals(a1));
		assertFalse(a2.asPrismContainerValue().equivalent(a1.asPrismContainerValue()));			// a bit redundant
    }

    @Test
    public void testContextlessAssignmentEquals() throws Exception {
    	System.out.println("\n\n===[ testContextlessAssignmentEquals ]===\n");
        AssignmentType a1 = new AssignmentType();            // no prismContext here
        a1.setDescription("descr1");

        AssignmentType a2 = new AssignmentType();            // no prismContext here
        a2.setDescription("descr2");

        AssignmentType a3 = new AssignmentType();            // no prismContext here
        a3.setDescription("descr1");

        assertFalse(a1.equals(a2));                          // this should work even without prismContext
        assertTrue(a1.equals(a3));                           // this should work even without prismContext

        PrismContext prismContext = PrismTestUtil.getPrismContext();
        prismContext.adopt(a1);
        prismContext.adopt(a2);
        prismContext.adopt(a3);
        assertFalse(a1.equals(a2));                         // this should work as well
        assertTrue(a1.equals(a3));
    }

    @Test
    public void testContextlessAssignmentEquals2() throws Exception {
    	System.out.println("\n\n===[ testContextlessAssignmentEquals2 ]===\n");

        // (1) user without prismContext - the functionality is reduced

        UserType user = new UserType();

        AssignmentType a1 = new AssignmentType();            // no prismContext here
        a1.setDescription("descr1");
        user.getAssignment().add(a1);
        AssignmentType a2 = new AssignmentType();            // no prismContext here
        a2.setDescription("descr2");
        user.getAssignment().add(a2);

        AssignmentType a2identical = new AssignmentType();
        a2identical.setDescription("descr2");
        assertTrue(user.getAssignment().contains(a2identical));

        ObjectDelta delta1 = user.asPrismObject().createDelta(ChangeType.DELETE);       // delta1 is without prismContext
        assertNull(delta1.getPrismContext());

        // (2) user with prismContext

        UserType userWithContext = new UserType(PrismTestUtil.getPrismContext());

        AssignmentType b1 = new AssignmentType();            // no prismContext here
        b1.setDescription("descr1");
        userWithContext.getAssignment().add(b1);
        AssignmentType b2 = new AssignmentType();            // no prismContext here
        b2.setDescription("descr2");
        userWithContext.getAssignment().add(b2);

        AssignmentType b2identical = new AssignmentType();
        b2identical.setDescription("descr2");
        assertTrue(user.getAssignment().contains(b2identical));

        // b1 and b2 obtain context when they are added to the container
        assertNotNull(b1.asPrismContainerValue().getPrismContext());
        assertNotNull(b2.asPrismContainerValue().getPrismContext());
        assertFalse(b1.equals(b2));

        ObjectDelta delta2 = userWithContext.asPrismObject().createDelta(ChangeType.DELETE);
        assertNotNull(delta2.getPrismContext());
    }

    @Test
    public void testDiffShadow() throws Exception {
    	System.out.println("\n\n===[ testDiffShadow ]===\n");
    	PrismContext prismContext = PrismTestUtil.getPrismContext();

    	PrismObject<ShadowType> shadow1 = prismContext.getSchemaRegistry()
    			.findObjectDefinitionByCompileTimeClass(ShadowType.class).instantiate();
    	ShadowType shadow1Type = shadow1.asObjectable();
    	shadow1Type.setName(new PolyStringType("Whatever"));
    	shadow1Type.setFailedOperationType(FailedOperationTypeType.ADD);
    	shadow1Type.getAuxiliaryObjectClass().add(new QName(NS_TEST_RI, "foo"));
    	PrismContainer<Containerable> shadow1Attrs = shadow1.findOrCreateContainer(ShadowType.F_ATTRIBUTES);
    	
    	ShadowType shadow2Type = new ShadowType();
    	PrismObject<ShadowType> shadow2 = shadow2Type.asPrismObject();
    	prismContext.adopt(shadow2Type);
    	shadow2Type.setName(new PolyStringType("Whatever"));
    	shadow2Type.getAuxiliaryObjectClass().add(new QName(NS_TEST_RI, "foo"));
    	shadow2Type.getAuxiliaryObjectClass().add(new QName(NS_TEST_RI, "bar"));
    	PrismContainer<Containerable> shadow2Attrs = shadow2.findOrCreateContainer(ShadowType.F_ATTRIBUTES);
    	
    	PrismProperty<String> attrEntryUuid = new PrismProperty<>(new QName(NS_TEST_RI, "entryUuid"), prismContext);
    	PrismPropertyDefinition<String> attrEntryUuidDef = new PrismPropertyDefinitionImpl<>(new QName(NS_TEST_RI, "entryUuid"),
    			DOMUtil.XSD_STRING, prismContext);
    	attrEntryUuid.setDefinition(attrEntryUuidDef);
		shadow2Attrs.add(attrEntryUuid);
		attrEntryUuid.addRealValue("1234-5678-8765-4321");
		
		PrismProperty<String> attrDn = new PrismProperty<>(new QName(NS_TEST_RI, "dn"), prismContext);
		PrismPropertyDefinition<String> attrDnDef = new PrismPropertyDefinitionImpl<>(new QName(NS_TEST_RI, "dn"),
    			DOMUtil.XSD_STRING, prismContext);
		attrDn.setDefinition(attrDnDef);
		shadow2Attrs.add(attrDn);
		attrDn.addRealValue("uid=foo,o=bar");
		
		System.out.println("Shadow 1");
    	System.out.println(shadow1.debugDump(1));
    	System.out.println("Shadow 2");
    	System.out.println(shadow2.debugDump(1));
    	
    	// WHEN
    	ObjectDelta<ShadowType> delta = shadow1.diff(shadow2);

    	// THEN
    	assertNotNull("No delta", delta);
    	System.out.println("Delta");
    	System.out.println(delta.debugDump(1));
    	
    	PrismAsserts.assertIsModify(delta);
    	PrismAsserts.assertPropertyDelete(delta, ShadowType.F_FAILED_OPERATION_TYPE, FailedOperationTypeType.ADD);
    	PrismAsserts.assertPropertyAdd(delta, ShadowType.F_AUXILIARY_OBJECT_CLASS, new QName(NS_TEST_RI, "bar"));
    	PrismAsserts.assertContainerAdd(delta, ShadowType.F_ATTRIBUTES, shadow2Attrs.getValue().clone());
    	PrismAsserts.assertModifications(delta, 3);
    }

    
}
