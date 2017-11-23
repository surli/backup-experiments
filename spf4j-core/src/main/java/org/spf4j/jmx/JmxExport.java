/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
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
package org.spf4j.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark setters and getters of attributes to export via JMX,
 * Any other methods that do not respect get/set/is bean naming conventions will be exported as JMX operation.
 * Any method parameters annotated with JMXExport allows you to provide names and descriptions to your
 * operation parameters.
 * Names are inferred from the method names, but can be customized further with JmxExport.value.
 *
 * Open type mapping is configurable by registering a new mapper with: GlobalMXBeanMapperSupplier.register
 * or modifying the current ones (if supported). The default implementation, Spf4jOpenTypeMapper supports this.
 * attribute description can be added to the annotation.
 *
 * @author zoly
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER })
public @interface JmxExport {
    /**
     * @return - the name of the operation or attribute or parameter.
     */
     String value() default "";
    /**
     * @return - the description of the operation attribute or parameter.
     */
     String description() default "";

     /**
      * Map to openType the types associated to the exported entity. (or not)
      * @return
      */
     boolean mapOpenType() default true;
}
