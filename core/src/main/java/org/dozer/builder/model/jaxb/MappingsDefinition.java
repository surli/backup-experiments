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
import org.dozer.classmap.ClassMap;
import org.dozer.classmap.Configuration;
import org.dozer.config.BeanContainer;
import org.dozer.factory.DestBeanCreator;
import org.dozer.propertydescriptor.PropertyDescriptorFactory;

/**
 * The document root.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "mappings")
public class MappingsDefinition {

    @XmlTransient
    private String schemaLocation;

    @XmlElement(name = "configuration")
    protected ConfigurationDefinition configuration;

    @XmlElement(name = "mapping")
    protected List<MappingDefinition> mapping;

    public MappingsDefinition() {

    }

    public ConfigurationDefinition getConfiguration() {
        return configuration;
    }

    public List<MappingDefinition> getMapping() {
        return mapping;
    }

    protected void setConfiguration(ConfigurationDefinition configuration) {
        this.configuration = configuration;
    }

    protected void setMapping(List<MappingDefinition> mapping) {
        this.mapping = mapping;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public ConfigurationDefinition withConfiguration() {
        if (this.configuration == null) {
            setConfiguration(new ConfigurationDefinition(this));
        }

        return configuration;
    }

    public MappingDefinition addMapping() {
        if (this.mapping == null) {
            setMapping(new ArrayList<>());
        }

        MappingDefinition mapping = new MappingDefinition(this);
        getMapping().add(mapping);

        return mapping;
    }

    public List<ClassMap> build(Configuration configuration, BeanContainer beanContainer, DestBeanCreator destBeanCreator, PropertyDescriptorFactory propertyDescriptorFactory) {
        List<ClassMap> answer = new ArrayList<>();

        if (mapping != null) {
            for (MappingDefinition current : mapping) {
                answer.add(current.build(configuration, beanContainer, destBeanCreator, propertyDescriptorFactory));
            }
        }

        return answer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("configuration", configuration)
            .append("mapping", mapping)
            .toString();
    }
}
