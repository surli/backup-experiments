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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dozer.config.BeanContainer;
import org.dozer.converters.CustomConverterDescription;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "custom-converters")
public class CustomConvertersDefinition {

    @XmlTransient
    private final ConfigurationDefinition parent;

    @XmlElement(name = "converter", required = true)
    protected List<ConverterTypeDefinition> converter;

    public CustomConvertersDefinition() {
        this(null);
    }

    public CustomConvertersDefinition(ConfigurationDefinition parent) {
        this.parent = parent;
    }

    public List<ConverterTypeDefinition> getConverter() {
        return converter;
    }

    protected void setConverter(List<ConverterTypeDefinition> converter) {
        this.converter = converter;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public ConverterTypeDefinition withConverter() {
        if (getConverter() == null) {
            setConverter(new ArrayList<ConverterTypeDefinition>());
        }

        ConverterTypeDefinition converter = new ConverterTypeDefinition(this);
        getConverter().add(converter);

        return converter;
    }

    public List<CustomConverterDescription> build(BeanContainer beanContainer) {
        List<CustomConverterDescription> answer = new ArrayList<CustomConverterDescription>();
        if (converter != null && converter.size() > 0) {
            for (ConverterTypeDefinition current : converter) {
                answer.add(current.build(beanContainer));
            }
        }

        return answer;
    }

    public ConfigurationDefinition end() {
        return parent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("converter", converter)
            .toString();
    }
}
