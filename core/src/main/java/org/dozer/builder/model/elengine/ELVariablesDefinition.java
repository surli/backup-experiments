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
import org.dozer.builder.model.jaxb.VariableDefinition;
import org.dozer.builder.model.jaxb.VariablesDefinition;
import org.dozer.el.ELEngine;

public class ELVariablesDefinition extends VariablesDefinition {

    private final ELEngine elEngine;

    public ELVariablesDefinition(ELEngine elEngine, VariablesDefinition copy) {
        this(elEngine, (ConfigurationDefinition)null);

        if (copy != null) {
            if (copy.getVariable() != null && copy.getVariable().size() > 0) {
                this.variables = copy.getVariable()
                    .stream()
                    .map(v -> new ELVariableDefinition(elEngine, v))
                    .collect(Collectors.toList());
            }
        }
    }

    public ELVariablesDefinition(ELEngine elEngine, ConfigurationDefinition parent) {
        super(parent);

        this.elEngine = elEngine;
    }

    @Override
    public VariableDefinition withVariable() {
        if (getVariable() == null) {
            setVariable(new ArrayList<VariableDefinition>());
        }

        ELVariableDefinition variable = new ELVariableDefinition(elEngine, this);
        getVariable().add(variable);

        return variable;
    }
}

