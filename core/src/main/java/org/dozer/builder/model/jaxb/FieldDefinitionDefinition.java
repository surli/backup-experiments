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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dozer.MappingException;
import org.dozer.fieldmap.DozerField;
import org.dozer.util.MappingUtils;

/**
 * Specifies one of the fields in the field mapping definition. Global configuration, mapping, class, and field
 * element values are inherited
 * <p>
 * Required Attributes:
 * <p>
 * Optional Attributes:
 * <p>
 * date-format The string format of Date fields. This is used for field mapping between Strings and Dates
 * <p>
 * set-method Indicates which set method to invoke when setting the destination value. Typically this will not be specified.
 * By default, the beans attribute setter is used.
 * <p>
 * get-method Indicates which get method to invoke on the src object to get the field value Typically this will not be specified.
 * By default, the beans attribute getter is used.
 * <p>
 * is-accessible Indicates whether Dozer bypasses getter/setter methods and accesses the field directly. This will typically be set to "false". The default value is
 * "false". If set to "true", the
 * getter/setter methods will NOT be invoked. You would want to set this to "true" if the field is lacking a getter or setter method.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldDefinition")
public class FieldDefinitionDefinition {

    @XmlTransient
    private final FieldExcludeDefinition fieldExcludeParent;

    @XmlTransient
    private final FieldDefinition fieldParent;

    @XmlValue
    private String name;

    @XmlAttribute(name = "date-format")
    private String dateFormat;

    @XmlAttribute(name = "type")
    private FieldType type;

    @XmlAttribute(name = "set-method")
    private String setMethod;

    @XmlAttribute(name = "get-method")
    private String getMethod;

    @XmlAttribute(name = "key")
    private String key;

    @XmlAttribute(name = "map-set-method")
    private String mapSetMethod;

    @XmlAttribute(name = "map-get-method")
    private String mapGetMethod;

    @XmlAttribute(name = "is-accessible")
    private Boolean isAccessible;

    @XmlAttribute(name = "create-method")
    private String createMethod;

    public FieldDefinitionDefinition() {
        this(null, null);
    }

    public FieldDefinitionDefinition(FieldExcludeDefinition fieldExcludeParent, FieldDefinition fieldParent) {
        this.fieldExcludeParent = fieldExcludeParent;
        this.fieldParent = fieldParent;
    }

    public String getName() {
        return name;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public FieldType getType() {
        return type;
    }

    public String getSetMethod() {
        return setMethod;
    }

    public String getGetMethod() {
        return getMethod;
    }

    public String getKey() {
        return key;
    }

    public String getMapSetMethod() {
        return mapSetMethod;
    }

    public String getMapGetMethod() {
        return mapGetMethod;
    }

    public Boolean getAccessible() {
        return isAccessible;
    }

    public String getCreateMethod() {
        return createMethod;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    private void setType(FieldType type) {
        this.type = type;
    }

    private void setSetMethod(String setMethod) {
        this.setMethod = setMethod;
    }

    private void setGetMethod(String getMethod) {
        this.getMethod = getMethod;
    }

    private void setKey(String key) {
        this.key = key;
    }

    private void setMapSetMethod(String mapSetMethod) {
        this.mapSetMethod = mapSetMethod;
    }

    private void setMapGetMethod(String mapGetMethod) {
        this.mapGetMethod = mapGetMethod;
    }

    private void setAccessible(Boolean accessible) {
        isAccessible = accessible;
    }

    private void setCreateMethod(String createMethod) {
        this.createMethod = createMethod;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public FieldDefinitionDefinition withName(String name) {
        setName(name);

        return this;
    }

    public FieldDefinitionDefinition withDateFormat(String dateFormat) {
        setDateFormat(dateFormat);

        return this;
    }

    public FieldDefinitionDefinition withType(FieldType type) {
        setType(type);

        return this;
    }

    public FieldDefinitionDefinition withSetMethod(String setMethod) {
        setSetMethod(setMethod);

        return this;
    }

    public FieldDefinitionDefinition withGetMethod(String getMethod) {
        setGetMethod(getMethod);

        return this;
    }

    public FieldDefinitionDefinition withKey(String key) {
        setKey(key);

        return this;
    }

    public FieldDefinitionDefinition withMapSetMethod(String mapSetMethod) {
        setMapSetMethod(mapSetMethod);

        return this;
    }

    public FieldDefinitionDefinition withMapGetMethod(String mapGetMethod) {
        setMapGetMethod(mapGetMethod);

        return this;
    }

    public FieldDefinitionDefinition withAccessible(Boolean accessible) {
        setAccessible(accessible);

        return this;
    }

    public FieldDefinitionDefinition withCreateMethod(String createMethod) {
        setCreateMethod(createMethod);

        return this;
    }

    public FieldExcludeDefinition end() {
        return fieldExcludeParent;
    }

    public FieldDefinition endField() {
        return fieldParent;
    }

    public DozerField convert() {
        DozerField field = prepareField(name, type == null ? "" : type.value());
        field.setDateFormat(dateFormat);
        field.setMapGetMethod(mapGetMethod);
        field.setMapSetMethod(mapSetMethod);
        field.setTheGetMethod(getMethod);
        field.setTheSetMethod(setMethod);

        if (StringUtils.isNotEmpty(key)) {
            field.setKey(key);
        }

        if (isAccessible != null) {
            field.setAccessible(isAccessible);
        }

        field.setCreateMethod(createMethod);

        return field;
    }

    private DozerField prepareField(String name, String type) {
        if (MappingUtils.isBlankOrNull(name)) {
            throw new MappingException("Field name can not be empty");
        }

        String fieldName;
        String fieldType = null;
        if (isIndexed(name)) {
            fieldName = getFieldNameOfIndexedField(name);
        } else {
            fieldName = name;
        }

        if (StringUtils.isNotEmpty(type)) {
            fieldType = type;
        }

        DozerField field = new DozerField(fieldName, fieldType);
        if (isIndexed(name)) {
            field.setIndexed(true);
            field.setIndex(getIndexOfIndexedField(name));
        }

        return field;
    }

    private boolean isIndexed(String fieldName) {
        return (fieldName != null) && (fieldName.matches(".+\\[\\d+\\]"));
    }

    private String getFieldNameOfIndexedField(String fieldName) {
        return fieldName == null ? null : fieldName.replaceAll("\\[\\d+\\]$", "");
    }

    private int getIndexOfIndexedField(String fieldName) {
        return Integer.parseInt(fieldName.replaceAll(".*\\[", "").replaceAll("\\]", ""));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("name", name)
            .append("dateFormat", dateFormat)
            .append("type", type)
            .append("setMethod", setMethod)
            .append("getMethod", getMethod)
            .append("key", key)
            .append("mapSetMethod", mapSetMethod)
            .append("mapGetMethod", mapGetMethod)
            .append("isAccessible", isAccessible)
            .append("createMethod", createMethod)
            .toString();
    }
}
