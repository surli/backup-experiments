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

import org.apache.commons.lang3.StringUtils;
import org.dozer.builder.model.jaxb.FieldDefinition;
import org.dozer.builder.model.jaxb.FieldExcludeDefinition;
import org.dozer.builder.model.jaxb.MappingDefinition;
import org.dozer.classmap.ClassMap;
import org.dozer.config.BeanContainer;
import org.dozer.el.ELEngine;
import org.dozer.factory.DestBeanCreator;
import org.dozer.fieldmap.FieldMap;
import org.dozer.propertydescriptor.PropertyDescriptorFactory;

public class ELFieldDefinition extends FieldDefinition {

    private final ELEngine elEngine;

    public ELFieldDefinition(ELEngine elEngine, FieldDefinition copy) {
        this(elEngine, null, null);

        if (copy != null) {
            this.a = copy.getA();
            this.b = copy.getB();
            this.aHint = copy.getAHint();
            this.bHint = copy.getBHint();
            this.aDeepIndexHint = copy.getADeepIndexHint();
            this.bDeepIndexHint = copy.getBDeepIndexHint();
            this.relationshipType = copy.getRelationshipType();
            this.removeOrphans = copy.getRemoveOrphans();
            this.type = copy.getType();
            this.mapId = copy.getMapId();
            this.copyByReference = copy.getCopyByReference();
            this.customConverter = copy.getCustomConverter();
            this.customConverterId = copy.getCustomConverterId();
            this.customConverterParam = copy.getCustomConverterParam();
        }
    }

    public ELFieldDefinition(ELEngine elEngine, MappingDefinition parent, FieldExcludeDefinition fieldExcludeParent) {
        super(parent, fieldExcludeParent);

        this.elEngine = elEngine;
    }

    @Override
    public FieldMap build(ClassMap classMap, BeanContainer beanContainer, DestBeanCreator destBeanCreator, PropertyDescriptorFactory propertyDescriptorFactory) {
        if (!StringUtils.isBlank(customConverter)) {
            setCustomConverter(elEngine.resolve(customConverter));
        }

        if (!StringUtils.isBlank(aHint)) {
            setAHint(elEngine.resolve(aHint));
        }

        if (!StringUtils.isBlank(bHint)) {
            setBHint(elEngine.resolve(bHint));
        }

        if (!StringUtils.isBlank(aDeepIndexHint)) {
            setADeepIndexHint(elEngine.resolve(aDeepIndexHint));
        }

        if (!StringUtils.isBlank(bDeepIndexHint)) {
            setBDeepIndexHint(elEngine.resolve(bDeepIndexHint));
        }

        return super.build(classMap, beanContainer, destBeanCreator, propertyDescriptorFactory);
    }
}
