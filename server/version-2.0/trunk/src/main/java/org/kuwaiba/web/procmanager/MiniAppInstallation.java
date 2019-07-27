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
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
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
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.procmanager.connections.ComponentConnectionCreator;
import org.kuwaiba.web.procmanager.rackview.ComponentDeviceList;
import org.kuwaiba.web.procmanager.rackview.ComponentRackSelector;

/**
 * Mini application used to configure the installation of a device
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
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
            MaterialBean materialBean = new MaterialBean(device, wsBean);
            materialBeans.add(materialBean);
        }
        devicesGrid.setItems(materialBeans);
                
    }
    
    private List<RemoteObject> updateMaterials() {
        List<RemoteObject> materials = new ArrayList();
        
        if (getInputParameters() != null) {
            for (String id : getInputParameters().stringPropertyNames()) {
                try {
                    RemoteObject material = wsBean.getObject(
                            getInputParameters().getProperty(String.valueOf(id)),
                            String.valueOf(id),
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                    materials.add(material);
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            }
        }
        return materials;
    }

    @Override
    public Component launchEmbedded() {
        
        Panel panel = new Panel();
        panel.setWidth(1280, Unit.PIXELS);
        panel.setHeight(720, Unit.PIXELS);
        
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeUndefined();
        
        Grid<MaterialBean> gridMaterials = new Grid<>();
        gridMaterials.setSelectionMode(Grid.SelectionMode.SINGLE);
        gridMaterials.setWidth(1200, Unit.PIXELS);
                
        String columnMaterialId = "columnMaterialId"; //NOI18N
        String columnCityId = "columnCityId"; //NOI18N
        String columnBuildingId = "columnBuildingId"; //NOI18N
        String columnWarehouseId = "columnWarehouseId"; //NOI18N
        String columnRackId = "columnRackId"; //NOI18N
        String columnViewRackId = "columnViewRackId"; //NOI18N
        String columnCountryId = "columnCountryId"; //NOI18N
////        String columnSelectRackId = "columnSelectRackId"; //NOI18N
                
        updateDevicesGrid(updateMaterials(), gridMaterials);
        
        ButtonRenderer buttonRenderer = new ButtonRenderer(new RendererClickListener<MaterialBean>() {
            
            @Override
            public void click(ClickableRenderer.RendererClickEvent<MaterialBean> event) {
                
                MaterialBean materialBean = (MaterialBean) event.getItem();
                
                if (materialBean == null)
                    return;
                else if (materialBean.getRackObject() == null)
                    return;
                                                
                SceneExporter sceneExporter = SceneExporter.getInstance();
                
                String oldPath = SceneExporter.PATH;
                String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
                String newPath = processEnginePath + "/temp/"; //NOI18N

                SceneExporter.PATH = newPath;

                String img = sceneExporter.buildRackView(
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")), //NOI18N
                    wsBean, 
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
        });
        buttonRenderer.setHtmlContentAllowed(true);
        
        gridMaterials.addColumn(MaterialBean::getMaterial).setCaption("Material").setId(columnMaterialId);
        gridMaterials.addColumn(MaterialBean::getRack).setCaption("Rack").setId(columnRackId);
        gridMaterials.addColumn(MaterialBean::getBtnRackView, buttonRenderer).
            setMinimumWidth(50f).
            setMaximumWidth(50f).
            setDescriptionGenerator(e -> "<b>Rack View</b>", ContentMode.HTML).
            setId(columnViewRackId);
        gridMaterials.addColumn(MaterialBean::getWarehouse).setCaption("Warehouse").setId(columnWarehouseId);
        gridMaterials.addColumn(MaterialBean::getBuilding).setCaption("Building").setId(columnBuildingId);
        gridMaterials.addColumn(MaterialBean::getCity).setCaption("City").setId(columnCityId);
        gridMaterials.addColumn(MaterialBean::getCountry).setCaption("Country").setId(columnCountryId);
        
        Button btnRackView = new Button();
        btnRackView.setCaption("Rack Configuration");
        btnRackView.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
                ComponentDeviceList componentDeviceList = new ComponentDeviceList(updateMaterials(), wsBean, remoteSession);

                ComponentRackSelector componentRackSelector = new ComponentRackSelector(componentDeviceList, wsBean);

                Window window = new Window();
                window.setCaption("Rack Configuration");
                window.setDraggable(true);
                window.setContent(componentRackSelector);
                window.setSizeFull();
                window.addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent e) {                            
                        updateDevicesGrid(updateMaterials(), gridMaterials);
                    }
                });
                UI.getCurrent().addWindow(window);
            }
        });
        
        Button btnConnection = new Button("Connect Devices");
        btnConnection.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
////                ComponentConnectionSource componentConnectionSource = new ComponentConnectionSource(updateMaterials(), wsBean);
////                ComponentConnectionCreator componentConnectionCreator = new ComponentConnectionCreator(componentConnectionSource, wsBean);
                ComponentConnectionCreator componentConnectionCreator = new ComponentConnectionCreator(updateMaterials(), wsBean);
                
                Window window = new Window();
                window.setCaption("Connect Devices");
                window.setDraggable(true);
                
                window.setContent(componentConnectionCreator);
                window.setSizeFull();
                window.addCloseListener(new Window.CloseListener() {
                    @Override
                    public void windowClose(Window.CloseEvent e) {                            
                        updateDevicesGrid(updateMaterials(), gridMaterials);
                    }
                });
                UI.getCurrent().addWindow(window);                
            }
        });        
        HorizontalLayout tools = new HorizontalLayout();
        tools.setSpacing(false);
        tools.setSizeUndefined();
        
        tools.addComponent(btnRackView);
        tools.addComponent(btnConnection);
                
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
        private final RemoteObject device;
        private RemoteObjectLight material;
        private RemoteObjectLight city;
        private RemoteObjectLight rack;
        private RemoteObjectLight building;
        private RemoteObjectLight warehouse;
        private RemoteObjectLight physicalNode;
        private RemoteObjectLight country;
        
        public MaterialBean(RemoteObject device, WebserviceBean webserviceBean) {
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
            }
            return null;
        }
        
        public String getCity() {
            RemoteObjectLight rol = null;
            
            if (physicalNode == null)
                rol = device;
            else
                rol = physicalNode;
            
            if (rol != null) {
                
                try {
                    city = webserviceBean.getFirstParentOfClass(
                            rol.getClassName(),
                            rol.getId(),
                            "City", //NOI18N
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                    return city != null ? city.getName() : null;
                } catch (ServerSideException ex) {
                }
            }
            return null;
        }
        
        public String getCountry() {
            RemoteObjectLight rol;
            if (physicalNode == null)
                rol = device;
            else
                rol = physicalNode;
            
            if (rol != null) {
                try {
                    country = webserviceBean.getFirstParentOfClass(
                        rol.getClassName(), 
                        rol.getId(), 
                        "Country", //NOI18N
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                    
                    return country != null ? country.getName() : null;
                } catch (ServerSideException ex) {
                }
            }
            return null;
        }
                
        public String getBuilding() {
            RemoteObjectLight rol = null;
            
            if (physicalNode == null)
                rol = device;
            else
                rol = physicalNode;
            
            if (rol != null) {
                
                try {
                    if (rol.getClassName().equals("Building")) //NOI18N
                        building = rol;
                    else {
                        building = webserviceBean.getFirstParentOfClass(
                                rol.getClassName(),
                                rol.getId(), "Building", //NOI18N
                                Page.getCurrent().getWebBrowser().getAddress(),
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                    }                    
                    return building != null ? building.getName() : null;
                } catch (ServerSideException ex) {
                }
            }
            return null;
        }
        
        public String getWarehouse() {
            try {
                warehouse = webserviceBean.getWarehouseToObject(
                    device.getClassName(), 
                    device.getId(), 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                physicalNode = webserviceBean.getPhysicalNodeToObjectInWarehouse(
                    device.getClassName(), 
                    device.getId(), 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                return warehouse != null ? warehouse.getName() : null;
            } catch (ServerSideException ex) {
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
            }
            return null;
        }
        
        public String getBtnSelectRack() {
            return "Select Rack";
        }
        
        public String getBtnRackView() {
            return "<span class=\"v-icon\" style=\"font-family: " //NOI18N
                + VaadinIcons.SERVER.getFontFamily() 
                + "\">&#x" //NOI18N
                + Integer.toHexString(VaadinIcons.SERVER.getCodepoint())
                + ";</span>"; //NOI18N
        }
    }
}
