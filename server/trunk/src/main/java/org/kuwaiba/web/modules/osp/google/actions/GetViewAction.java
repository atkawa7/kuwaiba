/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.modules.osp.google.actions;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Image;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.google.OSPTopComponent;
import org.kuwaiba.web.modules.osp.google.overlays.MarkerNode;
import org.openide.util.Exceptions;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Load the view of an element in the view
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GetViewAction extends AbstractAction {
    
    public GetViewAction(WebserviceBean wsBean) {
        super("Get View", wsBean);
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        
//        OSPTopComponent map = (OSPTopComponent) sourceComponent; map.getTopComponent();
//        WebserviceBeanLocal wsBean = map.getTopComponent().getWsBean();
//        String sessionId = map.getTopComponent().getApplicationSession().getSessionId();
//        String address = Page.getCurrent().getWebBrowser().getAddress();
//        
//        
//        RemoteObjectLight spliceBox = ((MarkerNode) targetObject).getRemoteObjectLight();
//        try {
//            SpliceBoxEscene sb = new SpliceBoxEscene(wsBean, sessionId, address);
//            sb.render(spliceBox);
//            Window subWindow = new Window("Registrar Nueva Empresa");
//            subWindow.setResizable(true);
//            subWindow.setClosable(true);
//            VerticalLayout subContent = new VerticalLayout();
//            // Show the image in the application
//            Image image = new Image(null, new ExternalResource("http://localhost:8080/imgs/"  +  spliceBox.getClassName() + "_" + spliceBox.getOid() +".png"));
//            Panel panel = new Panel("Embedding");
//            panel.setWidth("650px");
//            panel.setHeight("450px");
//            panel.setContent(image);
//            panel.getContent().setSizeUndefined();
//            subContent.addComponent(panel); //put the image here.
//            subWindow.setContent(subContent);
//            subWindow.setModal(true);
//            subWindow.center(); // Center it in the browser window
//            map.getUI().addWindow(subWindow); // Open it in the UI
//        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    @Override
    public void actionPerformed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
