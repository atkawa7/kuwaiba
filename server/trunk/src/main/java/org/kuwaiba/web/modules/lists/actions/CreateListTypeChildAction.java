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
package org.kuwaiba.web.modules.lists.actions;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.listmanagernodes.ListTypeChildNode;
import org.kuwaiba.apis.web.gui.nodes.listmanagernodes.ListTypeNode;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfoLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CreateListTypeChildAction extends AbstractAction {
    
    public CreateListTypeChildAction() {
        super("New object");
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        try {
            ListTypeNode parentNode = (ListTypeNode) targetObject;
            ClassInfoLight parentObject = (ClassInfoLight) parentNode.getObject();
            
            TopComponent parentComponent = ((DynamicTree) sourceComponent)
                    .getTopComponent();
            
            String className = parentObject.getClassName();
            
            long oid = parentComponent.getWsBean().createListTypeItem(
                    className, "", "", 
                    Page.getCurrent().getWebBrowser().getAddress(),
                    parentComponent.getApplicationSession().getSessionId());
            
            RemoteObjectLight listTypeObject = new RemoteObjectLight(oid, "", className);
            ListTypeChildNode childNode = new ListTypeChildNode(listTypeObject);
            childNode.setTree(parentNode.getTree());
            
            //((DynamicTree) sourceComponent).setParent(childNode, parentNode);            
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
}
