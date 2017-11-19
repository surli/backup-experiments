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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dozer.classmap.DozerClass;
import org.dozer.config.BeanContainer;

/**
 * Specifies one of the classes in the mapping definition. All Mapping definitions are bi-directional by default.
 * Global configuration and Mapping element values are inherited
 * <p>
 * Required Attributes:
 * <p>
 * Optional Attributes:
 * <p>
 * bean-factory The factory class to create data objects. This typically will not be specified.
 * By default Dozer constructs new instances of data objects by invoking the no-arg constructor
 * <p>
 * factory-bean-id The id passed to the specified bean factory
 * <p>
 * map-set-method For Map backed objects, this indicates which setter method should be used to retrieve field
 * values. This should only be used of Map backed objects.
 * <p>
 * map-get-method For Map backed objects, this indicates which getter method should be used to retrieve field values.
 * This should only be used of Map backed objects.
 * <p>
 * create-method Which method to invoke to create a new instance of the class. This is typically not specified.
 * By default, the no arg constructor(public or private) is used
 * <p>
 * map-null Indicates whether null values are mapped. The default value is "true"
 * <p>
 * map-empty-string Indicates whether empty string values are mapped. The default value is "true"
 * <p>
 * is-accessible Indicates whether Dozer bypasses getter/setter methods and accesses the field directly. This will typically be set to "false". The default value is
 * "false". If set to "true", the getter/setter methods will NOT be invoked. You would want to set this to "true" if the field is lacking a getter or setter method.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "class")
public class ClassDefinition {

    @XmlTransient
    private final MappingDefinition parentMappingDefinition;

    @XmlTransient
    private final ConverterTypeDefinition parentConverterTypeDefinition;

    @XmlValue
    protected String clazz;

    @XmlAttribute(name = "bean-factory")
    protected String beanFactory;

    @XmlAttribute(name = "factory-bean-id")
    protected String factoryBeanId;

    @XmlAttribute(name = "map-set-method")
    protected String mapSetMethod;

    @XmlAttribute(name = "map-get-method")
    protected String mapGetMethod;

    @XmlAttribute(name = "create-method")
    protected String createMethod;

    @XmlAttribute(name = "map-null")
    protected Boolean mapNull;

    @XmlAttribute(name = "map-empty-string")
    protected Boolean mapEmptyString;

    @XmlAttribute(name = "is-accessible")
    protected Boolean isAccessible;

    public ClassDefinition() {
        this(null, null);
    }

    public ClassDefinition(MappingDefinition parentMappingDefinition, ConverterTypeDefinition parentConverterTypeDefinition) {
        this.parentMappingDefinition = parentMappingDefinition;
        this.parentConverterTypeDefinition = parentConverterTypeDefinition;
    }

    public String getClazz() {
        return clazz;
    }

    public String getBeanFactory() {
        return beanFactory;
    }

    public String getFactoryBeanId() {
        return factoryBeanId;
    }

    public String getMapSetMethod() {
        return mapSetMethod;
    }

    public String getMapGetMethod() {
        return mapGetMethod;
    }

    public String getCreateMethod() {
        return createMethod;
    }

    public Boolean getMapNull() {
        return mapNull;
    }

    public Boolean getMapEmptyString() {
        return mapEmptyString;
    }

    public Boolean getAccessible() {
        return isAccessible;
    }

    protected void setClazz(String clazz) {
        this.clazz = clazz;
    }

    protected void setBeanFactory(String beanFactory) {
        this.beanFactory = beanFactory;
    }

    private void setFactoryBeanId(String factoryBeanId) {
        this.factoryBeanId = factoryBeanId;
    }

    private void setMapSetMethod(String mapSetMethod) {
        this.mapSetMethod = mapSetMethod;
    }

    private void setMapGetMethod(String mapGetMethod) {
        this.mapGetMethod = mapGetMethod;
    }

    private void setCreateMethod(String createMethod) {
        this.createMethod = createMethod;
    }

    private void setMapNull(Boolean mapNull) {
        this.mapNull = mapNull;
    }

    private void setMapEmptyString(Boolean mapEmptyString) {
        this.mapEmptyString = mapEmptyString;
    }

    private void setAccessible(Boolean accessible) {
        isAccessible = accessible;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public ClassDefinition withClazz(String clazz) {
        setClazz(clazz);

        return this;
    }

    public ClassDefinition withBeanFactory(String beanFactory) {
        setBeanFactory(beanFactory);

        return this;
    }

    public ClassDefinition withFactoryBeanId(String factoryBeanId) {
        setFactoryBeanId(factoryBeanId);

        return this;
    }

    public ClassDefinition withMapSetMethod(String mapSetMethod) {
        setMapSetMethod(mapSetMethod);

        return this;
    }

    public ClassDefinition withMapGetMethod(String mapGetMethod) {
        setMapGetMethod(mapGetMethod);

        return this;
    }

    public ClassDefinition withCreateMethod(String createMethod) {
        setCreateMethod(createMethod);

        return this;
    }

    public ClassDefinition withMapNull(Boolean mapNull) {
        setMapNull(mapNull);

        return this;
    }

    public ClassDefinition withMapEmptyString(Boolean mapEmptyString) {
        setMapEmptyString(mapEmptyString);

        return this;
    }

    public ClassDefinition withAccessible(Boolean accessible) {
        setAccessible(accessible);

        return this;
    }

    public MappingDefinition end() {
        return parentMappingDefinition;
    }

    public ConverterTypeDefinition endType() {
        return parentConverterTypeDefinition;
    }

    public DozerClass build(BeanContainer beanContainer) {
        DozerClass dozerClass = new DozerClass(beanContainer);
        dozerClass.setName(clazz);
        dozerClass.setBeanFactory(beanFactory);
        dozerClass.setFactoryBeanId(factoryBeanId);
        dozerClass.setMapGetMethod(mapGetMethod);
        dozerClass.setMapSetMethod(mapSetMethod);
        dozerClass.setCreateMethod(createMethod);
        dozerClass.setMapNull(mapNull == null ? null : mapNull);
        dozerClass.setMapEmptyString(mapEmptyString == null ? null : mapEmptyString);
        dozerClass.setAccessible(isAccessible == null ? null : isAccessible);

        return dozerClass;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("clazz", clazz)
            .append("beanFactory", beanFactory)
            .append("factoryBeanId", factoryBeanId)
            .append("mapSetMethod", mapSetMethod)
            .append("mapGetMethod", mapGetMethod)
            .append("createMethod", createMethod)
            .append("mapNull", mapNull)
            .append("mapEmptyString", mapEmptyString)
            .append("isAccessible", isAccessible)
            .toString();
    }
}
