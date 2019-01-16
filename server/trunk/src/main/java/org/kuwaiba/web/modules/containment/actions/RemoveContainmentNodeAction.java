/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.containment.actions;

import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Notification;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RemoveContainmentNodeAction extends AbstractAction {

    public RemoveContainmentNodeAction() {
        super("Remove", new ThemeResource("img/warning.gif"));
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
//        try {
//            AbstractNode node = (AbstractNode) targetObject;
//            AbstractNode parentNode = (AbstractNode) 
//                    ((DynamicTree) sourceComponent).getParent(node);
//            
//            ClassInfoLight parentObject = (ClassInfoLight) parentNode.getObject();
//            ClassInfoLight childObject = (ClassInfoLight) node.getObject();
//            
//            TopComponent parentComponent = ((DynamicTree) sourceComponent)
//                    .getTopComponent();
//            
//            parentComponent.getWsBean().removePossibleChildren(
//                    parentObject.getId(), 
//                    new long[]{ childObject.getId() }, 
//                    Page.getCurrent().getWebBrowser().getAddress(),
//                    parentComponent.getApplicationSession().getSessionId());
//            
//            node.delete();
//            
//            Notification.show("Operation completed successfully", Notification.Type.TRAY_NOTIFICATION);
//        } catch (ServerSideException ex) {
//            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//        }
    }

    @Override
    public void actionPerformed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
