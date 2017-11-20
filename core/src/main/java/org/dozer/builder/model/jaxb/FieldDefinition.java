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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dozer.MappingException;
import org.dozer.classmap.ClassMap;
import org.dozer.classmap.MappingDirection;
import org.dozer.classmap.RelationshipType;
import org.dozer.config.BeanContainer;
import org.dozer.factory.DestBeanCreator;
import org.dozer.fieldmap.CustomGetSetMethodFieldMap;
import org.dozer.fieldmap.DozerField;
import org.dozer.fieldmap.FieldMap;
import org.dozer.fieldmap.GenericFieldMap;
import org.dozer.fieldmap.HintContainer;
import org.dozer.fieldmap.MapFieldMap;
import org.dozer.propertydescriptor.PropertyDescriptorFactory;

/**
 * Specifies a custom field mapping. Fields that share the same attribute name do not need to be defined.
 * Dozer automatically maps fields that match on attribute name. All Field Mapping definitions are bi-directional
 * by default.
 * Global configuration, Mapping, and ClassDefinition element values are inherited.
 * <p>
 * Hints are used for mapping Collection types. A hint indicates which type of destination object should be created
 * and added to the
 * destination Collection.
 * <p>
 * Required Attributes:
 * <p>
 * Optional Attributes:
 * <p>
 * relationship-type For collections, indicates whether to add to existing values or to always replace any existing
 * entries
 * <p>
 * remove-orphans For collections, indicates whether items that did not exist in the source collection should be
 * removed from the destination collection.
 * <p>
 * type Indicates whether this mapping is bi-directional or only one-way. Typically this will be set to bi-directional. The default is "bi-directional".
 * <p>
 * map-id The id that uniquely identifies this mapping definition. This typically will not be specified.
 * You would only need to specify this for only need this for special context based mapping
 * and when mapping between Map objects and Custom Data Objects.
 * <p>
 * copy-by-reference Indicates whether the source field value is copied by reference or by value when populating the destination field. The default value is "false"
 * <p>
 * custom-converter Indicates that a specific custom converter should be used for mapping this field. Typically
 * this will not be specified.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "field")
public class FieldDefinition {

    @XmlTransient
    private final MappingDefinition parentMappingDefinition;

    @XmlTransient
    private final FieldExcludeDefinition parentFieldExclude;

    @XmlElement(required = true)
    protected FieldDefinitionDefinition a;

    @XmlElement(required = true)
    protected FieldDefinitionDefinition b;

    @XmlElement(name = "a-hint")
    protected String aHint;

    @XmlElement(name = "b-hint")
    protected String bHint;

    @XmlElement(name = "a-deep-index-hint")
    protected String aDeepIndexHint;

    @XmlElement(name = "b-deep-index-hint")
    protected String bDeepIndexHint;

    @XmlAttribute(name = "relationship-type")
    protected Relationship relationshipType;

    @XmlAttribute(name = "remove-orphans")
    protected Boolean removeOrphans;

    @XmlAttribute(name = "type")
    protected Type type;

    @XmlAttribute(name = "map-id")
    protected String mapId;

    @XmlAttribute(name = "copy-by-reference")
    protected Boolean copyByReference;

    @XmlAttribute(name = "custom-converter")
    protected String customConverter;

    @XmlAttribute(name = "custom-converter-id")
    protected String customConverterId;

    @XmlAttribute(name = "custom-converter-param")
    protected String customConverterParam;

    public FieldDefinition() {
        this(null, null);
    }

    public FieldDefinition(MappingDefinition parentMappingDefinition, FieldExcludeDefinition parentFieldExclude) {
        this.parentMappingDefinition = parentMappingDefinition;
        this.parentFieldExclude = parentFieldExclude;

    }

    public FieldDefinitionDefinition getA() {
        return a;
    }

    public FieldDefinitionDefinition getB() {
        return b;
    }

    public String getAHint() {
        return aHint;
    }

    public String getBHint() {
        return bHint;
    }

    public String getADeepIndexHint() {
        return aDeepIndexHint;
    }

    public String getBDeepIndexHint() {
        return bDeepIndexHint;
    }

    public Relationship getRelationshipType() {
        return relationshipType;
    }

    public Boolean getRemoveOrphans() {
        return removeOrphans;
    }

    public Type getType() {
        return type;
    }

    public String getMapId() {
        return mapId;
    }

    public Boolean getCopyByReference() {
        return copyByReference;
    }

    public String getCustomConverter() {
        return customConverter;
    }

    public String getCustomConverterId() {
        return customConverterId;
    }

    public String getCustomConverterParam() {
        return customConverterParam;
    }

    private void setA(FieldDefinitionDefinition a) {
        this.a = a;
    }

    private void setB(FieldDefinitionDefinition b) {
        this.b = b;
    }

    protected void setAHint(String aHint) {
        this.aHint = aHint;
    }

    protected void setBHint(String bHint) {
        this.bHint = bHint;
    }

    protected void setADeepIndexHint(String aDeepIndexHint) {
        this.aDeepIndexHint = aDeepIndexHint;
    }

    protected void setBDeepIndexHint(String bDeepIndexHint) {
        this.bDeepIndexHint = bDeepIndexHint;
    }

    private void setRelationshipType(Relationship relationshipType) {
        this.relationshipType = relationshipType;
    }

    private void setRemoveOrphans(Boolean removeOrphans) {
        this.removeOrphans = removeOrphans;
    }

    private void setType(Type type) {
        this.type = type;
    }

    private void setMapId(String mapId) {
        this.mapId = mapId;
    }

    private void setCopyByReference(Boolean copyByReference) {
        this.copyByReference = copyByReference;
    }

    protected void setCustomConverter(String customConverter) {
        this.customConverter = customConverter;
    }

    private void setCustomConverterId(String customConverterId) {
        this.customConverterId = customConverterId;
    }

    private void setCustomConverterParam(String customConverterParam) {
        this.customConverterParam = customConverterParam;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public FieldDefinitionDefinition withA() {
        FieldDefinitionDefinition a = new FieldDefinitionDefinition(parentFieldExclude, this);
        setA(a);

        return a;
    }

    public FieldDefinitionDefinition withB() {
        FieldDefinitionDefinition b = new FieldDefinitionDefinition(parentFieldExclude, this);
        setB(b);

        return b;
    }

    public FieldDefinition withAHint(String aHint) {
        setAHint(aHint);

        return this;
    }

    public FieldDefinition withBHint(String bHint) {
        setBHint(bHint);

        return this;
    }

    public FieldDefinition withADeepIndexHint(String aDeepIndexHint) {
        setADeepIndexHint(aDeepIndexHint);

        return this;
    }

    public FieldDefinition withBDeepIndexHint(String bDeepIndexHint) {
        setBDeepIndexHint(bDeepIndexHint);

        return this;
    }

    public FieldDefinition withRelationshipType(Relationship relationshipType) {
        setRelationshipType(relationshipType);

        return this;
    }

    public FieldDefinition withRemoveOrphans(Boolean removeOrphans) {
        setRemoveOrphans(removeOrphans);

        return this;
    }

    public FieldDefinition withType(Type type) {
        setType(type);

        return this;
    }

    public FieldDefinition withMapId(String mapId) {
        setMapId(mapId);

        return this;
    }

    public FieldDefinition withCopyByReference(Boolean copyByReference) {
        setCopyByReference(copyByReference);

        return this;
    }

    public FieldDefinition withCustomConverter(String customConverter) {
        setCustomConverter(customConverter);

        return this;
    }

    public FieldDefinition withCustomConverterId(String customConverterId) {
        setCustomConverterId(customConverterId);

        return this;
    }

    public FieldDefinition withCustomConverterParam(String customConverterParam) {
        setCustomConverterParam(customConverterParam);

        return this;
    }

    public FieldExcludeDefinition endFieldExclude() {
        return parentFieldExclude;
    }

    public MappingDefinition end() {
        return parentMappingDefinition;
    }

    public FieldMap build(ClassMap classMap, BeanContainer beanContainer, DestBeanCreator destBeanCreator, PropertyDescriptorFactory propertyDescriptorFactory) {
        if (a == null || b == null) {
            throw new MappingException("Field name can not be empty");
        }

        DozerField aField = a.convert();
        DozerField bField = b.convert();

        FieldMap fieldMap = resolveFieldMapType(classMap, aField, bField, beanContainer, destBeanCreator, propertyDescriptorFactory);
        fieldMap.setSrcField(aField);
        fieldMap.setDestField(bField);
        if (type != null) {
            fieldMap.setType(MappingDirection.valueOf(type.value()));
        }

        if (relationshipType != null) {
            fieldMap.setRelationshipType(RelationshipType.valueOf(relationshipType.value()));
        }

        fieldMap.setRemoveOrphans(removeOrphans == null ? false : removeOrphans);

        HintContainer aHintContainer = getHintContainer(aHint, beanContainer);
        if (aHintContainer != null) {
            fieldMap.setSrcHintContainer(aHintContainer);
        }

        HintContainer bHintContainer = getHintContainer(bHint, beanContainer);
        if (bHintContainer != null) {
            fieldMap.setDestHintContainer(bHintContainer);
        }

        HintContainer aDeepHintContainer = getHintContainer(aDeepIndexHint, beanContainer);
        if (aDeepHintContainer != null) {
            fieldMap.setSrcDeepIndexHintContainer(aDeepHintContainer);
        }

        HintContainer bDeepHintContainer = getHintContainer(bDeepIndexHint, beanContainer);
        if (bDeepHintContainer != null) {
            fieldMap.setDestDeepIndexHintContainer(bDeepHintContainer);
        }

        if (copyByReference != null) {
            fieldMap.setCopyByReference(copyByReference == null ? null : copyByReference);
        }

        fieldMap.setMapId(mapId);
        fieldMap.setCustomConverter(customConverter);
        fieldMap.setCustomConverterId(customConverterId);
        fieldMap.setCustomConverterParam(customConverterParam);

        return fieldMap;
    }

    private FieldMap resolveFieldMapType(ClassMap classMap, DozerField aField, DozerField bField, BeanContainer beanContainer, DestBeanCreator destBeanCreator,
                                         PropertyDescriptorFactory propertyDescriptorFactory) {
        FieldMap answer;
        if (aField.isMapTypeCustomGetterSetterField()
            || bField.isMapTypeCustomGetterSetterField()
            || classMap.isSrcClassMapTypeCustomGetterSetter()
            || classMap.isDestClassMapTypeCustomGetterSetter()) {
            answer = new MapFieldMap(classMap, beanContainer, destBeanCreator, propertyDescriptorFactory);
        } else if (aField.isCustomGetterSetterField() || bField.isCustomGetterSetterField()) {
            answer = new CustomGetSetMethodFieldMap(classMap, beanContainer, destBeanCreator, propertyDescriptorFactory);
        } else {
            answer = new GenericFieldMap(classMap, beanContainer, destBeanCreator, propertyDescriptorFactory);
        }

        return answer;
    }

    private HintContainer getHintContainer(String hint, BeanContainer beanContainer) {
        HintContainer hintContainer = null;
        if (!StringUtils.isEmpty(hint)) {
            hintContainer = new HintContainer(beanContainer);
            hintContainer.setHintName(hint);
        }

        return hintContainer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("a", a)
            .append("b", b)
            .append("aHint", aHint)
            .append("bHint", bHint)
            .append("aDeepIndexHint", aDeepIndexHint)
            .append("bDeepIndexHint", bDeepIndexHint)
            .append("relationshipType", relationshipType)
            .append("removeOrphans", removeOrphans)
            .append("type", type)
            .append("mapId", mapId)
            .append("copyByReference", copyByReference)
            .append("customConverter", customConverter)
            .append("customConverterId", customConverterId)
            .append("customConverterParam", customConverterParam)
            .append("AHint", aHint)
            .append("BHint", bHint)
            .append("ADeepIndexHint", aDeepIndexHint)
            .append("BDeepIndexHint", bDeepIndexHint)
            .toString();
    }
}
