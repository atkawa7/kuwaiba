/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.procmanager;

import com.neotropic.kuwaiba.modules.reporting.img.SceneExporter;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.ejb.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.apis.web.gui.notifications.MessageBox;
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
        String columnMaterialId = "columnMaterialId";
        String columnCityId = "columnCityId";
        String columnRackId = "columnRackId";
        String columnActionsId = "columnActionsId";
        
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
        gridMaterials.addColumn(MaterialBean::getbtnSelectRack, new ButtonRenderer(new RendererClickListener<MaterialBean>() {
            @Override
            public void click(ClickableRenderer.RendererClickEvent<MaterialBean> event) {
                
                MaterialBean materialBean = (MaterialBean) event.getItem();
            }
        })).setCaption("Actions").setId(columnActionsId);

        verticalLayout.addComponent(gridMaterials);
        
        Grid<PortBean> gridPort = new Grid<>();
        List<PortBean> portBeans = new ArrayList();
        String columnPortId = "columnPortId";
        String columnPortActionsId = "columnPortActionsId";
        
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
                
                SceneExporter sceneExporter = SceneExporter.getInstance(
                    PersistenceService.getInstance().getBusinessEntityManager(), 
                    PersistenceService.getInstance().getMetadataEntityManager());
                
                try {
                    String oldPath = SceneExporter.PATH;
                    String newPath = "/data/attachments/";
                    
                    SceneExporter.PATH = newPath;
                    
                    String img = sceneExporter.buildPhysicalPathView(portBean.getObjectClass(), portBean.getObjectId());
                    
                    SceneExporter.PATH = oldPath;
                                        
//                    MessageBox messageBox = MessageBox.getInstance();
                    
                    Panel panel = new Panel();
                    
                    FileResource resource = new FileResource(new File(newPath + img + ".png"));                    
                    
                    Image image = new Image();
                    image.setSource(resource);
                                        
//                    VerticalLayout verticalLayout = new VerticalLayout();
//                    verticalLayout.addComponent(image);
                                                            
                    panel.setContent(image);
//                    panel.setHeight(70, Sizeable.Unit.PERCENTAGE);
//                    panel.setWidth(80, Sizeable.Unit.PERCENTAGE);
                    
                    Window window = new Window();
                    window.setContent(panel);
                    window.center();
                    
                    UI.getCurrent().addWindow(window);
                    
//                    messageBox.showMessage(panel);
//                    
//                    messageBox.addClickListener(new ClickListener() {
//                        
//                        @Override
//                        public void buttonClick(Button.ClickEvent event) {
//                        }
//                    });
                    
                } catch (MetadataObjectNotFoundException | ObjectNotFoundException | 
                        ApplicationObjectNotFoundException | InvalidArgumentException | 
                        BusinessObjectNotFoundException ex) {
                    
                    Notification.show(ex.getMessage());
                }
            }
        })).setCaption("Actions").setId(columnPortActionsId);
        
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
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
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
                        "City",
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                
                return city != null ? city.getName() : null;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage());
            }
            return null;
        }
        
        public String getRack() {
            try {
                rack = webserviceBean.getFirstParentOfClass(
                        objectClass,
                        objectId,
                        "Rack",
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                
                return rack != null ? rack.getName() : null;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage());
            }
            return null;
        }
        
        public String getbtnSelectRack() {
            return "Select Rack";
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
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
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
