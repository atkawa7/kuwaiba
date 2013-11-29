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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.Constants;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.AttributeMetadataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Provides a property editor
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class AttributeMetadataProperty extends PropertySupport.ReadWrite {

    private Object value;
    private AttributeMetadataNode node;
    private long classId;

    public AttributeMetadataProperty(String name, Object value,
            AttributeMetadataNode node, long classId) {
        super(name,value.getClass(),name,name);
        this.value = value;
        this.node = node;
        this.classId = classId;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        if (this.value == LocalObjectLight.class) //It's a list type
            return node.getObject().getListAttributeClassName();
        else
            return this.value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        CommunicationsStub com = CommunicationsStub.getInstance();

        if(com.setAttributeProperties(classId, node.getObject().getId(), getName().equals(Constants.PROPERTY_NAME) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_DISPLAYNAME) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_TYPE) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_DESCRIPTION) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_ADMINISTRATIVE) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_VISIBLE) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_READONLY) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_NOCOPY) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_UNIQUE) ? (Boolean)t : null)){
            this.value = t;
            //Refresh the cache
            com.getMetaForClass(classId, true);
            nu.showStatusMessage("Attribute updated successfully", true);
        }else
            nu.showSimplePopup("Attribute Property Update", NotificationUtil.ERROR, com.getError());
    }
    
    @Override
    public PropertyEditor getPropertyEditor(){
        if (getName().equals(Constants.PROPERTY_TYPE)){
            if (this.value == LocalObjectLight.class) //It's a list type
                return new ListAttributeMetadataProperty(node.getObject().getListAttributeClassName());
            else
                return new ListAttributeMetadataProperty(((Class)this.value).getName());
        }
        else
            return super.getPropertyEditor();
    }

    @Override
    public boolean canWrite(){
        return true;
    }
}