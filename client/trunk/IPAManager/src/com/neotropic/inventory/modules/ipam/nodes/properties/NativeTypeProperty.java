/*
 *  Copyright 2010-2016, Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.inventory.modules.ipam.nodes.properties;

import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.PropertySupport.ReadWrite;

/**
 * Provides a valid representation of LocalObjects attributes as Properties,
 * as LocalObject is just a proxy and can't be a bean itself
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NativeTypeProperty extends ReadWrite {
    private SubnetPoolNode node;
    private Object value;

    /**
     * This constructor is called when the type is anything but a list
     */
    public NativeTypeProperty(String name, Class valueType, String displayName,
            String toolTextTip, SubnetPoolNode node, Object value) {
        super(name, valueType, displayName, toolTextTip);
        //this.setName(name);
        this.node = node;
        this.value = value;
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {        
       if (value == null && getValueType() == Boolean.class)
           return false;
       return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        LocalObject update = new LocalObject(node.getSubnetPool().getClassName(), node.getSubnetPool().getOid(), 
                new String[]{this.getName()}, new Object[]{t});

        if(!CommunicationsStub.getInstance().saveObject(update))
            NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            value = t;
            if (getName().equals(Constants.PROPERTY_NAME))
                node.getSubnetPool().setName((String)t);
        }
    }

    @Override
    public PropertyEditor getPropertyEditor(){        
        if (this.getValueType() ==  Date.class)
            return java.beans.PropertyEditorManager.findEditor(String.class);
        return super.getPropertyEditor();
    }

    @Override
    public boolean canWrite(){
        //Dates are read only for now until we integrate a date picker
        return !getValueType().equals(Date.class);
            
    }
}