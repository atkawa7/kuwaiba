/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.core.templates.nodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.navigation.applicationnodes.objectnodes.properties.ItemListPropertyEditor;
import org.openide.nodes.PropertySupport;

/**
 * A dedicated property class for list type attributes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ListTypeProperty extends PropertySupport.ReadWrite<LocalObjectListItem> {
    private PropertyEditor propertyEditor;
    private LocalObject businessObject;

    public ListTypeProperty(String name, String displayName, String shortDescription, 
            List<LocalObjectListItem> list, LocalObject businessObject) {
        super(name, LocalObjectListItem.class, displayName, shortDescription);
        this.propertyEditor = new ItemListPropertyEditor(list);
        this.businessObject = businessObject;
    }

    @Override
    public LocalObjectListItem getValue() throws IllegalAccessException, InvocationTargetException {        
       return (LocalObjectListItem)businessObject.getAttribute(getName());
    }

    @Override
    public void setValue(LocalObjectListItem value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        businessObject.setAttribute(getName(), value); 
    }

    @Override
    public PropertyEditor getPropertyEditor(){
        return propertyEditor;
    }
}