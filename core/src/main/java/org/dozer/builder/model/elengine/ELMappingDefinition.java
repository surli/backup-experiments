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
package org.dozer.builder.model.elengine;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dozer.builder.model.jaxb.FieldDefinition;
import org.dozer.builder.model.jaxb.MappingDefinition;
import org.dozer.builder.model.jaxb.MappingsDefinition;
import org.dozer.classmap.ClassMap;
import org.dozer.classmap.Configuration;
import org.dozer.config.BeanContainer;
import org.dozer.el.ELEngine;
import org.dozer.factory.DestBeanCreator;
import org.dozer.propertydescriptor.PropertyDescriptorFactory;

public class ELMappingDefinition extends MappingDefinition {

    private final ELEngine elEngine;

    public ELMappingDefinition(ELEngine elEngine, MappingDefinition copy) {
        this(elEngine, (MappingsDefinition)null);

        if (copy != null) {
            this.classA = new ELClassDefinition(elEngine, copy.getClassA());
            this.classB = new ELClassDefinition(elEngine, copy.getClassB());

            if (copy.getFieldOrFieldExclude() != null && copy.getFieldOrFieldExclude().size() > 0) {
                this.fieldOrFieldExclude = copy.getFieldOrFieldExclude()
                        .stream()
                        .map(f -> f instanceof FieldDefinition
                                ? new ELFieldDefinition(elEngine, (FieldDefinition)f)
                                : f)
                        .collect(Collectors.toList());
            }

            this.dateFormat = copy.getDateFormat();
            this.stopOnErrors = copy.getStopOnErrors();
            this.wildcard = copy.getWildcard();
            this.trimStrings = copy.getTrimStrings();
            this.mapNull = copy.getMapNull();
            this.mapEmptyString = copy.getMapEmptyString();
            this.beanFactory = copy.getBeanFactory();
            this.type = copy.getType();
            this.relationshipType = copy.getRelationshipType();
            this.mapId = copy.getMapId();
        }
    }

    public ELMappingDefinition(ELEngine elEngine, MappingsDefinition parent) {
        super(parent);

        this.elEngine = elEngine;
    }

    @Override
    public FieldDefinition withField() {
        if (getFields() == null) {
            setFields(new ArrayList<FieldDefinition>());
        }

        ELFieldDefinition field = new ELFieldDefinition(elEngine, this, null);
        getFields().add(field);

        return field;
    }

    @Override
    public ClassMap build(Configuration configuration, BeanContainer beanContainer, DestBeanCreator destBeanCreator, PropertyDescriptorFactory propertyDescriptorFactory) {
        if (!StringUtils.isBlank(beanFactory)) {
            setBeanFactory(elEngine.resolve(beanFactory));
        }

        return super.build(configuration, beanContainer, destBeanCreator, propertyDescriptorFactory);
    }
}
