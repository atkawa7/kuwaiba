/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.nodes.properties;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.navigation.navigationtree.nodes.ReadOnlyObjectNode;
import org.openide.explorer.propertysheet.PropertySheetView;


/**
 * Provides a custom property editor for list-type values
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ItemListPropertyEditor extends PropertyEditorSupport {
    
    private ListTypeProperty parentProperty;
    private List<LocalObjectListItem> list;

    public ItemListPropertyEditor(List<LocalObjectListItem> list, ListTypeProperty parentProperty){        
        this.list = list;
        this.parentProperty = parentProperty;
    }
    
    @Override
    public String getAsText(){
        return getValue() == null ? list.get(0).toString() : getValue().toString();
    }

    @Override
    public void setAsText(String text){
        for (LocalObjectListItem loli : list) {
            if (text.equals(loli.getName())) {
                setValue(loli);
                break;
            }
        }
    }

    @Override
    public String[] getTags(){
        //Remember that CommunicationsStub->getList returns the list, but adds the null value as well,
        //so it's not necessary to add it here
        String [] res = new String[list.size()];
    
        for (int i = 0; i < list.size(); i++)
            res[i] = list.get(i).toString();
        
        return res;
    }

    @Override
    public boolean supportsCustomEditor(){
        return true;
    }
    
    @Override
    public Component getCustomEditor() {
        final PropertySheetView psv = new PropertySheetView();
        
                
        psv.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                try {
                    psv.setNodes(new ReadOnlyObjectNode[] {new ReadOnlyObjectNode(parentProperty.getValue())});
                } catch (IllegalAccessException | InvocationTargetException ex) {
                }
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
}