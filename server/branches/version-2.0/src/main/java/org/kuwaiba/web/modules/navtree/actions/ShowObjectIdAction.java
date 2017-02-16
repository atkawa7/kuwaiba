/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.modules.navtree.actions;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
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
 * Gets the selected object oid of the Inventory Object Node
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShowObjectIdAction extends AbstractAction implements Window.CloseListener {
    private InventoryObjectNode node;
    
    public ShowObjectIdAction() {
        super("Show Object Id");
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        node = (InventoryObjectNode) targetObject;
        
        UI.getCurrent().addWindow(
                new ShowObjectIdWindows());
    }

    @Override
    public void windowClose(Window.CloseEvent e) {}
    
    public class ShowObjectIdWindows extends MessageDialogWindow {

        public ShowObjectIdWindows() {
            super(ShowObjectIdAction.this, "Object id", 
                    MessageDialogWindow.ONLY_OK_OPTION);
        }

        @Override
        public VerticalLayout initContent() {
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
                
                Label lblId = new Label("<b>id: </b>" + object.getOid(),ContentMode.HTML);
                Label lblClass = new Label("<b>Class: </b>" + object.getClassName(),ContentMode.HTML);
                Label lblContainment = new Label("<b>Containment Path: </b>" + containmentPath,ContentMode.HTML);
                
                content.addComponent(lblId);
                content.addComponent(lblClass);
                content.addComponent(lblContainment);
                
                return content;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                return null;
            }
        }
    }
}
