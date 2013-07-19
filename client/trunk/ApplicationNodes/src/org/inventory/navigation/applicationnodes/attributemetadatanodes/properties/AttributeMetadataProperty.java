/*
 * Copyright (c) 2013 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package org.inventory.navigation.applicationnodes.attributemetadatanodes.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.AttributeMetadataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 *
 * @author adrian
 */
public class AttributeMetadataProperty  extends PropertySupport.ReadWrite implements PropertyChangeListener {

    private Object _value;
    private String _name;
    private AttributeMetadataNode _node;
    private long classId;
    private LocalClassMetadata lclm;
    private LocalAttributeMetadata latm;
    
    public AttributeMetadataProperty(String _name, Class _valueType, Object _value,
            String _displayName, String _toolTextTip, AttributeMetadataNode _node, long classId) {
        super(_name,_valueType,_displayName,_toolTextTip);
        this._name = _name;
        this._value = _value;
        this._node = _node;
        this.classId = classId;
        
        this.getPropertyEditor().addPropertyChangeListener(this);
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return this._value;
    }
    
    @Override
    public PropertyEditor getPropertyEditor(){
        if (_name.equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_TYPE")))
            return new ListAttributeMetadataProperty();
        else
            return super.getPropertyEditor();
    }
    
    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        CommunicationsStub com = CommunicationsStub.getInstance();
        lclm = CommunicationsStub.getInstance().getMetaForClass(classId, true);
        
        
        for(LocalAttributeMetadata attribute: lclm.getAttributes()){
            if(attribute.getId() == _node.getObject().getId()){
                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NAME"))) 
                    attribute.setName(t.toString());

                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DISPLAYNAME"))) 
                    attribute.setDisplayName(t.toString());

                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DESCRIPTION")))
                    attribute.setDescription(t.toString());
                
                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_TYPE"))) 
                    attribute.setType(t.getClass());

                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_VISIBLE"))) 
                    attribute.setVisible((Boolean) t);

                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ADMINISTRATIVE"))) 
                    attribute.setAdministrative((Boolean) t);

                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_UNIQUE"))) 
                    System.out.println("a");

                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NO_SERIALIZABLE"))) 
                    System.out.println("a");

                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NO_COPY"))) 
                    System.out.println("a");
                
                CommunicationsStub.getInstance().setAttributePropertyValue(classId, attribute.getId(), 
                        attribute.getName(), attribute.getDisplayName(), attribute.getType().toString(), 
                        attribute.getDescription(), attribute.isAdministrative(), attribute.isVisible(), attribute.isNoCopy(), attribute.isUnique());
            }
        }
            
        if(true){
            this._value = t;
            //Refresh the cache
            //com.getMetaForClass(myClass.getClassName(), true);
            nu.showSimplePopup("Attribute Property Update", NotificationUtil.INFO, "Attribute modified successfully");
        }else
            nu.showSimplePopup("Attribute Property Update", NotificationUtil.ERROR, com.getError());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (this.getValue() == null) 
                return;

            if (this.getName().equals("name")){
                _node.getObject().setName((String)getPropertyEditor().getValue());
                _node.setDisplayName((String)getPropertyEditor().getValue());
            }
            
        } catch (Exception ex) {
            return;
        }
    }
    
    @Override
    public boolean canWrite(){
        return true;
    }
}
