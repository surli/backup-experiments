/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2016 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.ast.nodeTypes;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.Optional;

public interface NodeWithParameters<N extends Node> {
    NodeList<Parameter> getParameters();

    default Parameter getParameter(int i) {
        return getParameters().get(i);
    }

    @SuppressWarnings("unchecked")
    default N setParameter(int i, Parameter parameter) {
        getParameters().set(i, parameter);
        return (N) this;
    }

    N setParameters(NodeList<Parameter> parameters);

    default N addParameter(Type type, String name) {
        return addParameter(new Parameter(type, name));
    }

    default N addParameter(Class<?> paramClass, String name) {
        ((Node) this).tryAddImportToParentCompilationUnit(paramClass);
        return addParameter(new ClassOrInterfaceType(paramClass.getSimpleName()), name);
    }

    /**
     * Remember to import the class in the compilation unit yourself
     *
     * @param className the name of the class, ex : org.test.Foo or Foo if you added manually the import
     * @param name the name of the parameter
     */
    default N addParameter(String className, String name) {
        return addParameter(new ClassOrInterfaceType(className), name);
    }

    @SuppressWarnings("unchecked")
    default N addParameter(Parameter parameter) {
        getParameters().add(parameter);
        return (N) this;
    }

    default Parameter addAndGetParameter(Type type, String name) {
        return addAndGetParameter(new Parameter(type, name));
    }

    default Parameter addAndGetParameter(Class<?> paramClass, String name) {
        ((Node) this).tryAddImportToParentCompilationUnit(paramClass);
        return addAndGetParameter(new ClassOrInterfaceType(paramClass.getSimpleName()), name);
    }

    /**
     * Remember to import the class in the compilation unit yourself
     *
     * @param className the name of the class, ex : org.test.Foo or Foo if you added manually the import
     * @param name the name of the parameter
     * @return the {@link Parameter} created
     */
    default Parameter addAndGetParameter(String className, String name) {
        return addAndGetParameter(new ClassOrInterfaceType(className), name);
    }

    default Parameter addAndGetParameter(Parameter parameter) {
        getParameters().add(parameter);
        return parameter;
    }

    /**
     * Try to find a {@link Parameter} by its name
     *
     * @param name the name of the param
     * @return null if not found, the param found otherwise
     */
    default Optional<Parameter> getParameterByName(String name) {
        return getParameters().stream()
                .filter(p -> p.getNameAsString().equals(name)).findFirst();
    }

    /**
     * Try to find a {@link Parameter} by its type
     *
     * @param type the type of the param
     * @return null if not found, the param found otherwise
     */
    default Optional<Parameter> getParameterByType(String type) {
        return getParameters().stream()
                .filter(p -> p.getType().toString().equals(type)).findFirst();
    }

    /**
     * Try to find a {@link Parameter} by its type
     *
     * @param type the type of the param <b>take care about generics, it wont work</b>
     * @return null if not found, the param found otherwise
     */
    default Optional<Parameter> getParameterByType(Class<?> type) {
        return getParameters().stream()
                .filter(p -> p.getType().toString().equals(type.getSimpleName())).findFirst();
    }
}
