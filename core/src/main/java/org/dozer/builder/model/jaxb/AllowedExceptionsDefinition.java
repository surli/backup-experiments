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

import org.dozer.config.BeanContainer;
import org.dozer.util.MappingUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "allowed-exceptions")
public class AllowedExceptionsDefinition {

    @XmlTransient
    private final ConfigurationDefinition parent;

    @XmlElement(name = "exception", required = true)
    protected List<String> exceptions;

    public AllowedExceptionsDefinition() {
        this(null);
    }

    public AllowedExceptionsDefinition(ConfigurationDefinition parent) {
        this.parent = parent;
    }

    public List<String> getException() {
        return exceptions;
    }

    protected void setException(List<String> exceptions) {
        this.exceptions = exceptions;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public AllowedExceptionsDefinition addException(String exception) {
        if (getException() == null) {
            setException(new ArrayList<String>());
        }

        getException().add(exception);

        return this;
    }

    @SuppressWarnings("unchecked")
    public List<Class<RuntimeException>> build(BeanContainer beanContainer) {
        List<Class<RuntimeException>> answer = new ArrayList<Class<RuntimeException>>();
        for (String current : exceptions) {
            Class runtimeException = MappingUtils.loadClass(current, beanContainer);

            if (!RuntimeException.class.isAssignableFrom(runtimeException)) {
                MappingUtils.throwMappingException("allowed-exceptions must extend java.lang.RuntimeException. Found: "
                                                   + runtimeException.getName());
            }

            answer.add(runtimeException);
        }

        return answer;
    }

    public ConfigurationDefinition end() {
        return parent;
    }

    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this)
            .append("exception", exceptions)
            .toString();
    }
}
