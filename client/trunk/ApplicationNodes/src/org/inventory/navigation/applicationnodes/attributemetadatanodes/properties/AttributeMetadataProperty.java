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
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.AttributeMetadataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Provides a property editor
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
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
                attribute.setId(_node.getObject().getId());
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
                    attribute.setUnique((Boolean) t);

                if (getName().equals(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NO_COPY"))) 
                    attribute.setNoCopy((Boolean) t);

                try{
                    CommunicationsStub.getInstance().setAttributePropertyValue(classId, attribute.getId(), 
                            attribute.getName(), attribute.getDisplayName(), attribute.getType().toString(), 
                            attribute.getDescription(), attribute.isAdministrative(), attribute.isVisible(), attribute.isReadOnly(), attribute.isNoCopy(), attribute.isUnique());
            
                    this._value = t;
                }catch(Exception e){
                    nu.showSimplePopup("Attribute Property Update", NotificationUtil.ERROR, com.getError());                    
                }
                break;
            }
        }
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
        if(_name.equals("name") && _value.equals("name") || _name.equals("creationDate"))
            return false;
        else
            return true;
    }
}
