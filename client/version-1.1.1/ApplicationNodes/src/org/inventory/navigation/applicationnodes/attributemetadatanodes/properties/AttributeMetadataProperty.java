/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.core.caching.Cache;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.AttributeMetadataNode;
import org.openide.nodes.PropertySupport;

/**
 * Provides a property editor
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class AttributeMetadataProperty extends PropertySupport.ReadWrite {

    private Object value;
    private AttributeMetadataNode node;
    private long classId;

    public AttributeMetadataProperty(String name, String displayName, Object value, AttributeMetadataNode node, long classId) {
        super(name,value.getClass(),displayName,name);
        this.value = value;
        this.node = node;
        this.classId = classId;
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        CommunicationsStub com = CommunicationsStub.getInstance();

        if(com.setAttributeProperties(classId, node.getAttributeMetadata().getId(), getName().equals(Constants.PROPERTY_NAME) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_DISPLAYNAME) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_TYPE) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_DESCRIPTION) ? (String)t : null, 
                getName().equals(Constants.PROPERTY_ADMINISTRATIVE) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_VISIBLE) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_READONLY) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_NOCOPY) ? (Boolean)t : null,
                getName().equals(Constants.PROPERTY_UNIQUE) ? (Boolean)t : null)){
            this.value = t;

            //Force a cache reload
            Cache.getInstace().resetAll();
            //Refresh the class node
            node.getClassNode().refresh();
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Attribute updated successfully");
        }else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
    }
    
    @Override
    public PropertyEditor getPropertyEditor(){
        if (getName().equals(Constants.PROPERTY_TYPE))
            return new ListAttributeMetadataProperty((String)this.value);
        else
            return super.getPropertyEditor();
    }

    @Override
    public boolean canWrite(){
        return true;
    }   
}