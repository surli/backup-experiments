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
import org.dozer.classmap.CopyByReference;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "copy-by-references")
public class CopyByReferencesDefinition {

    @XmlTransient
    private final ConfigurationDefinition parent;

    @XmlElement(name = "copy-by-reference", required = true)
    protected List<String> copyByReference;

    public CopyByReferencesDefinition() {
        this(null);
    }

    public CopyByReferencesDefinition(ConfigurationDefinition parent) {
        this.parent = parent;
    }

    public List<String> getCopyByReference() {
        return copyByReference;
    }

    protected void setCopyByReference(List<String> copyByReference) {
        this.copyByReference = copyByReference;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public CopyByReferencesDefinition addCopyByReference(String copyByReference) {
        if (getCopyByReference() == null) {
            setCopyByReference(new ArrayList<String>());
        }

        getCopyByReference().add(copyByReference);

        return this;
    }

    public List<CopyByReference> build() {
        List<CopyByReference> answer = new ArrayList<CopyByReference>();
        for (String current : copyByReference) {
            answer.add(new CopyByReference(current));
        }

        return answer;
    }

    public ConfigurationDefinition end() {
        return parent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("copyByReference", copyByReference)
            .toString();
    }
}
