/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.custom;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Tree;

/**
 * A tree view section show a tree
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class TreeView extends CustomComponent {
    private Tree menu = new Tree();
    private String oldValue;
    
    public TreeView(final EventBus eventBus) {
        
        
        menu.addItem("Colombia");
        menu.addItem("Valle del Cauca");
        menu.addItem("Santiago de Cali");
        menu.addItem("Cauca");
        menu.addItem("Popayán");
        menu.addItem("Nariño");
        menu.addItem("Pasto");
        
        menu.setParent("Valle del Cauca", "Colombia");
        menu.setParent("Cauca", "Colombia");
        menu.setParent("Nariño", "Colombia");
        menu.setParent("Santiago de Cali", "Valle del Cauca");
        menu.setParent("Popayán", "Cauca");
        menu.setParent("Pasto", "Nariño");
        
        menu.expandItem("Colombia");
        menu.expandItem("Valle del Cauca");
        menu.expandItem("Cauca");
        menu.expandItem("Nariño");
        
        menu.setDragMode(Tree.TreeDragMode.NODE);
        menu.addItemClickListener(new ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                oldValue = (String) event.getItemId();
                eventBus.post(event);
            }
        });
        
        setCompositionRoot(menu);
    }   
    
    @Subscribe
    public void nameChange(Property.ValueChangeEvent value) {        
        String newValue = (String) value.getProperty().getValue();
        menu.setItemCaption(oldValue, newValue);       
    }
}
