/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.navigation.applicationnodes.attributemetadatanodes.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.AttributeMetadataNode;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.customeditor.AttributeEditorSupport;
import org.openide.nodes.PropertySupport;

/**
 * Provides a property editor
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ClassAttributeMetadataProperty extends PropertySupport.ReadWrite implements PropertyChangeListener{

    private Object value;
    private AttributeMetadataNode node;
    private long classId;
    
    public ClassAttributeMetadataProperty(String _name, Class _valueType, Object _value,
            String _displayName,String _toolTextTip, AttributeMetadataNode _node, long classId) {
        super(_name,_valueType,_displayName,_toolTextTip);
        setName(_name);
        this.value = _value;
        this.node = _node;
        this.classId = classId;
        this.getPropertyEditor().addPropertyChangeListener(this);
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return this.value;
    }
    
    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
    }
    
    @Override
    public PropertyEditor getPropertyEditor(){
        return new AttributeEditorSupport(node.getObject(), classId);
    }

    @Override
    public boolean canWrite(){
        if (getName().equals("name") || getName().equals("type"))
            return false;
        else
            return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
