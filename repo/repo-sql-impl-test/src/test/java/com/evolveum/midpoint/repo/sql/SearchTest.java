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

package com.evolveum.midpoint.repo.sql;

import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.Objectable;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.match.PolyStringOrigMatchingRule;
import com.evolveum.midpoint.prism.match.PolyStringStrictMatchingRule;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.query.AndFilter;
import com.evolveum.midpoint.prism.query.EqualFilter;
import com.evolveum.midpoint.prism.query.NotFilter;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrderDirection;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.prism.query.builder.QueryBuilder;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.schema.ResultHandler;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * @author lazyman
 */
@ContextConfiguration(locations = {"../../../../../ctx-test.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SearchTest extends BaseSQLRepoTest {

    private static final Trace LOGGER = TraceManager.getTrace(SearchTest.class);

    @BeforeClass
    public void beforeClass() throws Exception {
        super.beforeClass();

        PrismTestUtil.resetPrismContext(MidPointPrismContextFactory.FACTORY);

        List<PrismObject<? extends Objectable>> objects = prismContext.parserFor(new File(FOLDER_BASIC, "objects.xml")).parseObjects();
        objects.addAll(prismContext.parserFor(new File(FOLDER_BASIC, "objects-2.xml")).parseObjects());

        OperationResult result = new OperationResult("add objects");
        for (PrismObject object : objects) {
            repositoryService.addObject(object, null, result);
        }

        result.recomputeStatus();
        assertTrue(result.isSuccess());
    }

    @Test
    public void iterateEmptySet() throws Exception {
        OperationResult result = new OperationResult("search empty");

        ResultHandler handler = (object, parentResult) -> {
			fail();
			return false;
		};

        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_NAME).eqPoly("asdf", "asdf").matchingStrict()
                .build();

        repositoryService.searchObjectsIterative(UserType.class, query, handler, null, false, result);
        result.recomputeStatus();

        assertTrue(result.isSuccess());
    }

    @Test
    public void iterateSet() throws Exception {
        OperationResult result = new OperationResult("search set");

        final List<PrismObject> objects = new ArrayList<PrismObject>();

        ResultHandler handler = (object, parentResult) -> {
			objects.add(object);
			return true;
		};

        repositoryService.searchObjectsIterative(UserType.class, null, handler, null, false, result);
        result.recomputeStatus();

        assertTrue(result.isSuccess());
        assertEquals(3, objects.size());
    }

    @Test
    public void iterateSetWithPaging() throws Exception {
        iterateGeneral(0, 2, 2, "atestuserX00002", "atestuserX00003");
        iterateGeneral(0, 2, 1, "atestuserX00002", "atestuserX00003");
        iterateGeneral(0, 1, 10, "atestuserX00002");
        iterateGeneral(0, 1, 1, "atestuserX00002");
        iterateGeneral(1, 1, 1, "atestuserX00003");
    }

    private void iterateGeneral(int offset, int size, int batch, final String... names) throws Exception {
        OperationResult result = new OperationResult("search general");

        final List<PrismObject> objects = new ArrayList<PrismObject>();

        ResultHandler handler = new ResultHandler() {

            int index = 0;
            @Override
            public boolean handle(PrismObject object, OperationResult parentResult) {
                objects.add(object);
                assertEquals("Incorrect object name was read", names[index++], object.asObjectable().getName().getOrig());
                return true;
            }
        };

        SqlRepositoryConfiguration config = ((SqlRepositoryServiceImpl) repositoryService).getConfiguration();
        int oldbatch = config.getIterativeSearchByPagingBatchSize();
        config.setIterativeSearchByPagingBatchSize(batch);

        LOGGER.trace(">>>>>> iterateGeneral: offset = " + offset + ", size = " + size + ", batch = " + batch + " <<<<<<");

        ObjectQuery query = new ObjectQuery();
        query.setPaging(ObjectPaging.createPaging(offset, size, ObjectType.F_NAME, OrderDirection.ASCENDING));
        repositoryService.searchObjectsIterative(UserType.class, query, handler, null, false, result);
        result.recomputeStatus();

        config.setIterativeSearchByPagingBatchSize(oldbatch);

        assertTrue(result.isSuccess());
        assertEquals(size, objects.size());
    }

    @Test
    public void caseSensitiveSearchTest() throws Exception {
        final String existingNameOrig = "Test UserX00003";
        final String nonExistingNameOrig = "test UserX00003";
        final String nameNorm = "test userx00003";

        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_FULL_NAME).eqPoly(existingNameOrig, nameNorm).matchingOrig()
                .build();

        OperationResult result = new OperationResult("search");
        List<PrismObject<UserType>> users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one user", 1, users.size());

        query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_FULL_NAME).eqPoly(nonExistingNameOrig, nameNorm).matchingOrig()
                .build();

        users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Found user (shouldn't) because case insensitive search was used", 0, users.size());
    }

    @Test
    public void roleMembershipSearchTest() throws Exception {
        PrismReferenceValue r456 = new PrismReferenceValue("r456", RoleType.COMPLEX_TYPE);
        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_ROLE_MEMBERSHIP_REF).ref(r456)
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<UserType>> users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one user", 1, users.size());
        assertEquals("Wrong user name", "atestuserX00003", users.get(0).getName().getOrig());

        PrismReferenceValue r123 = new PrismReferenceValue("r123", RoleType.COMPLEX_TYPE);
        query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_ROLE_MEMBERSHIP_REF).ref(r123)
                .build();
        users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find two users", 2, users.size());
    }

    @Test
    public void delegatedSearchTest() throws Exception {
        PrismReferenceValue r789 = new PrismReferenceValue("r789", RoleType.COMPLEX_TYPE);
        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_DELEGATED_REF).ref(r789)
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<UserType>> users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one user", 1, users.size());
        assertEquals("Wrong user name", "atestuserX00003", users.get(0).getName().getOrig());

        PrismReferenceValue r123 = new PrismReferenceValue("r123", RoleType.COMPLEX_TYPE);
        query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_DELEGATED_REF).ref(r123)
                .build();
        users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find no users", 0, users.size());
    }


    @Test
    public void assignmentOrgRefSearchTest() throws Exception {
        PrismReferenceValue o123456 = new PrismReferenceValue("o123456", OrgType.COMPLEX_TYPE);
        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_ASSIGNMENT, AssignmentType.F_ORG_REF).ref(o123456)
                .build();

        OperationResult result = new OperationResult("search");
        List<PrismObject<UserType>> users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one user", 1, users.size());
        assertEquals("Wrong user name", "atestuserX00002", users.get(0).getName().getOrig());

        PrismReferenceValue o999 = new PrismReferenceValue("o999", RoleType.COMPLEX_TYPE);
        query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_ASSIGNMENT, AssignmentType.F_ORG_REF).ref(o999)
                .build();

        users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find zero users", 0, users.size());
    }

    @Test
    public void assignmentResourceRefSearchTest() throws Exception {
        PrismReferenceValue resourceRef = new PrismReferenceValue("10000000-0000-0000-0000-000000000004", ResourceType.COMPLEX_TYPE);
        ObjectQuery query = QueryBuilder.queryFor(RoleType.class, prismContext)
                .item(RoleType.F_ASSIGNMENT, AssignmentType.F_CONSTRUCTION, ConstructionType.F_RESOURCE_REF).ref(resourceRef)
                .build();

        OperationResult result = new OperationResult("search");
        List<PrismObject<RoleType>> roles = repositoryService.searchObjects(RoleType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one role", 1, roles.size());
        assertEquals("Wrong role name", "Judge", roles.get(0).getName().getOrig());

        PrismReferenceValue resourceRef2 = new PrismReferenceValue("FFFFFFFF-0000-0000-0000-000000000004", ResourceType.COMPLEX_TYPE);
        query = QueryBuilder.queryFor(RoleType.class, prismContext)
                .item(RoleType.F_ASSIGNMENT, AssignmentType.F_CONSTRUCTION, ConstructionType.F_RESOURCE_REF).ref(resourceRef2)
                .build();
        roles = repositoryService.searchObjects(RoleType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find zero roles", 0, roles.size());
    }

    @Test
    public void roleAssignmentSearchTest() throws Exception {
        PrismReferenceValue r456 = new PrismReferenceValue("r123", RoleType.COMPLEX_TYPE);
        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_ASSIGNMENT, AssignmentType.F_TARGET_REF).ref(r456)
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<UserType>> users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one user", 1, users.size());
        assertEquals("Wrong user name", "atestuserX00002", users.get(0).getName().getOrig());

    }
    
    @Test
    public void orgAssignmentSearchTest() throws Exception {
        PrismReferenceValue org = new PrismReferenceValue("00000000-8888-6666-0000-100000000085", OrgType.COMPLEX_TYPE);
        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_ASSIGNMENT, AssignmentType.F_TARGET_REF).ref(org)
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<UserType>> users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one user", 1, users.size());
        assertEquals("Wrong user name", "atestuserX00002", users.get(0).getName().getOrig());

    }
    
    @Test
    public void roleAndOrgAssignmentSearchTest() throws Exception {
        PrismReferenceValue r123 = new PrismReferenceValue("r123", RoleType.COMPLEX_TYPE);
        PrismReferenceValue org = new PrismReferenceValue("00000000-8888-6666-0000-100000000085", OrgType.COMPLEX_TYPE);
        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_ASSIGNMENT, AssignmentType.F_TARGET_REF).ref(r123)
                .and().item(UserType.F_ASSIGNMENT, AssignmentType.F_TARGET_REF).ref(org)
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<UserType>> users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one user", 1, users.size());
        assertEquals("Wrong user name", "atestuserX00002", users.get(0).getName().getOrig());

    }

    @Test
    public void notBusinessRoleTypeSearchTest() throws Exception {
        ObjectQuery query = QueryBuilder.queryFor(RoleType.class, prismContext)
                .not().item(RoleType.F_ROLE_TYPE).eq("business")
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<RoleType>> roles = repositoryService.searchObjects(RoleType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find two roles", 2, roles.size());

        int judge = roles.get(0).getName().getOrig().startsWith("J") ? 0 : 1;
        assertEquals("Wrong role1 name", "Judge", roles.get(judge).getName().getOrig());
        assertEquals("Wrong role2 name", "Admin-owned role", roles.get(1-judge).getName().getOrig());
    }

    @Test
    public void businessRoleTypeSearchTest() throws Exception {
        ObjectQuery query = QueryBuilder.queryFor(RoleType.class, prismContext)
                .item(RoleType.F_ROLE_TYPE).eq("business")
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<RoleType>> roles = repositoryService.searchObjects(RoleType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one role", 1, roles.size());
        assertEquals("Wrong role name", "Pirate", roles.get(0).getName().getOrig());

    }

    @Test
    public void emptyRoleTypeSearchTest() throws Exception {
        ObjectQuery query = QueryBuilder.queryFor(RoleType.class, prismContext)
                .item(RoleType.F_ROLE_TYPE).isNull()
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<RoleType>> roles = repositoryService.searchObjects(RoleType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find two roles", 2, roles.size());

        int judge = roles.get(0).getName().getOrig().startsWith("J") ? 0 : 1;
        assertEquals("Wrong role1 name", "Judge", roles.get(judge).getName().getOrig());
        assertEquals("Wrong role2 name", "Admin-owned role", roles.get(1-judge).getName().getOrig());
    }

    @Test
    public void nonEmptyRoleTypeSearchTest() throws Exception {
        ObjectQuery query = QueryBuilder.queryFor(RoleType.class, prismContext)
                .not().item(RoleType.F_ROLE_TYPE).isNull()
                .build();
        OperationResult result = new OperationResult("search");
        List<PrismObject<RoleType>> roles = repositoryService.searchObjects(RoleType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Should find one role", 1, roles.size());
        assertEquals("Wrong role name", "Pirate", roles.get(0).getName().getOrig());

    }

    @Test
    public void testIndividualOwnerRef() throws Exception {
        testOwnerRef(RoleType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "Admin-owned role");
        testOwnerRef(RoleType.class, null, "Judge", "Pirate");
        testOwnerRef(RoleType.class, "123");

        testOwnerRef(OrgType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "Admin-owned org");
        testOwnerRef(OrgType.class, null, "F0085");
        testOwnerRef(OrgType.class, "123");

        testOwnerRef(TaskType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "Synchronization: Embedded Test OpenDJ");
        testOwnerRef(TaskType.class, null, "Task with no owner");
        testOwnerRef(TaskType.class, "123");

        testOwnerRef(AccessCertificationCampaignType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "All user assignments 1");
        testOwnerRef(AccessCertificationCampaignType.class, null, "No-owner campaign");
        testOwnerRef(AccessCertificationCampaignType.class, "123");

        testOwnerRef(AccessCertificationDefinitionType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "Admin-owned definition");
        testOwnerRef(AccessCertificationDefinitionType.class, null);
        testOwnerRef(AccessCertificationDefinitionType.class, "123");
    }

    @Test
    public void testOwnerRefWithTypeRestriction() throws Exception {
        testOwnerRefWithTypeRestriction(RoleType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "Admin-owned role");
        testOwnerRefWithTypeRestriction(RoleType.class, null, "Judge", "Pirate");
        testOwnerRefWithTypeRestriction(RoleType.class, "123");

        testOwnerRefWithTypeRestriction(OrgType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "Admin-owned org");
        testOwnerRefWithTypeRestriction(OrgType.class, null, "F0085");
        testOwnerRefWithTypeRestriction(OrgType.class, "123");

        testOwnerRefWithTypeRestriction(TaskType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "Synchronization: Embedded Test OpenDJ");
        testOwnerRefWithTypeRestriction(TaskType.class, null, "Task with no owner");
        testOwnerRefWithTypeRestriction(TaskType.class, "123");

        testOwnerRefWithTypeRestriction(AccessCertificationCampaignType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "All user assignments 1");
        testOwnerRefWithTypeRestriction(AccessCertificationCampaignType.class, null, "No-owner campaign");
        testOwnerRefWithTypeRestriction(AccessCertificationCampaignType.class, "123");

        testOwnerRefWithTypeRestriction(AccessCertificationDefinitionType.class, SystemObjectsType.USER_ADMINISTRATOR.value(), "Admin-owned definition");
        testOwnerRefWithTypeRestriction(AccessCertificationDefinitionType.class, null);
        testOwnerRefWithTypeRestriction(AccessCertificationDefinitionType.class, "123");
    }

    private void testOwnerRef(Class<? extends ObjectType> clazz, String oid, String... names) throws SchemaException {
        ObjectQuery query = QueryBuilder.queryFor(clazz, prismContext)
                .item(new QName(SchemaConstants.NS_C, "ownerRef")).ref(oid)
                .build();
        checkResult(clazz, clazz, oid, query, names);
    }

    private void testOwnerRefWithTypeRestriction(Class<? extends ObjectType> clazz, String oid, String... names) throws SchemaException {
        ObjectQuery query = QueryBuilder.queryFor(ObjectType.class, prismContext)
                .type(clazz)
                    .item(new QName(SchemaConstants.NS_C, "ownerRef")).ref(oid)
                .build();
        checkResult(ObjectType.class, clazz, oid, query, names);
    }

    private void checkResult(Class<? extends ObjectType> queryClass, Class<? extends ObjectType> realClass, String oid, ObjectQuery query, String[] names)
            throws SchemaException {
        OperationResult result = new OperationResult("search");
        SearchResultList<? extends PrismObject<? extends ObjectType>> objects = repositoryService.searchObjects(queryClass, query, null, result);
        System.out.println(realClass.getSimpleName() + " owned by " + oid + ": " + objects.size());
        assertEquals("Wrong # of found objects", names.length, objects.size());
        Set<String> expectedNames = new HashSet<>(Arrays.asList(names));
        Set<String> realNames = new HashSet<>();
        for (PrismObject<? extends ObjectType> object : objects) {
            realNames.add(object.asObjectable().getName().getOrig());
        }
        assertEquals("Wrong names of found objects", expectedNames, realNames);
    }

    @Test
    public void testWildOwnerRef() throws SchemaException {
        final String oid = SystemObjectsType.USER_ADMINISTRATOR.value();
        ItemDefinition<?> ownerRefDef = prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(RoleType.class).findItemDefinition(RoleType.F_OWNER_REF);
        ObjectQuery query = QueryBuilder.queryFor(ObjectType.class, prismContext)
                .item(new ItemPath(new QName(SchemaConstants.NS_C, "ownerRef")), ownerRefDef).ref(oid)
                .build();
        OperationResult result = new OperationResult("search");
        try {
            repositoryService.searchObjects(ObjectType.class, query, null, result);
            fail("Ambiguous searchObjects succeeded even if it should have failed.");
        } catch (SystemException e) {
            assertTrue("Wrong exception message: " + e.getMessage(), e.getMessage().contains("Unable to determine root entity for ownerRef"));
        }
    }

	@Test
	public void testResourceUp() throws SchemaException {
		ObjectQuery query = QueryBuilder.queryFor(ResourceType.class, prismContext)
				.item(ResourceType.F_OPERATIONAL_STATE, OperationalStateType.F_LAST_AVAILABILITY_STATUS).eq(AvailabilityStatusType.UP)
				.build();
		OperationResult result = new OperationResult("search");
		List<PrismObject<ResourceType>> resources = repositoryService.searchObjects(ResourceType.class, query, null, result);
		result.recomputeStatus();
		assertTrue(result.isSuccess());
		assertEquals("Should find one resource", 1, resources.size());
	}

	@Test
	public void testMultivaluedExtensionPropertySubstringQualified() throws SchemaException {
		ObjectQuery query = QueryBuilder.queryFor(GenericObjectType.class, prismContext)
				.item(ObjectType.F_EXTENSION, new QName("http://example.com/p", "multivalued")).contains("slava")
				.build();
		OperationResult result = new OperationResult("search");
		List<PrismObject<GenericObjectType>> resources = repositoryService.searchObjects(GenericObjectType.class, query, null, result);
		result.recomputeStatus();
		assertTrue(result.isSuccess());
		assertEquals("Should find one object", 1, resources.size());
	}

	@Test
	public void testMultivaluedExtensionPropertyEqualsQualified() throws SchemaException {
		ObjectQuery query = QueryBuilder.queryFor(GenericObjectType.class, prismContext)
				.item(ObjectType.F_EXTENSION, new QName("http://example.com/p", "multivalued")).eq("Bratislava")
				.build();
		OperationResult result = new OperationResult("search");
		List<PrismObject<GenericObjectType>> resources = repositoryService.searchObjects(GenericObjectType.class, query, null, result);
		result.recomputeStatus();
		assertTrue(result.isSuccess());
		assertEquals("Should find one object", 1, resources.size());
	}

	@Test
	public void testMultivaluedExtensionPropertySubstringUnqualified() throws SchemaException {
		ObjectQuery query = QueryBuilder.queryFor(GenericObjectType.class, prismContext)
				.item(ObjectType.F_EXTENSION, new QName("multivalued")).contains("slava")
				.build();
		OperationResult result = new OperationResult("search");
		List<PrismObject<GenericObjectType>> resources = repositoryService.searchObjects(GenericObjectType.class, query, null, result);
		result.recomputeStatus();
		assertTrue(result.isSuccess());
		assertEquals("Should find one object", 1, resources.size());
	}

	@Test
	public void testMultivaluedExtensionPropertyEqualsUnqualified() throws SchemaException {
		ObjectQuery query = QueryBuilder.queryFor(GenericObjectType.class, prismContext)
				.item(ObjectType.F_EXTENSION, new QName("multivalued")).eq("Bratislava")
				.build();
		OperationResult result = new OperationResult("search");
		List<PrismObject<GenericObjectType>> resources = repositoryService.searchObjects(GenericObjectType.class, query, null, result);
		result.recomputeStatus();
		assertTrue(result.isSuccess());
		assertEquals("Should find one object", 1, resources.size());
	}

	@Test
	public void testRoleAttributes() throws SchemaException {
		ObjectQuery query = QueryBuilder.queryFor(RoleType.class, prismContext)
				.item(RoleType.F_RISK_LEVEL).eq("critical")
				.and().item(RoleType.F_IDENTIFIER).eq("123")
				.and().item(RoleType.F_DISPLAY_NAME).eqPoly("The honest one", "").matchingOrig()
				.build();
		OperationResult result = new OperationResult("search");
		List<PrismObject<RoleType>> roles = repositoryService.searchObjects(RoleType.class, query, null, result);
		result.recomputeStatus();
		assertTrue(result.isSuccess());
		assertEquals("Should find one object", 1, roles.size());
	}

	// testing MID-3568
    @Test
    public void caseInsensitiveSearchTest() throws Exception {
        final String existingNameNorm = "test userx00003";
        final String existingNameOrig = "Test UserX00003";
        final String emailLowerCase = "testuserx00003@example.com";
        final String emailVariousCase = "TeStUsErX00003@EXAmPLE.com";

        assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
                        .item(UserType.F_FULL_NAME).eqPoly(existingNameNorm).matchingNorm()
                        .build(),
                1);

        assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_FULL_NAME).eqPoly(existingNameOrig).matchingNorm()
                .build(),
                1);

        assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_EMAIL_ADDRESS).eq(emailLowerCase).matchingCaseIgnore()
                .build(),
                1);

        assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_EMAIL_ADDRESS).eq(emailVariousCase).matchingCaseIgnore()
                .build(),
                1);

        // comparing polystrings, but providing plain String
		assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
						.item(UserType.F_FULL_NAME).eq(existingNameNorm).matchingNorm()
						.build(),
				1);

		assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
						.item(UserType.F_FULL_NAME).eq(existingNameOrig).matchingNorm()
						.build(),
				1);

		assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
                        .item(UserType.F_FULL_NAME).containsPoly(existingNameNorm).matchingNorm()
                        .build(),
                1);

        assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_FULL_NAME).containsPoly(existingNameOrig).matchingNorm()
                .build(),
                1);

        assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_EMAIL_ADDRESS).contains(emailLowerCase).matchingCaseIgnore()
                .build(),
                1);

        assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_EMAIL_ADDRESS).contains(emailVariousCase).matchingCaseIgnore()
                .build(),
                1);

		// comparing polystrings, but providing plain String
		assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
						.item(UserType.F_FULL_NAME).contains(existingNameNorm).matchingNorm()
						.build(),
				1);

		assertObjectsFound(QueryBuilder.queryFor(UserType.class, prismContext)
						.item(UserType.F_FULL_NAME).contains(existingNameOrig).matchingNorm()
						.build(),
				1);
	}

    @SuppressWarnings("SameParameterValue")
	private void assertObjectsFound(ObjectQuery query, int expectedCount) throws Exception {
    	assertObjectsFoundBySearch(query, expectedCount);
    	assertObjectsFoundByCount(query, expectedCount);
	}

    private void assertObjectsFoundBySearch(ObjectQuery query, int expectedCount) throws Exception {
        OperationResult result = new OperationResult("search");
        List<PrismObject<UserType>> users = repositoryService.searchObjects(UserType.class, query, null, result);
        result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Wrong # of results found: " + query, expectedCount, users.size());
    }

    private void assertObjectsFoundByCount(ObjectQuery query, int expectedCount) throws Exception {
        OperationResult result = new OperationResult("count");
		int count = repositoryService.countObjects(UserType.class, query, result);
		result.recomputeStatus();
        assertTrue(result.isSuccess());
        assertEquals("Wrong # of results found: " + query, expectedCount, count);
    }

}
