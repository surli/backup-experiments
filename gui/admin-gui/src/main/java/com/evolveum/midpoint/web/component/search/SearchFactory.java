/*
 * Copyright (c) 2010-2015 Evolveum
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

package com.evolveum.midpoint.web.component.search;

import com.evolveum.midpoint.model.api.ModelInteractionService;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.schema.ResourceShadowDiscriminator;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Viliam Repan (lazyman)
 */
public class SearchFactory {

    private static final String DOT_CLASS = SearchFactory.class.getName() + ".";
    private static final String LOAD_OBJECT_DEFINITION = DOT_CLASS + "loadObjectDefinition";

    private static final Map<Class, List<ItemPath>> SEARCHABLE_OBJECTS = new HashMap<>();

    static {
        SEARCHABLE_OBJECTS.put(ObjectType.class, Arrays.asList(
                new ItemPath(ObjectType.F_NAME),
                new ItemPath(ObjectType.F_LIFECYCLE_STATE)));
        SEARCHABLE_OBJECTS.put(FocusType.class, Arrays.asList(
                new ItemPath(FocusType.F_ACTIVATION, ActivationType.F_ADMINISTRATIVE_STATUS),
                new ItemPath(FocusType.F_ACTIVATION, ActivationType.F_EFFECTIVE_STATUS)));
        SEARCHABLE_OBJECTS.put(UserType.class, Arrays.asList(
                new ItemPath(UserType.F_GIVEN_NAME),
                new ItemPath(UserType.F_FAMILY_NAME),
                new ItemPath(UserType.F_FULL_NAME),
                new ItemPath(UserType.F_ADDITIONAL_NAME),
                new ItemPath(UserType.F_COST_CENTER),
                new ItemPath(UserType.F_EMAIL_ADDRESS),
                new ItemPath(UserType.F_TELEPHONE_NUMBER),
                new ItemPath(UserType.F_EMPLOYEE_NUMBER),
                new ItemPath(UserType.F_EMPLOYEE_TYPE),
                new ItemPath(UserType.F_ORGANIZATIONAL_UNIT),
                new ItemPath(UserType.F_COST_CENTER),
                new ItemPath(UserType.F_LOCALITY)));
        SEARCHABLE_OBJECTS.put(RoleType.class, Arrays.asList(
                new ItemPath(RoleType.F_NAME),
                new ItemPath(RoleType.F_ROLE_TYPE)));
        SEARCHABLE_OBJECTS.put(ServiceType.class, Arrays.asList(
                new ItemPath(ServiceType.F_NAME),
                new ItemPath(ServiceType.F_SERVICE_TYPE),
                new ItemPath(ServiceType.F_URL)));
        SEARCHABLE_OBJECTS.put(ConnectorHostType.class, Arrays.asList(
                new ItemPath(ConnectorHostType.F_HOSTNAME)
        ));
        SEARCHABLE_OBJECTS.put(ConnectorType.class, Arrays.asList(
                new ItemPath(ConnectorType.F_CONNECTOR_BUNDLE),
                new ItemPath(ConnectorType.F_CONNECTOR_VERSION),
                new ItemPath(ConnectorType.F_CONNECTOR_TYPE)
        ));
        SEARCHABLE_OBJECTS.put(AbstractRoleType.class, Arrays.asList(
        		new ItemPath(AbstractRoleType.F_IDENTIFIER),
                new ItemPath(AbstractRoleType.F_REQUESTABLE)
        ));
        SEARCHABLE_OBJECTS.put(OrgType.class, Arrays.asList(
                new ItemPath(OrgType.F_DISPLAY_NAME),
                new ItemPath(OrgType.F_COST_CENTER),
                new ItemPath(OrgType.F_ORG_TYPE),
                new ItemPath(OrgType.F_TENANT),
                new ItemPath(OrgType.F_LOCALITY)
        ));
        SEARCHABLE_OBJECTS.put(GenericObjectType.class, Arrays.asList(
                new ItemPath(GenericObjectType.F_OBJECT_TYPE)
        ));
        SEARCHABLE_OBJECTS.put(NodeType.class, Arrays.asList(
                new ItemPath(NodeType.F_NODE_IDENTIFIER)
        ));
        SEARCHABLE_OBJECTS.put(ReportType.class, Arrays.asList(
                new ItemPath(ReportType.F_PARENT)
        ));
        SEARCHABLE_OBJECTS.put(ShadowType.class, Arrays.asList(
                new ItemPath(ShadowType.F_OBJECT_CLASS),
                new ItemPath(ShadowType.F_DEAD),
                new ItemPath(ShadowType.F_INTENT),
                new ItemPath(ShadowType.F_EXISTS),
                new ItemPath(ShadowType.F_SYNCHRONIZATION_SITUATION),
                new ItemPath(ShadowType.F_FAILED_OPERATION_TYPE)
        ));
        SEARCHABLE_OBJECTS.put(TaskType.class, Arrays.asList(
                new ItemPath(TaskType.F_TASK_IDENTIFIER),
                new ItemPath(TaskType.F_NODE),
                new ItemPath(TaskType.F_CATEGORY),
                new ItemPath(TaskType.F_RESULT_STATUS)
        ));
    }

    public static <T extends ObjectType> Search createSearchForShadow(
            ResourceShadowDiscriminator discriminator, PrismContext ctx, ModelInteractionService modelInteractionService) {
        return createSearch(ShadowType.class, discriminator, ctx, modelInteractionService, true);
    }

    public static <T extends ObjectType> Search createSearch(Class<T> type, PrismContext ctx,
                                                             ModelInteractionService modelInteractionService) {
        return createSearch(type, null, ctx, modelInteractionService, true);
    }

    public static <T extends ObjectType> Search createSearch(
            Class<T> type, ResourceShadowDiscriminator discriminator, PrismContext ctx,
            ModelInteractionService modelInteractionService, boolean useDefsFromSuperclass) {

        PrismObjectDefinition objectDef = findObjectDefinition(type, discriminator, ctx, modelInteractionService);

        Map<ItemPath, ItemDefinition> availableDefs = getAvailableDefinitions(objectDef, useDefsFromSuperclass);

        Search search = new Search(type, availableDefs);

        SchemaRegistry registry = ctx.getSchemaRegistry();
        PrismObjectDefinition objDef = registry.findObjectDefinitionByCompileTimeClass(ObjectType.class);
        PrismPropertyDefinition def = objDef.findPropertyDefinition(ObjectType.F_NAME);

        SearchItem item = search.addItem(def);
        if (item != null) {
            item.setFixed(true);
        }

        return search;
    }

    private static <T extends ObjectType> PrismObjectDefinition findObjectDefinition(
            Class<T> type, ResourceShadowDiscriminator discriminator, PrismContext ctx,
            ModelInteractionService modelInteractionService) {

        try {
            if (Modifier.isAbstract(type.getModifiers())) {
                SchemaRegistry registry = ctx.getSchemaRegistry();
                return registry.findObjectDefinitionByCompileTimeClass(type);
            }

            OperationResult result = new OperationResult(LOAD_OBJECT_DEFINITION);

            PrismObject empty = ctx.createObject(type);

            if (ShadowType.class.equals(type)) {
                return modelInteractionService.getEditShadowDefinition(discriminator,
                        AuthorizationPhaseType.REQUEST, result);
            } else {
                return modelInteractionService.getEditObjectDefinition(
                        empty, AuthorizationPhaseType.REQUEST, result);
            }
        } catch (SchemaException | ConfigurationException | ObjectNotFoundException ex) {
            throw new SystemException(ex);
        }
    }

    private static <T extends ObjectType> Map<ItemPath, ItemDefinition> getAvailableDefinitions(
            PrismObjectDefinition<T> objectDef, boolean useDefsFromSuperclass) {
        Map<ItemPath, ItemDefinition> map = new HashMap<>();

        map.putAll(createExtensionDefinitionList(objectDef));

        Class<T> typeClass = objectDef.getCompileTimeClass();
        while (typeClass != null && !com.evolveum.prism.xml.ns._public.types_3.ObjectType.class.equals(typeClass)) {
            List<ItemPath> paths = SEARCHABLE_OBJECTS.get(typeClass);
            if (paths != null) {
                for (ItemPath path : paths) {
                    ItemDefinition def = objectDef.findItemDefinition(path);
                    if (def != null) {
                        map.put(path, def);
                    }
                }
            }

            if (!useDefsFromSuperclass) {
                break;
            }

            typeClass = (Class<T>) typeClass.getSuperclass();
        }

        return map;
    }

    private static <T extends ObjectType> Map<ItemPath, ItemDefinition> createExtensionDefinitionList(
            PrismObjectDefinition<T> objectDef) {

        Map<ItemPath, ItemDefinition> map = new HashMap<>();

        ItemPath extensionPath = new ItemPath(ObjectType.F_EXTENSION);

        PrismContainerDefinition ext = objectDef.findContainerDefinition(ObjectType.F_EXTENSION);
        for (ItemDefinition def : (List<ItemDefinition>) ext.getDefinitions()) {
            if (!(def instanceof PrismPropertyDefinition)
                    && !(def instanceof PrismReferenceDefinition)) {
                continue;
            }

            map.put(new ItemPath(extensionPath, def.getName()), def);
        }

        return map;
    }
}
