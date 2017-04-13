/*
 * Copyright (c) 2010-2014 Evolveum
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

package com.evolveum.midpoint.model.impl.scripting;

import com.evolveum.midpoint.model.api.ScriptExecutionException;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EventHandlerType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

import java.util.*;

/**
 * Data that are passed between individual scripting actions.
 *
 * The content passed between actions (expressions) is a list of prism values
 * (object, container, reference, property).
 *
 * @author mederly
 */
public class Data implements DebugDumpable {

    private final List<PrismValue> data = new ArrayList<>();			// all items are not null

    // we want clients to use explicit constructors
    private Data() {
    }

    public List<PrismValue> getData() {
        return data;
    }

    @Override
    public String debugDump() {
        return debugDump(0);
    }

    @Override
    public String debugDump(int indent) {
        return DebugUtil.debugDump(data, indent);
    }

    public static Data create(Item data) {
        Data d = createEmpty();
        d.addItem(data);
        return d;
    }

    public static Data createEmpty() {
        return new Data();
    }

    public void addAllFrom(Data otherData) {
        if (otherData != null) {
            data.addAll(otherData.getData());
        }
    }

    @Deprecated
    public void addItem(@NotNull Item<?,?> item) {
        data.addAll(item.getValues());
    }

    public void addValue(PrismValue value) {
        data.add(value);
    }

    public void addValues(List<PrismValue> values) {
        data.addAll(values);
    }

    public String getDataAsSingleString() throws ScriptExecutionException {
        if (!data.isEmpty()) {
            if (data.size() == 1) {
                return (String) ((PrismPropertyValue) data.get(0)).getRealValue();       // todo implement some diagnostics when this would fail
            } else {
                throw new ScriptExecutionException("Multiple values where just one is expected");
            }
        } else {
            return null;
        }
    }

    public static Data createItem(PrismValue value, PrismContext prismContext) throws SchemaException {
        Data data = createEmpty();
        if (value != null) {
            data.addValue(value);
        }
        return data;
//        // TODO fix this temporary solution (haven't we somewhere universal method to do this?)
//        if (value instanceof PrismReferenceValue) {
//            PrismReference ref = new PrismReference(new QName("reference"));
//            ref.add((PrismReferenceValue) value);
//            return create(ref);
//        } else if (value instanceof PrismContainerValue) {
//            PrismContainerValue pcv = (PrismContainerValue) value;
//            return create(pcv.asSingleValuedContainer(new QName("container")));
//        } else if (value instanceof PrismPropertyValue) {
//            if (value.isRaw()) {
//                throw new IllegalArgumentException("Value cannot be raw at this point: " + value);
//            }
//            Class<?> clazz = value.getRealClass();
//            assert clazz != null;
//            PrismPropertyDefinition<?> propertyDefinition;
//            List<PrismPropertyDefinition> defs = prismContext.getSchemaRegistry()
//                    .findItemDefinitionsByCompileTimeClass(clazz, PrismPropertyDefinition.class);
//            if (defs.size() == 1) {
//                propertyDefinition = defs.get(0);
//            } else if (String.class.isAssignableFrom(clazz)) {
//                propertyDefinition = new PrismPropertyDefinitionImpl<>(PLAIN_STRING_ELEMENT_NAME, DOMUtil.XSD_STRING, prismContext);
//            } else if (ObjectDeltaType.class.isAssignableFrom(clazz)) {
//                propertyDefinition = new PrismPropertyDefinitionImpl<>(SchemaConstants.T_OBJECT_DELTA, SchemaConstants.T_OBJECT_DELTA_TYPE, prismContext);
//            } else if (EventHandlerType.class.isAssignableFrom(clazz)) {
//                propertyDefinition = new PrismPropertyDefinitionImpl<>(SchemaConstants.C_EVENT_HANDLER, EventHandlerType.COMPLEX_TYPE, prismContext);
//            } else {
//                // maybe determine type from class would be sufficient
//                TypeDefinition td = prismContext.getSchemaRegistry().findTypeDefinitionByCompileTimeClass(clazz, TypeDefinition.class);
//                if (td != null) {
//                    propertyDefinition = new PrismPropertyDefinitionImpl<>(SchemaConstants.C_VALUE, td.getTypeName(), prismContext);
//                } else {
//                    throw new IllegalStateException(
//                            "Unsupported data class (to be put into scripting data as property): " + clazz);
//                }
//            }
//            PrismProperty<?> property = propertyDefinition.instantiate();
//            property.add((PrismPropertyValue) value);
//            return create(property);
//        } else if (value == null) {
//            return createEmpty();
//        } else {
//            throw new IllegalArgumentException("Unsupported prism value: " + value);
//        }
    }

    public Collection<ObjectReferenceType> getDataAsReferences(QName defaultTargetType) throws ScriptExecutionException {
        Collection<ObjectReferenceType> retval = new ArrayList<>(data.size());
        for (PrismValue value : data) {
            if (value instanceof PrismObjectValue) {
                PrismObjectValue objectValue = (PrismObjectValue) value;
                ObjectReferenceType ref = new ObjectReferenceType();
                ref.setType(objectValue.asPrismObject().getDefinition().getTypeName()); // todo check the definition is present
                ref.setOid(objectValue.getOid());                  // todo check if oid is present
                retval.add(ref);
            } else if (value instanceof PrismPropertyValue) {
                Object realValue = ((PrismPropertyValue) value).getRealValue();
                if (realValue instanceof String) {
                    ObjectReferenceType ref = new ObjectReferenceType();
                    ref.setType(defaultTargetType);
                    ref.setOid((String) realValue);                         // todo implement search by name
                    retval.add(ref);
                } else if (realValue instanceof ObjectReferenceType) {
                    retval.add((ObjectReferenceType) realValue);
                } else {
                    throw new ScriptExecutionException("Unsupported reference type: " + value.getClass());
                }
            } else if (value instanceof PrismReferenceValue) {
                PrismReferenceValue referenceValue = (PrismReferenceValue) value;
                ObjectReferenceType ref = new ObjectReferenceType();
                ref.setupReferenceValue(referenceValue);
                retval.add(ref);
            }
        }
        return retval;
    }
}
