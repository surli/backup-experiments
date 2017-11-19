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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "variable")
public class VariableDefinition {

    @XmlTransient
    private final VariablesDefinition parent;

    @XmlValue
    protected String clazz;

    @XmlAttribute(name = "name", required = true)
    protected String name;

    public VariableDefinition() {
        this(null);
    }

    public VariableDefinition(VariablesDefinition parent) {
        this.parent = parent;
    }

    public String getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    private void setClazz(String clazz) {
        this.clazz = clazz;
    }

    private void setName(String value) {
        this.name = value;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public VariableDefinition withClazz(String clazz) {
        setClazz(clazz);

        return this;
    }

    public VariableDefinition withName(String value) {
        setName(value);

        return this;
    }

    public VariablesDefinition end() {
        return parent;
    }

    public void build() {
        //NOOP
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("clazz", clazz)
            .append("name", name)
            .toString();
    }
}
