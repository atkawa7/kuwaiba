/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.forms.components.impl;

import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementTree;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClickListener;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentTree extends GraphicalComponent {

    public ComponentTree(BasicTree dynamicTree) {
        super(dynamicTree);        
    }
    
    @Override
    public BasicTree getComponent() {
        return (BasicTree) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementTree) {
            //ElementTree tree = (ElementTree) element;
            
            getComponent().addItemClickListener(new ItemClickListener() {
                
                @Override
                public void itemClick(Tree.ItemClick event) {
                    
                    fireComponentEvent(new EventDescriptor(
                        Constants.EventAttribute.ONPROPERTYCHANGE, 
                        Constants.Property.VALUE, ((AbstractNode) event.getItem()).getObject(), null));
                }
            });
        }
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.VALUE.equals(event.getPropertyName())) {
                
                if (event.getNewValue() != null)
                    getComponent().resetTo(new InventoryObjectNode((RemoteObjectLight) event.getNewValue()));                                
                else {
                    getComponent().resetTo(new AbstractNode<RemoteObjectLight>(new RemoteObjectLight(org.kuwaiba.services.persistence.util.Constants.DUMMY_ROOT, "-1", "Navigation Root")) {
                        @Override
                        public AbstractAction[] getActions() { return new AbstractAction[0]; }

                        @Override
                        public void refresh(boolean recursive) { }
                    });
                }
            }
        }
    }
    
}
