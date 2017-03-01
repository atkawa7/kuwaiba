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
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.windows.MessageDialogWindow;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * Shows the object id, location and class.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MoreInformationAction extends AbstractAction implements Window.CloseListener {
    private InventoryObjectNode node;
    
    public MoreInformationAction() {
        super("More Information");
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        node = (InventoryObjectNode) targetObject;
        
        UI.getCurrent().addWindow(
                new ShowObjectIdWindow());
    }

    @Override
    public void windowClose(Window.CloseEvent e) {}
    
    public class ShowObjectIdWindow extends MessageDialogWindow {

        public ShowObjectIdWindow() {
            super(MoreInformationAction.this, "Additional Information", 
                    MessageDialogWindow.ONLY_OK_OPTION);
        }

        @Override
        public Component initSimpleMainComponent() {
            try {
                RemoteObjectLight object = (RemoteObjectLight) node.getObject();
                
                TopComponent parentComponent = node.getTree().getTopComponent();
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
                
                Label lblId = new Label(String.format("<b>id: </b>%s", object.getOid(),ContentMode.HTML));
                Label lblClass = new Label(String.format("<b>Class: </b>%s", object.getClassName(),ContentMode.HTML));
                Label lblContainment = new Label(String.format("<b>Containment Path: </b>%s", containmentPath,ContentMode.HTML));
                
                content.addComponent(lblId);
                content.addComponent(lblClass);
                content.addComponent(lblContainment);
                
                return content;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                return null;
            }
        }

        @Override
        public void initComplexMainComponent() {}
    }
}
