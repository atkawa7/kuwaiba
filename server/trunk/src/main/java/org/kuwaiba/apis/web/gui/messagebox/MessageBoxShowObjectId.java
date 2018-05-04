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
package org.kuwaiba.apis.web.gui.messagebox;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import de.steinwedel.messagebox.MessageBox;
import java.util.List;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MessageBoxShowObjectId {
    private final TopComponent parentComponent;
    private final RemoteObjectLight object;    
    
    public MessageBoxShowObjectId(TopComponent parentComponent, RemoteObjectLight object) {
        this.parentComponent = parentComponent;
        this.object = object;
    }
    
    public void open() {
        if (parentComponent == null || object == null)
            return;
        
        try {
            List<RemoteObjectLight> parents = parentComponent.getWsBean().getParents(
                object.getClassName(),
                object.getOid(),
                Page.getCurrent().getWebBrowser().getAddress(),
                parentComponent.getApplicationSession().getSessionId());
            
            String containmentPath = "";
            for (RemoteObjectLight parent : parents)
                containmentPath += ":" + parent;
            
            VerticalLayout content = new VerticalLayout();
            content.setMargin(true);
            
            Label lblId = new Label(String.format("<b>id: </b>%s", object.getOid()), ContentMode.HTML);
            Label lblClass = new Label(String.format("<b>Class: </b>%s", object.getClassName()), ContentMode.HTML);
            Label lblContainment = new Label(String.format("<b>Containment Path </b>%s", containmentPath), ContentMode.HTML);
                
            content.addComponent(lblId);
            content.addComponent(lblClass);
            content.addComponent(lblContainment);
            
            MessageBox messageBox = MessageBox.createInfo()
                .withCaption("Additional Information")
                .withMessage(content)
                .withOkButton();
            messageBox.open();
                
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }        
    }
}