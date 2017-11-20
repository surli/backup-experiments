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

import org.dozer.builder.model.jaxb.ConfigurationDefinition;
import org.dozer.builder.model.jaxb.MappingDefinition;
import org.dozer.builder.model.jaxb.MappingsDefinition;
import org.dozer.el.ELEngine;

public class ELMappingsDefinition extends MappingsDefinition {

    private final ELEngine elEngine;

    public ELMappingsDefinition(ELEngine elEngine) {
        this(elEngine, null);
    }

    public ELMappingsDefinition(ELEngine elEngine, MappingsDefinition copy) {
        this.elEngine = elEngine;

        if (copy != null) {
            if (copy.getConfiguration() != null) {
                this.configuration = new ELConfigurationDefinition(elEngine, copy.getConfiguration());
            }

            if (copy.getMapping() != null && copy.getMapping().size() > 0) {
                this.mapping = copy.getMapping()
                    .stream()
                    .map(m -> new ELMappingDefinition(elEngine, m))
                    .collect(Collectors.toList());
            }
        }
    }

    @Override
    public ConfigurationDefinition withConfiguration() {
        if (this.configuration == null) {
            setConfiguration(new ELConfigurationDefinition(this.elEngine, this));
        }

        return this.configuration;
    }

    @Override
    public MappingDefinition addMapping() {
        if (this.mapping == null) {
            setMapping(new ArrayList<>());
        }

        ELMappingDefinition mapping = new ELMappingDefinition(this.elEngine, this);
        getMapping().add(mapping);

        return mapping;
    }

}
