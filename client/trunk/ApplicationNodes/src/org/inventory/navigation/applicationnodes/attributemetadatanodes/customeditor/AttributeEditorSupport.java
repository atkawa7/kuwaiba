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
package org.inventory.navigation.applicationnodes.attributemetadatanodes.customeditor;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.AttributeMetadataNode;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.properties.ClassAttributeMetadataProperty;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * This is the editor to change the class attributes properties
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class AttributeEditorSupport extends PropertyEditorSupport
    implements ExPropertyEditor, VetoableChangeListener{
    
    /**
     * A reference to the notification mechanism
     */
    private NotificationUtil nu;

    /**
     * PropertyEnv instance
     */
    private PropertyEnv env;
    /**
     * Reference to the AttributeMetadataProperty
     */
    private LocalAttributeMetadata lam;
    /**
     * Reference to de CommunicationsStub singleton instance
     */
    private CommunicationsStub com;
    private PropertySheetView psv;
    private ClassAttributeMetadataProperty property;
    private long classId;
    

    public AttributeEditorSupport(LocalAttributeMetadata lam, long classId) {
        this.lam = lam;
        this.com = CommunicationsStub.getInstance();
        this.classId = classId;
        this.nu = Lookup.getDefault().lookup(NotificationUtil.class);
    }
    
    @Override
    public boolean supportsCustomEditor(){
        return true;
    }
    
    @Override
    public Component getCustomEditor(){
        //if (psv == null ){
            this.psv = new PropertySheetView();
            
            env.addVetoableChangeListener(this);
            
            psv.addComponentListener(new ComponentListener() {

                @Override
                public void componentResized(ComponentEvent e) {
                    //setNodes can't be called until the component is added to the component containment hierarchy and fully resized
                    psv.setNodes(new AttributeMetadataNode[]{new AttributeMetadataNode(lam,classId)});
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                }

                @Override
                public void componentShown(ComponentEvent e) {
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                }
            });
            return psv;
        //}else return psv;
    }
    
    @Override
    public void setValue(Object o){
        //Do nothing, because we set the password and make the validations in the vetoable event
    }
    
    @Override
    public String getAsText(){
        return "[Click on the button to edit]";
    }
    
    
    @Override
    public void attachEnv(PropertyEnv pe) {
        this.env = pe;
        this.env.addVetoableChangeListener(this);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
    
    }
    
}