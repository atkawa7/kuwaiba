/*
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.applicationnodes.objectnodes.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListTypeItemNode;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.util.Lookup;

/**
 * Provides a valid representation of LocalObjects attributes as Properties,
 * as LocalObject is just a proxy and can't be a bean itself
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectNodeProperty extends ReadWrite implements PropertyChangeListener{
    private Object value;
    private List<LocalObjectListItem> list;
    private ObjectNode node;

    /**
     * This constructor is called when the type is anything but a list
     */
    public ObjectNodeProperty(String name, Class valueType, Object value,
            String displayName,String toolTextTip,ObjectNode node) {
        super(name,valueType,displayName,toolTextTip);
        this.setName(name);
        this.value = value;
        this.node = node;
        getPropertyEditor().addPropertyChangeListener(this);
    }

    /**
     * This constructor is called when the property is a list
     * @param name
     */
    public ObjectNodeProperty(String name, Class valueType, Object value,
            String displayName,String toolTextTip, List<LocalObjectListItem> list, ObjectNode node) {
        super(name,valueType,displayName,toolTextTip);
        if (value != null)
            this.value = value;
        else
            //If it is a null value, we create a dummy null value from the generic method available in the interface
            this.value = new LocalObjectListItem();
        this.list = list;
        this.node = node;
        getPropertyEditor().addPropertyChangeListener(this);
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
       return this.value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try{
            LocalObject update;
            
            if (t instanceof LocalObjectListItem)
                update = new LocalObject(node.getObject().getClassName(), node.getObject().getOid(), 
                        new String[]{this.getName()}, new Object[]{((LocalObjectListItem)t).getOid()});
            else
                update = new LocalObject(node.getObject().getClassName(), node.getObject().getOid(), 
                        new String[]{this.getName()}, new Object[]{t});
                
            if(!CommunicationsStub.getInstance().saveObject(update))
                throw new Exception(CommunicationsStub.getInstance().getError());
            else
                value = t;
            
            if (node instanceof ListTypeItemNode)
                CommunicationsStub.getInstance().getList(node.getObject().getClassName(), true, true);
        }catch(Exception e){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("Object update", NotificationUtil.ERROR, "An error occurred while updating this object: "+e.getMessage());
        }
    }

    
    @Override
    public PropertyEditor getPropertyEditor(){
        if (value instanceof LocalObjectListItem)
            return new ItemListPropertyEditor(list);
        else
            return super.getPropertyEditor();
    }

     //PropertyListener methods
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (this.getValue() == null) 
                return;

            if (this.getName().equals(Constants.PROPERTY_NAME)){
                node.getObject().setName((String)getPropertyEditor().getValue());
                node.setDisplayName((String)getPropertyEditor().getValue());
            }
            
        } catch (Exception ex) {} 
    }

    @Override
    public boolean canWrite(){
        //Dates are read only  by now until we integrate a date picker
        if (getValueType().equals(Date.class))
            return false;
        return true;
    }
}