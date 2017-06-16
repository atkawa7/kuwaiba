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

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyEditorSupport;
import org.inventory.customization.classhierarchy.nodes.AttributeMetadataNode;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertySheetView;

/**
 * This is the editor to change the class attributes properties
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class AttributeEditorSupport extends PropertyEditorSupport
    implements ExPropertyEditor {
    
    /**
     * Reference to the AttributeMetadataProperty
     */
    private ClassAttributeMetadataProperty parentProperty;
    /**
     * Reference to de CommunicationsStub singleton instance
     */
    private PropertySheetView psv;
    

    public AttributeEditorSupport(ClassAttributeMetadataProperty parentProperty) {
        this.parentProperty = parentProperty;
    }
    
    @Override
    public boolean supportsCustomEditor(){
        return true;
    }
    
    @Override
    public Component getCustomEditor(){
        this.psv = new PropertySheetView();

        psv.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                //setNodes can't be called until the component is added to the component containment hierarchy and fully resized
                psv.setNodes(
                        new AttributeMetadataNode[]{
                            new AttributeMetadataNode(
                                    parentProperty.getAttributeMetadata(), 
                                    parentProperty.getClassNode())
                        }
                );
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
    }
    
    @Override
    public void setValue(Object o){
        
    }
    
    @Override
    public String getAsText(){
        return "[Click the button to edit]";
    }
    
    
    @Override
    public void attachEnv(PropertyEnv pe) {
    }
}