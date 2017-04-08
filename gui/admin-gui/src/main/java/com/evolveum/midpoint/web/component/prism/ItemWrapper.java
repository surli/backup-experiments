/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.component.prism;

import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.Revivable;
import com.evolveum.midpoint.util.DebugDumpable;

/**
 * @author lazyman
 */
public interface ItemWrapper<I extends Item, ID extends ItemDefinition> extends Revivable, DebugDumpable {

	QName getName();
	
    String getDisplayName();

    void setDisplayName(String name);

    I getItem();
    
    /**
     * Item definition.
     * The definition defines how the item will be displayed (type, read-only, read-write or
     * not displayed at all). This behavior can be overriden by readonly and visible flags.
     */
    ID getItemDefinition();
    
    /**
     * Read only flag. This is an override of the default behavior given by the definition.
     * If set to TRUE then it overrides the value from the definition.
     */
    boolean isReadonly();

	boolean isEmpty();

    boolean hasChanged();
    
    public List<ValueWrapper> getValues();

    /**
     * Visibility flag. This is NOT an override, it defines whether the item
     * should be displayed or not.
     */
    public boolean isVisible();
    
    /**
     * Used to display the form elements with stripe in every other line.
     */
    public boolean isStripe();
    
    void setStripe(boolean isStripe);
    
    ContainerWrapper getContainer();
    
    public void addValue();

}
