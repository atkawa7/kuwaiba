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
package org.kuwaiba.web.modules.navtree.actions;

import com.vaadin.server.ThemeResource;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;

/**
 * Action to delete a inventory object
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeleteInventoryObjectAction extends AbstractAction {
        
    public DeleteInventoryObjectAction() {
        super("Delete", new ThemeResource("img/warning.gif"));
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
//        InventoryObjectNode node = (InventoryObjectNode) targetObject;
//        
//        MessageBox messageBox = MessageBox.createQuestion()
//            .withCaption("Delete Object")
//            .withMessage("Are you sure you want to delete this object? (all children will be removed as well)")
//            .withOkButton(() -> {
//                try {
//                    TopComponent parentComponent = node.getTree().getTopComponent();
//                    
//                    RemoteObjectLight object = (RemoteObjectLight) node.getObject();
//
//                    parentComponent.getWsBean().deleteObjects(
//                            new String[] {object.getClassName()}, 
//                            new long [] {object.getId()},
//                            false,
//                            Page.getCurrent().getWebBrowser().getAddress(),
//                            parentComponent.getApplicationSession().getSessionId());
//
//                    node.delete();
//
//                    Notification.show("The object was removed successfully", Notification.Type.TRAY_NOTIFICATION);
//
//                } catch (ServerSideException ex) {
//                    Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//                }
//            })
//            .withCancelButton();
//        messageBox.open();
    }

    @Override
    public void actionPerformed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
