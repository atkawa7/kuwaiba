/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.customization.classhierarchy.nodes.properties;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.core.caching.Cache;
import org.inventory.communications.util.Constants;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;
import org.openide.nodes.PropertySupport.ReadWrite;

/**
 * ClassMetadata properties
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class ClassMetadataProperty extends ReadWrite {

    private Object value;
    private ClassMetadataNode node;

    public ClassMetadataProperty(String name, Class valueType, Object value,
            String displayName,String toolTextTip, ClassMetadataNode node){
        super(name, valueType, displayName, toolTextTip);
        this.value = value;
        this.node = node;
    }
   
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (t == value)
            return;
        try{
            if(!CommunicationsStub.getInstance().setClassMetadataProperties(node.getClassMetadata().getOid(), 
                    (getName().equals(Constants.PROPERTY_NAME)) ? (String)t :  null, 
                    (getName().equals(Constants.PROPERTY_DISPLAYNAME)) ? (String)t :  null,
                    (getName().equals(Constants.PROPERTY_DESCRIPTION)) ? (String)t :  null, 
                    (getName().equals(Constants.PROPERTY_SMALLICON)) ? (byte[])t :  null, 
                    (getName().equals(Constants.PROPERTY_ICON)) ? (byte[])t :  null, 
                    (getName().equals(Constants.PROPERTY_COLOR)) ? (Integer)((Color)t).getRGB() : -1,
                    (getName().equals(Constants.PROPERTY_ABSTRACT)) ? (Boolean)t :  null, 
                    (getName().equals(Constants.PROPERTY_INDESIGN)) ? (Boolean)t :  null, 
                    (getName().equals(Constants.PROPERTY_COUNTABLE)) ? (Boolean)t :  null, 
                    (getName().equals(Constants.PROPERTY_CUSTOM)) ? (Boolean)t :  null))
                throw new Exception(CommunicationsStub.getInstance().getError());                      

            value = t;
            
            if (this.getName().equals(Constants.PROPERTY_NAME)){
                node.getClassMetadata().setClassName((String)value);
                node.propertyChange(new PropertyChangeEvent(this, Constants.PROPERTY_NAME, null, value));
            }
            
            Cache.getInstace().resetAll();
            
        }catch(Exception e){
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, e.getMessage());
        }
    }

    @Override
    public PropertyEditor getPropertyEditor(){
        if(getName().equals(Constants.PROPERTY_ICON))
            return new IconPropertyEditor(node.getClassMetadata().getOid(), Constants.PROPERTY_ICON);
        if(getName().equals(Constants.PROPERTY_SMALLICON))
            return new IconPropertyEditor(node.getClassMetadata().getOid(), Constants.PROPERTY_SMALLICON);
        else
            return super.getPropertyEditor();
    }
    
    @Override
    public boolean canWrite(){
        //Dates are read only  by now until we integrate a date picker
        return !getValueType().equals(Date.class);
    }
    
    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}