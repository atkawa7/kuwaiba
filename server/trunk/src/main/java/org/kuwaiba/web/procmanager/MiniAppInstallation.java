/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.procmanager;

import com.neotropic.kuwaiba.modules.reporting.img.SceneExporter;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.ejb.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.openide.util.Exceptions;

/**
 * Wrapped to configure the instalation of a device
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MiniAppInstallation extends AbstractComponentMiniApplication {

    public MiniAppInstallation(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Component launchDetached() {
        return null;
    }

    @Override
    public Component launchEmbedded() {        
        List<String> objectClasses = new ArrayList();
        List<String> objectIds = new ArrayList();
        
        objectClasses.add("MPLSRouter");
        objectIds.add("23196");
        
        objectClasses.add("MPLSRouter");
        objectIds.add("8596");
                
        objectClasses.add("ODF");
        objectIds.add("4019");
        
        List<String> portClasses = new ArrayList();
        List<String> portIds = new ArrayList();
        
        portClasses.add("OpticalPort");
        portIds.add("35583");
        
        portClasses.add("OpticalPort");
        portIds.add("4254");
        
        portClasses.add("OpticalPort");
        portIds.add("14853");
                
        Panel panel = new Panel();
        VerticalLayout verticalLayout = new VerticalLayout();
        
        Grid<MaterialBean> gridMaterials = new Grid<>();
        List<MaterialBean> materialBeans = new ArrayList();
        String columnMaterialId = "columnMaterialId"; //NOI18N
        String columnCityId = "columnCityId"; //NOI18N
        String columnRackId = "columnRackId"; //NOI18N
        String columnViewRackId = "columnViewRackId"; //NOI18N
        String columnSelectRackId = "columnSelectRackId"; //NOI18N
                
        for (int i = 0; i < objectClasses.size(); i++) {

            MaterialBean materialBean = new MaterialBean(Long.valueOf(objectIds.get(i)), objectClasses.get(i), getWebserviceBean());
            materialBeans.add(materialBean);
        }
        gridMaterials.setItems(materialBeans);
        gridMaterials.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridMaterials.setWidth("100%");

        gridMaterials.addColumn(MaterialBean::getMaterial).setCaption("Material").setId(columnMaterialId);
        gridMaterials.addColumn(MaterialBean::getCity).setCaption("City").setId(columnCityId);
        gridMaterials.addColumn(MaterialBean::getRack).setCaption("Rack").setId(columnRackId);
        gridMaterials.addColumn(MaterialBean::getBtnRackView, new ButtonRenderer(new RendererClickListener<MaterialBean>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent<MaterialBean> event) {
                
                MaterialBean materialBean = (MaterialBean) event.getItem();
                                
                SceneExporter sceneExporter = SceneExporter.getInstance();
                
                String oldPath = SceneExporter.PATH;
                String newPath = "/data/attachments/"; //NOI18N

                SceneExporter.PATH = newPath;

                String img = sceneExporter.buildRackView(
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")), //NOI18N
                    getWebserviceBean(), 
                    materialBean.getRackObject().getClassName(), 
                    materialBean.getRackObject().getId());

                SceneExporter.PATH = oldPath;
                
                Panel panel = new Panel();

                FileResource resource = new FileResource(new File(newPath + img + ".png"));                    

                Image image = new Image();
                image.setSource(resource);
                
                image.setWidth("100%");
                image.setHeightUndefined();
                
                panel.setSizeFull();
                panel.setContent(image);
                
                Window window = new Window();
                window.setWidth("90%");
                window.setHeight("80%"); 
                window.setContent(panel);
                window.center();

                UI.getCurrent().addWindow(window);
            }
        })).setCaption("Actions").setId(columnViewRackId);        
        gridMaterials.addColumn(MaterialBean::getBtnSelectRack, new ButtonRenderer(new RendererClickListener<MaterialBean>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent<MaterialBean> event) {
                
                MaterialBean materialBean = (MaterialBean) event.getItem();
            }
        })).setCaption("Actions").setId(columnSelectRackId);
        
        Label lblMaterials = new Label("Materials");
        lblMaterials.addStyleNames(ValoTheme.LABEL_H1);

        verticalLayout.addComponent(lblMaterials);
        verticalLayout.addComponent(gridMaterials);
        
        Grid<PortBean> gridPort = new Grid<>();
        List<PortBean> portBeans = new ArrayList();
        String columnPortId = "columnPortId"; //NOI18N
        String columnPortActionsId = "columnPortActionsId"; //NOI18N
        
        for (int i = 0; i < objectClasses.size(); i++) {

            PortBean portBean = new PortBean(Long.valueOf(portIds.get(i)), portClasses.get(i), getWebserviceBean());
            portBeans.add(portBean);
        }
        gridPort.setItems(portBeans);
        gridPort.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridPort.setWidth("100%");

        gridPort.addColumn(PortBean::getPort).setCaption("Port").setId(columnPortId);
        gridPort.addColumn(PortBean::getBtnViewPhysicalPath, new ButtonRenderer(new RendererClickListener<PortBean>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent<PortBean> event) {
                PortBean portBean = (PortBean) event.getItem();
                
                SceneExporter sceneExporter = SceneExporter.getInstance();
                
                String oldPath = SceneExporter.PATH;
                String newPath = "/data/attachments/"; //NOI18N

                SceneExporter.PATH = newPath;
                try {
                    String img = sceneExporter.buildPhysicalPathView(portBean.getObjectClass(), portBean.getObjectId());
                    
                    

                    Panel panel = new Panel();

                    FileResource resource = new FileResource(new File(newPath + img + ".png"));                    

                    Image image = new Image();
                    image.setSource(resource);

                    image.setWidth("100%");
                    image.setHeight("100%");

                    panel.setSizeFull();
                    panel.setContent(image);

                    Window window = new Window();
                    window.setWidth("90%");
                    window.setHeight("80%"); 
                    window.setContent(panel);
                    window.center();

                    UI.getCurrent().addWindow(window);
                
                } catch (MetadataObjectNotFoundException | ObjectNotFoundException | ApplicationObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                SceneExporter.PATH = oldPath;
            }
        })).setCaption("Actions").setId(columnPortActionsId);
        
        Label lblPorts = new Label("Ports");
        lblPorts.addStyleName(ValoTheme.LABEL_H1);
        
        verticalLayout.addComponent(lblPorts);
        verticalLayout.addComponent(gridPort);
                                            
        panel.setContent(verticalLayout);
        return panel;
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
    public class MaterialBean {
        private final WebserviceBean webserviceBean;
        private final String objectClass;
        private final long objectId;
        private RemoteObjectLight material;
        private RemoteObjectLight city;
        private RemoteObjectLight rack;
        
        public MaterialBean(long objectId, String objectClass, WebserviceBean webserviceBean) {
            this.objectId = objectId;
            this.objectClass = objectClass;
            this.webserviceBean = webserviceBean;
        }
        
        public String getMaterial() {
            try {
                material = webserviceBean.getObjectLight(
                    objectClass,
                    objectId,
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                return material != null ? material.getName() : null;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage());
            }
            return null;
        }
        
        public String getCity() {
            try {
                city = webserviceBean.getFirstParentOfClass(
                        objectClass,
                        objectId,
                        "City", //NOI18N
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                                
                return city != null ? city.getName() : null;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage());
            }
            return null;
        }
        
        public RemoteObjectLight getRackObject() {
            getRack();
            return rack;
        }
        
        public String getRack() {
            try {
                rack = webserviceBean.getFirstParentOfClass(
                        objectClass,
                        objectId,
                        "Rack", //NOI18N
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                                
                return rack != null ? rack.getName() : null;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage());
            }
            return null;
        }
        
        public String getBtnSelectRack() {
            return "Select Rack";
        }
        
        public String getBtnRackView() {
            return "Show Rack View";
        }
    }
    
    public class PortBean {
        private WebserviceBean webserviceBean;
        private String objectClass;
        private long objectId;
                
        public PortBean(long objectId, String objectClass, WebserviceBean webserviceBean) {
            this.objectId = objectId;
            this.objectClass = objectClass;
            this.webserviceBean = webserviceBean;
        }
        
        public String getObjectClass() {
            return objectClass;
        }
        
        public long getObjectId() {
            return objectId;
        }
        
        private RemoteObjectLight getPortObject() {
            try {
                RemoteObjectLight port = webserviceBean.getObjectLight(
                    objectClass,
                    objectId,
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                return port;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage());
            }
            return null;
        }
        
        public String getPort() {
            RemoteObjectLight port =  getPortObject();
            return port != null ? port.getName() : null;
        }
        
        public String getBtnViewPhysicalPath() {
            return "Show Phisical Path";
        }
    }
}
