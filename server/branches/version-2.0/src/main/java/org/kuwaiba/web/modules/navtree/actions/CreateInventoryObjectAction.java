/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractComposedAction;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectRootNode;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;

/**
 * Action that requests an Inventory Object creation
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CreateInventoryObjectAction extends AbstractComposedAction {

    public CreateInventoryObjectAction() {
        super("New");
    }

    @Override
    public void finalActionPerformed(Object sourceComponent, Object targetObject, Object selectedOption) {
        try {
            TopComponent parentComponent = ((AbstractNode) targetObject).getTree().getTopComponent();
            
            long oid = parentComponent.getWsBean().createObject(
                    ((ClassInfoLight) selectedOption).getClassName(), 
                    targetObject instanceof InventoryObjectRootNode ? null : ((RemoteObjectLight) ((InventoryObjectNode) targetObject).getObject()).getClassName(),
                    targetObject instanceof InventoryObjectRootNode ? -1 : ((RemoteObjectLight) ((InventoryObjectNode) targetObject).getObject()).getOid(),
                    new String[0],
                    new String[0][0],
                    0,
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    parentComponent.getApplicationSession().getSessionId());
            
            RemoteObjectLight object = parentComponent.getWsBean().getObjectLight(
                    ((ClassInfoLight) selectedOption).getClassName(), 
                    oid, 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    parentComponent.getApplicationSession().getSessionId());
            
            InventoryObjectNode childNode = new InventoryObjectNode(object);
            childNode.setTree(((AbstractNode) targetObject).getTree());
            
            ((AbstractNode) targetObject).getTree().setParent(childNode, targetObject);            
//            ((AbstractNode) targetObject).expand();
            
            Notification.show("Object created successfully",  Notification.Type.TRAY_NOTIFICATION);
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        if (targetObject instanceof AbstractNode) {
            TopComponent parentComponent = ((AbstractNode) targetObject).getTree().getTopComponent();
            
            List<ClassInfoLight> children = new ArrayList();
            try {
                if (targetObject instanceof InventoryObjectRootNode)
                    children = parentComponent.getWsBean().getPossibleChildren(
                            "DummyRoot", 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            parentComponent.getApplicationSession().getSessionId());
                
                if (targetObject instanceof InventoryObjectNode)
                    children = parentComponent.getWsBean().getPossibleChildren(
                            ((RemoteObjectLight)((InventoryObjectNode) targetObject).getObject()).getClassName(), 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            parentComponent.getApplicationSession().getSessionId());
                    
                if (!children.isEmpty())
                    showSubMenu(sourceComponent, targetObject, children);
                
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
            
        }
    }
}