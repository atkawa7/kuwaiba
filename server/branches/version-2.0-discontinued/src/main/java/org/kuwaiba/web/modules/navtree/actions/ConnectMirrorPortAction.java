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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import de.steinwedel.messagebox.MessageBox;
import java.util.List;
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
public class ConnectMirrorPortAction extends AbstractAction {    
    
    public ConnectMirrorPortAction() {
        super("Connect Mirror Port");
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        FormLayout formLayout = new FormLayout();        
        formLayout.setMargin(true);
                
        ComboBox cmbPorts = new ComboBox();
        cmbPorts.setCaption("The other ports in the parent device are");
        
        formLayout.addComponent(cmbPorts);
        //
        InventoryObjectNode node = (InventoryObjectNode) targetObject;
        RemoteObjectLight selectedPort = (RemoteObjectLight) node.getObject();
        
        TopComponent parentComponent = node.getTree().getTopComponent();
                
        try {
            List<RemoteObjectLight> ports = parentComponent.getWsBean().getSiblings(selectedPort.getClassName(), 
                selectedPort.getOid(), 
                -1, 
                Page.getCurrent().getWebBrowser().getAddress(), 
                parentComponent.getApplicationSession().getSessionId());
            
            cmbPorts.addItems(ports);
            
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
        }
        
        MessageBox mbSaveView = MessageBox.createQuestion()
            .withCaption("Mirror Port Connection")
            .withMessage(formLayout)
            .withOkButton(() -> {
                
                RemoteObjectLight selectedMirrorPort = (RemoteObjectLight) cmbPorts.getValue();
                
                try {
                    parentComponent.getWsBean().connectMirrorPort(
                            selectedPort.getClassName(),
                            selectedPort.getOid(),
                            selectedMirrorPort.getClassName(),
                            selectedMirrorPort.getOid(),
                            Page.getCurrent().getWebBrowser().getAddress(),
                            parentComponent.getApplicationSession().getSessionId());
                    
                    Notification.show("success", "Port mirrored successfully", Notification.Type.HUMANIZED_MESSAGE);
                    
                } catch (ServerSideException ex) {
                    
                    NotificationsUtil.showError(ex.getMessage());
                }
            })
            .withCancelButton();
        
        mbSaveView.open();
    }
    
}
