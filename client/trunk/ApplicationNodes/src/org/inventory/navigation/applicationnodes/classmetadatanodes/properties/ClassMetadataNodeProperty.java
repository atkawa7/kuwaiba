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
package org.inventory.navigation.applicationnodes.classmetadatanodes.properties;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.caching.Cache;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataNode;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.util.Lookup;

/**
 * ClassMetadata properties
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ClassMetadataNodeProperty extends ReadWrite implements PropertyChangeListener{

    private Object value;
    private ClassMetadataNode node;

    public ClassMetadataNodeProperty(String _name, Class _valueType, Object _value,
            String _displayName,String _toolTextTip, ClassMetadataNode _node){
        super(_name, _valueType, _displayName, _toolTextTip);
        setName(_name);
        this.value = _value;
        this.node = _node;
        this.getPropertyEditor().addPropertyChangeListener(this);
    }

    public long getId(){
        return node.getClassMetadata().getOid();
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try{
            
            LocalClassMetadata update = CommunicationsStub.getInstance().getMetaForClass(node.getClassMetadata().getOid(), true);
            String[] attributes = new String[] {this.getName()};
            Object[] values = new Object[]{t};

            byte[] smallIcon = null;
            byte[] icon = null;
            String x;
            if(attributes[0].equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NAME")))
                update.setClassName((String)values[0]);
            
            if(attributes[0].equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DISPLAYNAME")))
                update.setDisplayName((String)values[0]);
            
            if(attributes[0].equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DESCRIPTION")))
                update.setDescription((String)values[0]);
            
            if(attributes[0].equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ABSTRACT")))
                update.setAbstract((Boolean)values[0]);
            
            if(attributes[0].equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_INDESIGN")))
                update.setInDesign((Boolean)values[0]);
            
            if(attributes[0].equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_COUNTABLE")))
                update.setCountable((Boolean)values[0]);
            
    //            if(attributes[0].equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SMALL_ICON")))
    //                x = (String)values[0];
    //            
    //            if(attributes[0].equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ICON")))
    //                x = (String)values[0];
            
            if(!CommunicationsStub.getInstance().setClassMetadataProperties(update.getOid(), update.getClassName(), update.getDisplayName(), update.getDescription(), null, null, update.isAbstract(), update.isInDesign(), update.isCountable()))
                throw new Exception(CommunicationsStub.getInstance().getError());
           

            value = t;
            Cache.getInstace().removeMeta(node.getName());
            
        }catch(Exception e){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("Class update", NotificationUtil.ERROR, "An error occurred while updating this class metadata : "+e.getMessage());
        }
    }

    @Override
    public PropertyEditor getPropertyEditor(){
        if(getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ICON")))
            return new IconPropertyEditor(node.getClassMetadata().getOid(), false);
        if(getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SMALL_ICON")))
            return new IconPropertyEditor(node.getClassMetadata().getOid(), true);
        else
            return super.getPropertyEditor();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
         try {
            if (this.getValue() == null) 
                return;

            if (this.getName().equals("name")){
                node.getClassMetadata().setClassName((String)getPropertyEditor().getValue());
                node.setDisplayName((String)getPropertyEditor().getValue());
                node.refresh();
            }
        } catch (Exception ex) {
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("", NotificationUtil.ERROR, String.format("Error modifying class attribute %1s", ex.getMessage()));
        } 
    }
    
    @Override
    public boolean canWrite(){
        //Dates are read only  by now until we integrate a date picker
        if (getValueType().equals(Date.class)){
            return false;
        }
        return true;
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
