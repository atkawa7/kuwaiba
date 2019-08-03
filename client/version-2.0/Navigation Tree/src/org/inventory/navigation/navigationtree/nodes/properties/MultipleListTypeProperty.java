/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.navigationtree.nodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.PropertySupport;

/**
 * A property to be used in multiple list type attributes (list type attributes that you can choose multiple items from)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class MultipleListTypeProperty extends PropertySupport.ReadWrite<String> {
    private PropertyEditor propertyEditor;
    private List<LocalObjectListItem> value;
    private List<LocalObjectListItem> list;
    private ObjectNode node;

    public MultipleListTypeProperty(String name, String displayName, String toolTextTip, 
            List<LocalObjectListItem> list, ObjectNode node, List<LocalObjectListItem> value) {
        super(name, String.class, displayName, toolTextTip);
        this.value = value == null ? new ArrayList<>() : value;
        this.node = node;
        this.list = list;
    }

    @Override
    public String getValue() throws IllegalAccessException, InvocationTargetException {        
       return "Click the button for details...";
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (propertyEditor == null)
            propertyEditor = new MultipleListTypePropertyEditor(list, value, this);
        return propertyEditor;
    }
    
    public ObjectNode getNode() {
        return node;
    }

    @Override
    public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //Not used since the set is done in the custom editor
    }
}