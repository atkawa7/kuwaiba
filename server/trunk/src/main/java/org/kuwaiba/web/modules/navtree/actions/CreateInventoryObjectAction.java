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
package org.kuwaiba.web.modules.navtree.actions;

import com.vaadin.ui.Notification;
import org.kuwaiba.apis.web.gui.actions.CreateInventoryObjectChildAction;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectRootNode;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.physicalconnections.windows.EndpointNode;

/**
 * Action that requests an Inventory Object creation
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CreateInventoryObjectAction extends CreateInventoryObjectChildAction {
    private AbstractNode parentNode;

    public CreateInventoryObjectAction() {
        super("New");
    }

    @Override
    public void finalActionPerformed(Object sourceComponent, Object targetObject, Object selectedOption) {
        super.finalActionPerformed(sourceComponent, targetObject, selectedOption);
            
        RemoteObjectLight object = getNewObject();
            
        AbstractNode childNode = null;
        // special case of inventory object that represent a port
        if (parentNode instanceof EndpointNode) 
            childNode = new EndpointNode(object);
        else
            childNode = new InventoryObjectNode(object);
        
        childNode.setTree(parentNode.getTree());
        //parentNode.getTree().setParent(childNode, parentNode);            
            
        Notification.show("Object created successfully",  Notification.Type.TRAY_NOTIFICATION);
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        if (targetObject instanceof AbstractNode) {
            parentNode = (AbstractNode) targetObject;           
            
            if (targetObject instanceof InventoryObjectRootNode) {
                super.actionPerformed(
                        parentNode.getTree(),
                        new RemoteObjectLight(-1, null, "DummyRoot"));
            }
            
            if (targetObject instanceof InventoryObjectNode) {
                super.actionPerformed(
                    parentNode.getTree(), 
                    ((RemoteObjectLight) parentNode.getObject())
                );
            }
        }
    }
}