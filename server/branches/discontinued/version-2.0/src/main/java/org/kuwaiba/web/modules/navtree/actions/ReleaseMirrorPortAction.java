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

import com.vaadin.server.Page;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import de.steinwedel.messagebox.MessageBox;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ReleaseMirrorPortAction extends AbstractAction {
    
    public ReleaseMirrorPortAction() {
        super("Release Mirror Port");
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        MessageBox mbSaveView = MessageBox.createWarning()
            .withCaption("Mirror Port Connection")
            .withMessage(new Label("Are you sure you want to release this mirror port?"))
            .withOkButton(() -> {
                                
                try {
                    InventoryObjectNode node = (InventoryObjectNode) targetObject;
                    TopComponent parentComponent = node.getTree().getTopComponent();

                    RemoteObjectLight selectedPort = (RemoteObjectLight) node.getObject();

                    parentComponent.getWsBean().releaseMirrorPort(
                            selectedPort.getClassName(),
                            selectedPort.getOid(),
                            Page.getCurrent().getWebBrowser().getAddress(),
                            parentComponent.getApplicationSession().getSessionId());
                    
                    Notification.show("Success", "Miror port released successfully", Notification.Type.ERROR_MESSAGE);
                    
                } catch (ServerSideException ex) {
                    NotificationsUtil.showError(ex.getMessage());
                }

            })
            .withCancelButton();
        
        mbSaveView.open();
    }
    
}
