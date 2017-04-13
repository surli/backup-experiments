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

package com.evolveum.midpoint.repo.sql.data.common.any;

import com.evolveum.midpoint.prism.*;
import org.apache.commons.lang.Validate;

/**
 * @author lazyman
 */
public enum RValueType {

    PROPERTY(PrismProperty.class, PrismPropertyValue.class),
    CONTAINER(PrismContainer.class, PrismContainerValue.class),
    OBJECT(PrismObject.class, PrismContainerValue.class),
    REFERENCE(PrismReference.class, PrismReferenceValue.class);

    private Class<? extends Item> itemClass;
    private Class<? extends PrismValue> valueClass;

    private RValueType(Class<? extends Item> itemClass, Class<? extends PrismValue> valueClass) {
        this.itemClass = itemClass;
        this.valueClass = valueClass;
    }

    public Class<? extends PrismValue> getValueClass() {
        return valueClass;
    }

    public Class<? extends Item> getItemClass() {
        return itemClass;
    }

    public static RValueType getTypeFromItemClass(Class<? extends Item> clazz) {
        Validate.notNull(clazz, "Class must not be null.");
        for (RValueType value : RValueType.values()) {
            if (value.getItemClass().isAssignableFrom(clazz)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown enum value type for '" + clazz.getName() + "'.");
    }

    public static RValueType getTypeFromValueClass(Class<? extends PrismValue> clazz) {
        Validate.notNull(clazz, "Class must not be null.");
        for (RValueType value : RValueType.values()) {
            if (value.getValueClass().isAssignableFrom(clazz)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown enum value type for '" + clazz.getName() + "'.");
    }
}
