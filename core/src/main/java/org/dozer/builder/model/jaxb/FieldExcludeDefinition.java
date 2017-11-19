/*
 * Copyright 2005-2017 Dozer Project
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
package org.dozer.builder.model.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dozer.classmap.ClassMap;
import org.dozer.classmap.MappingDirection;
import org.dozer.config.BeanContainer;
import org.dozer.factory.DestBeanCreator;
import org.dozer.fieldmap.DozerField;
import org.dozer.fieldmap.ExcludeFieldMap;
import org.dozer.fieldmap.FieldMap;
import org.dozer.propertydescriptor.PropertyDescriptorFactory;

/**
 * Exclude a particular field from being mapped
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "field-exclude")
public class FieldExcludeDefinition {

    @XmlTransient
    private final MappingDefinition parent;

    @XmlElement(name = "a", required = true)
    private FieldDefinitionDefinition a;

    @XmlElement(name = "b", required = true)
    private FieldDefinitionDefinition b;

    @XmlAttribute(name = "type")
    private Type type;

    public FieldExcludeDefinition() {
        this(null);
    }

    public FieldExcludeDefinition(MappingDefinition parent) {
        this.parent = parent;
    }

    public FieldDefinitionDefinition getA() {
        return a;
    }

    public FieldDefinitionDefinition getB() {
        return b;
    }

    public Type getType() {
        if (type == null) {
            return Type.BI_DIRECTIONAL;
        } else {
            return type;
        }
    }

    private void setA(FieldDefinitionDefinition a) {
        this.a = a;
    }

    private void setB(FieldDefinitionDefinition b) {
        this.b = b;
    }

    private void setType(Type type) {
        this.type = type;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public FieldDefinitionDefinition withA() {
        FieldDefinitionDefinition a = new FieldDefinitionDefinition(this, null);
        setA(a);

        return a;
    }

    public FieldDefinitionDefinition withB() {
        FieldDefinitionDefinition b = new FieldDefinitionDefinition(this, null);
        setB(b);

        return b;
    }

    public FieldExcludeDefinition withType(Type type) {
        setType(type);

        return this;
    }

    public MappingDefinition end() {
        return parent;
    }

    public FieldMap build(ClassMap classMap, BeanContainer beanContainer, DestBeanCreator destBeanCreator, PropertyDescriptorFactory propertyDescriptorFactory) {
        DozerField aField = a.convert();
        DozerField bField = b.convert();

        FieldMap fieldMap = new ExcludeFieldMap(classMap, beanContainer, destBeanCreator, propertyDescriptorFactory);
        fieldMap.setSrcField(aField);
        fieldMap.setDestField(bField);
        if (type != null) {
            fieldMap.setType(MappingDirection.valueOf(type.value()));
        }

        return fieldMap;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("a", a)
            .append("b", b)
            .append("type", type)
            .toString();
    }
}
