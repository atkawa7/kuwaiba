/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>
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
import org.openide.nodes.PropertySupport;

/**
 * A property editor that is a combo box
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 * @param <T>The kind of objects in the drop down list
 */
public abstract class AbstractComboBoxProperty<T> extends PropertySupport.ReadWrite<T> {
    protected T[] tags;
    public AbstractComboBoxProperty(T[] tags, String name, Class<T> type, 
                                String displayName, String shortDescription) {
        super(name, type, displayName, shortDescription);
        this.tags = tags;
    }
    
    @Override
    public PropertyEditor getPropertyEditor(){        
        return new ComboBoxPropertyEditorSupport<>(tags);
    }
}
