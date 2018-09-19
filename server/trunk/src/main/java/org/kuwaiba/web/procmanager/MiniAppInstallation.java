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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
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
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.procmanager.connections.ComponentConnectionCreator;
import org.kuwaiba.web.procmanager.connections.ComponentConnectionSource;
import org.kuwaiba.web.procmanager.rackview.ComponentDeviceList;
import org.kuwaiba.web.procmanager.rackview.ComponentRackSelector;

/**
 * Wrapped to configure the instalation of a device
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MiniAppInstallation extends AbstractMiniApplication<Component, Component> {

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
    
    private void updateDevicesGrid(List<RemoteObject> devices, Grid devicesGrid) {
        List<MaterialBean> materialBeans = new ArrayList();
        
        for (RemoteObject device : devices) {
            MaterialBean materialBean = new MaterialBean(device, getWebserviceBean());
            materialBeans.add(materialBean);
        }
        devicesGrid.setItems(materialBeans);
                
    }

    @Override
    public Component launchEmbedded() {
////        long targetId = 51970;
////        String targetClassName = "Rack";
        
////        List<String> objectClasses = new ArrayList();
////        List<String> objectIds = new ArrayList();
        
        List<RemoteObject> selectedDevices = new ArrayList();
        
        try {
////            List<RemoteObjectLight> childrenLight = getWebserviceBean().getObjectChildren(
////                    "Rack",
////                    51345,
////                    0,
////                    Page.getCurrent().getWebBrowser().getAddress(),
////                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
////            
////            for (RemoteObjectLight childLight : childrenLight) {
////                objectClasses.add(childLight.getClassName());
////                objectIds.add(String.valueOf(childLight.getId()));
////                
////                RemoteObject child = getWebserviceBean().getObject(
////                    childLight.getClassName(), 
////                    childLight.getId(), 
////                    Page.getCurrent().getWebBrowser().getAddress(),
////                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
////                
////                selectedDevices.add(child);                
////            }
////            
////            childrenLight = getWebserviceBean().getObjectChildren(
////                    targetClassName,
////                    targetId,
////                    0,
////                    Page.getCurrent().getWebBrowser().getAddress(),
////                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
////            
////            for (RemoteObjectLight childLight : childrenLight) {
////                objectClasses.add(childLight.getClassName());
////                objectIds.add(String.valueOf(childLight.getId()));
////                
////                RemoteObject child = getWebserviceBean().getObject(
////                    childLight.getClassName(), 
////                    childLight.getId(), 
////                    Page.getCurrent().getWebBrowser().getAddress(),
////                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
////                
////                selectedDevices.add(child);                
////            }
            if (getInputParameters() != null) {
                
                for (Object id : getInputParameters().keySet()) {

                    RemoteObject child = getWebserviceBean().getObject(
                        getInputParameters().getProperty(String.valueOf(id)), 
                        Long.valueOf(String.valueOf(id)), 
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                    selectedDevices.add(child);                
                }
            }
                        
        } catch (ServerSideException ex) {
            //Exceptions.printStackTrace(ex);
        }
//        List<String> portClasses = new ArrayList();
//        List<String> portIds = new ArrayList();
//        
//        portClasses.add("OpticalPort");
//        portIds.add("35583");
//        
//        portClasses.add("OpticalPort");
//        portIds.add("4254");
//        
//        portClasses.add("OpticalPort");
//        portIds.add("14853");
                
        Panel panel = new Panel();
        VerticalLayout verticalLayout = new VerticalLayout();
        
        Grid<MaterialBean> gridMaterials = new Grid<>();
        
        String columnMaterialId = "columnMaterialId"; //NOI18N
        String columnCityId = "columnCityId"; //NOI18N
        String columnRackId = "columnRackId"; //NOI18N
        String columnViewRackId = "columnViewRackId"; //NOI18N
        String columnSelectRackId = "columnSelectRackId"; //NOI18N
        
////        List<MaterialBean> materialBeans = new ArrayList();
////                
////        for (RemoteObject selectedDevice : selectedDevices) {
////            MaterialBean materialBean = new MaterialBean(selectedDevice, getWebserviceBean());
////            materialBeans.add(materialBean);
////        }
////        gridMaterials.setItems(materialBeans);
        updateDevicesGrid(selectedDevices, gridMaterials);

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
                String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
                String newPath = processEnginePath + "/temp/"; //NOI18N

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
        
////        gridMaterials.addColumn(MaterialBean::getBtnSelectRack, new ButtonRenderer(new RendererClickListener<MaterialBean>() {
////            @Override
////            public void click(ClickableRenderer.RendererClickEvent<MaterialBean> event) {
////                
////                MaterialBean materialBean = (MaterialBean) event.getItem();
////            }
////        })).setCaption("Actions").setId(columnSelectRackId);
        
////        Label lblMaterials = new Label("Materials");
////        lblMaterials.addStyleNames(ValoTheme.LABEL_H1);        
//        Grid<PortBean> gridPort = new Grid<>();
//        List<PortBean> portBeans = new ArrayList();
//        String columnPortId = "columnPortId"; //NOI18N
//        String columnPortActionsId = "columnPortActionsId"; //NOI18N
        
//        for (int i = 0; i < objectClasses.size(); i++) {
//
//            PortBean portBean = new PortBean(Long.valueOf(portIds.get(i)), portClasses.get(i), getWebserviceBean());
//            portBeans.add(portBean);
//        }
//        gridPort.setItems(portBeans);
//        gridPort.setSelectionMode(Grid.SelectionMode.SINGLE);
//        gridPort.setWidth("100%");
//
//        gridPort.addColumn(PortBean::getPort).setCaption("Port").setId(columnPortId);
//        gridPort.addColumn(PortBean::getBtnViewPhysicalPath, new ButtonRenderer(new RendererClickListener<PortBean>() {
//            @Override
//            public void click(ClickableRenderer.RendererClickEvent<PortBean> event) {
//                PortBean portBean = (PortBean) event.getItem();
//                
//                SceneExporter sceneExporter = SceneExporter.getInstance();
//                
//                String oldPath = SceneExporter.PATH;
//                String newPath = "/data/attachments/"; //NOI18N
//
//                SceneExporter.PATH = newPath;
//                try {
//                    String img = sceneExporter.buildPhysicalPathView(portBean.getObjectClass(), portBean.getObjectId());
//                    
//                    
//
//                    Panel panel = new Panel();
//
//                    FileResource resource = new FileResource(new File(newPath + img + ".png"));                    
//
//                    Image image = new Image();
//                    image.setSource(resource);
//
//                    image.setWidth("100%");
//                    image.setHeight("100%");
//
//                    panel.setSizeFull();
//                    panel.setContent(image);
//
//                    Window window = new Window();
//                    window.setWidth("90%");
//                    window.setHeight("80%"); 
//                    window.setContent(panel);
//                    window.center();
//
//                    UI.getCurrent().addWindow(window);
//                
//                } catch (MetadataObjectNotFoundException | ObjectNotFoundException | ApplicationObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//                SceneExporter.PATH = oldPath;
//            }
//        })).setCaption("Actions").setId(columnPortActionsId);
//        
//        Label lblPorts = new Label("Ports");
//        lblPorts.addStyleName(ValoTheme.LABEL_H1);
//        
//        verticalLayout.addComponent(lblPorts);
//        verticalLayout.addComponent(gridPort);
        
        Button btnRackView = new Button();
        btnRackView.setCaption("Rack Configuration");
        btnRackView.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                
////                try {

                                        
////                    RemoteObject targetObject = getWebserviceBean().getObject(
////                        targetClassName, 
////                        targetId, 
////                        Page.getCurrent().getWebBrowser().getAddress(), 
////                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                    
                    ComponentDeviceList componentDeviceList = new ComponentDeviceList(selectedDevices, getWebserviceBean());
////                    ComponentRackView componentRackView = new ComponentRackView(targetObject, getWebserviceBean());
                                        
                    ComponentRackSelector componentRackSelector = new ComponentRackSelector(componentDeviceList, getWebserviceBean()/*, componentRackView*/);
                    
                    Window window = new Window();
                    window.setContent(componentRackSelector);
                    window.setSizeFull();
                    window.addCloseListener(new Window.CloseListener() {
                        @Override
                        public void windowClose(Window.CloseEvent e) {                            
                            updateDevicesGrid(selectedDevices, gridMaterials);
                        }
                    });
                    UI.getCurrent().addWindow(window);
                
////                } catch (ServerSideException ex) {
////                    
////                }
            }
        });
        
        Button btnConnection = new Button();
        btnConnection.setCaption("Connect Devices");
        btnConnection.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ComponentConnectionSource componentConnectionSource = new ComponentConnectionSource(selectedDevices, getWebserviceBean());
////                ComponentConnectionTarget componentConnectionTarget = new ComponentConnectionTarget(getWebserviceBean());

                ComponentConnectionCreator componentConnectionCreator = new ComponentConnectionCreator(componentConnectionSource, getWebserviceBean()/*, componentConnectionTarget*/);

                Window window = new Window();
                window.setContent(componentConnectionCreator);
                window.setSizeFull();
                window.addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent e) {                            
                        updateDevicesGrid(selectedDevices, gridMaterials);
                    }
                });
                UI.getCurrent().addWindow(window);                
            }
        });
////        verticalLayout.addComponent(lblMaterials);
        HorizontalLayout tools = new HorizontalLayout();
        tools.setSpacing(false);
        tools.setSizeUndefined();
        
        tools.addComponent(btnRackView);
        tools.addComponent(btnConnection);
        
        //verticalLayout.addComponent(btnRackView);
        //verticalLayout.addComponent(btnConnection);
        verticalLayout.addComponent(tools);
        
        verticalLayout.addComponent(gridMaterials);
        panel.setContent(verticalLayout);
        return panel;
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
    public class MaterialBean {
        private final WebserviceBean webserviceBean;
//        private final String objectClass;
//        private final long objectId;
        private RemoteObject device;
        private RemoteObjectLight material;
        private RemoteObjectLight city;
        private RemoteObjectLight rack;
        
        public MaterialBean(/*long objectId, String objectClass, */RemoteObject device, WebserviceBean webserviceBean) {
//            this.objectId = objectId;
//            this.objectClass = objectClass;
            this.device = device;
            this.webserviceBean = webserviceBean;
        }
        
        public String getMaterial() {
            try {
                material = webserviceBean.getObjectLight(
                    device.getClassName(),
                    device.getId(),
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
                        device.getClassName(),
                        device.getId(),
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
                        device.getClassName(),
                        device.getId(),
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
