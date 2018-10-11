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
package org.kuwaiba.web.modules.warehouse.dashboard;

import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.procmanager.MiniAppRackView;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SpareInventoryObjectsDashboardWidget extends AbstractDashboardWidget {
    private final WebserviceBean webserviceBean;

    public SpareInventoryObjectsDashboardWidget(AbstractDashboard rootComponent, WebserviceBean webserviceBean) {
        super("Spare Inventory Objects", rootComponent);
        this.webserviceBean = webserviceBean;
        this.createCover();
    }
    
    @Override
    public void createCover() { 
        VerticalLayout lytViewsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright"); //NOI18N
        lytViewsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                createContent();
                swap();
            }
        });
        
        lytViewsWidgetCover.addComponent(lblText);
        lytViewsWidgetCover.setSizeFull();
        lytViewsWidgetCover.setStyleName("dashboard_cover_widget-darkred"); //NOI18N
        this.coverComponent = lytViewsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        UI.getCurrent().getPage().getStyles().add(".v-nativebutton {" + //NOI18N
            "background:none!important;" + //NOI18N
            "color:inherit;" + //NOI18N
            "border:none;" + //NOI18N
            "padding:0!important;" + //NOI18N
            "font: inherit;" + //NOI18N
            "text-decoration:underline;" + //NOI18N
            "cursor:pointer;" + //NOI18N
            "}");
        
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addStyleName(title);
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(false);
        verticalLayout.setMargin(false);
                
        List<RemoteObject> spareObjects = getSpareObjects();
        
        if (spareObjects != null) {
            
            List<SpareBean> spareBeans = new ArrayList();
            
            for (RemoteObject spareObject : spareObjects)
                spareBeans.add(new SpareBean(spareObject, webserviceBean));
            
            final String columnName = "name"; //NOI18N
            final String columnVendor = "vendor"; //NOI18N
            final String columnState = "state"; //NOI18N
            final String columnPosition = "position"; //NOI18N
            final String columnRack = "rack"; //NOI18N
            final String columnWarehouse = "warehouse"; //NOI18N
            final String columnBuilding = "building"; //NOI18N
            final String columnCity = "city"; //NOI18N
            
            ButtonRenderer buttonRenderer = new ButtonRenderer(new RendererClickListener<SpareBean>() {

                @Override
                public void click(ClickableRenderer.RendererClickEvent<SpareBean> event) {
                    SpareBean processInstanceBean = (SpareBean) event.getItem();
                    
                    try {
                        Properties inputParameters = new Properties();
                        inputParameters.setProperty("id", String.valueOf(processInstanceBean.getRackObject().getId())); //NOI18N
                        inputParameters.setProperty("className", processInstanceBean.getRackObject().getClassName()); //NOI18N

                        MiniAppRackView miniAppRackView = new MiniAppRackView(inputParameters);
                        miniAppRackView.setWebserviceBean(webserviceBean);
                        
                        Window window = new Window();
                        window.setWidth(80, Unit.PERCENTAGE);
                        window.setHeight(80, Unit.PERCENTAGE);
                        window.setModal(true);
                        window.setContent(miniAppRackView.launchDetached());

                        UI.getCurrent().addWindow(window);
                    } catch(Exception ex) {
                        Notification.show("The rack view cannot be opened", Notification.Type.ERROR_MESSAGE);
                    }
                }
            });
            buttonRenderer.setHtmlContentAllowed(true);
                        
            Grid<SpareBean> grid = new Grid();
            grid.setWidth(95, Unit.PERCENTAGE);
            grid.setHeight(80, Unit.PERCENTAGE);
            grid.setItems(spareBeans);
            grid.addColumn(SpareBean::getName).setCaption("Name").setId(columnName);
            grid.addColumn(SpareBean::getVendor).setCaption("Vendor").setId(columnVendor);
            grid.addColumn(SpareBean::getState).setCaption("State").setId(columnState);
            grid.addColumn(SpareBean::getPosition).setCaption("Position").setId(columnPosition);
            grid.addColumn(SpareBean::getRackName).setCaption("Rack").setId(columnRack);
            grid.addColumn(SpareBean::getRackViewButtonCaption, buttonRenderer).
                setMinimumWidth(50f).
                setMaximumWidth(50f).
                setDescriptionGenerator(e -> "<b>Rack View</b>", ContentMode.HTML);
            grid.addColumn(SpareBean::getWarehouseName).setCaption("Warehouse").setId(columnWarehouse);
            grid.addColumn(SpareBean::getBuildingName).setCaption("Building").setId(columnBuilding);
            grid.addColumn(SpareBean::getCityName).setCaption("City").setId(columnCity);
            
            TextField txtName = new TextField();
            TextField txtVendor = new TextField();
            TextField txtState = new TextField();
            TextField txtPosition = new TextField();
            TextField txtRack = new TextField();
            TextField txtWarehouse = new TextField();
            TextField txtBuilding = new TextField();
            TextField txtCity = new TextField();
            
            HeaderRow headerRow = grid.appendHeaderRow();
            
            headerRow.getCell(columnName).setComponent(txtName);
            headerRow.getCell(columnVendor).setComponent(txtVendor);
            headerRow.getCell(columnState).setComponent(txtState);
            headerRow.getCell(columnPosition).setComponent(txtPosition);
            headerRow.getCell(columnRack).setComponent(txtRack);
            headerRow.getCell(columnWarehouse).setComponent(txtWarehouse);
            headerRow.getCell(columnBuilding).setComponent(txtBuilding);
            headerRow.getCell(columnCity).setComponent(txtCity);
            
            ValueChangeListener<String> valueChangeListener = new ValueChangeListener<String>() {
                @Override
                public void valueChange(HasValue.ValueChangeEvent<String> event) {

                    if (spareBeans != null) {
                        List<SpareBean> filteredItems = getSpareBeans(
                            spareBeans.iterator(), 
                            txtName.getValue(), 
                            txtVendor.getValue(), 
                            txtState.getValue(), 
                            txtPosition.getValue(), 
                            txtRack.getValue(), 
                            txtWarehouse.getValue(), 
                            txtBuilding.getValue(), 
                            txtCity.getValue());

                        grid.setItems(filteredItems);
                    }
                }
            };
            
            txtName.addValueChangeListener(valueChangeListener);
            txtVendor.addValueChangeListener(valueChangeListener);
            txtState.addValueChangeListener(valueChangeListener);
            txtPosition.addValueChangeListener(valueChangeListener);
            txtRack.addValueChangeListener(valueChangeListener);
            txtWarehouse.addValueChangeListener(valueChangeListener);
            txtBuilding.addValueChangeListener(valueChangeListener);
            txtCity.addValueChangeListener(valueChangeListener);
                        
            verticalLayout.addComponent(grid);
            verticalLayout.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);
            verticalLayout.setExpandRatio(grid, 1f);
        }
        addComponent(verticalLayout);        
        
        this.contentComponent = verticalLayout;
    }
    
    private List<SpareBean> getSpareBeans(Iterator<SpareBean> iterator, String filterName, String filterVendor,String filterState,String filterPosition, String filterRack, String filterWarehouse, String filterBuilding,String filterCity) {                
        
        List<SpareBean> filteredItems = new ArrayList();

        while (iterator.hasNext()) {
            SpareBean spereBean = iterator.next();
            
            boolean flagName = spereBean.getName().toUpperCase().contains(filterName != null ? filterName.toUpperCase() : "");
            boolean flagVendor = spereBean.getVendor().toUpperCase().contains(filterVendor != null ? filterVendor.toUpperCase() : "");
            boolean flagState = spereBean.getState().toUpperCase().contains(filterState != null ? filterState.toUpperCase() : "");
            boolean flagPosition = spereBean.getPosition().toUpperCase().contains(filterPosition != null ? filterPosition.toUpperCase() : "");
            boolean flagRack = spereBean.getRackName().toUpperCase().contains(filterRack != null ? filterRack.toUpperCase() : "");
            boolean flagWarehouse = spereBean.getWarehouseName().toUpperCase().contains(filterWarehouse != null ? filterWarehouse.toUpperCase() : "");
            boolean flagBuilding = spereBean.getBuildingName().toUpperCase().contains(filterBuilding != null ? filterBuilding.toUpperCase() : "");
            boolean flagCity = spereBean.getCityName().toUpperCase().contains(filterCity != null ? filterCity.toUpperCase() : "");
            
            if (flagName && flagVendor && flagState && flagPosition && flagRack && flagWarehouse && flagBuilding && flagCity)
                filteredItems.add(spereBean);
        }
        return filteredItems;
    }
        
    private List<RemoteObject> getSpareObjects() {
        RemoteObjectLight stateSpare = null;
        try {
            List<RemoteObjectLight> operationalStates = webserviceBean.getListTypeItems(
                    "OperationalState", //NOI18N
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
            
            for (RemoteObjectLight operationalState : operationalStates) {
                
                if ("Spare".equals(operationalState.getName())) { //NOI18N
                    stateSpare = operationalState;                                                                               
                }
            }
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
        
        if (stateSpare != null) {
            
            try {
                List<RemoteObject> spareObjects = webserviceBean.getObjectsWithFilter(
                        "InventoryObject", //NOI18N
                        "state", //NOI18N
                        String.valueOf(stateSpare.getId()),
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                return spareObjects;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }
        return null;
    }
    
    private class SpareBean {
        private final WebserviceBean webserviceBean;
        private final RemoteObject spareObject;
        private RemoteObjectLight rackObject;
        private RemoteObjectLight warehouseObject;
        private RemoteObjectLight buildingObject;
        private RemoteObjectLight cityObject;
                        
        public SpareBean(RemoteObject spareObject, WebserviceBean webserviceBean) {
            this.spareObject = spareObject;
            this.webserviceBean = webserviceBean;
            
            if (spareObject != null && webserviceBean != null) {
                
                try {
                    List<RemoteObjectLight> parents = webserviceBean.getParentsUntilFirstOfClass(
                            spareObject.getClassName(),
                            spareObject.getId(),
                            "City", //NOI18N
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                    for (RemoteObjectLight parent : parents) {

                        switch(parent.getClassName()) {
                            case "Rack": //NOI18N
                                rackObject = parent;
                            break;
                            case "Building": //NOI18N
                                buildingObject = parent;
                            break;
                            case "City": //NOI18N
                                cityObject = parent;                     
                            break;
                        }
                    }
                } catch (Exception exception) {
                    // Catch the exception generated in objects whose parents are not a inventory object
                    try {
                        warehouseObject = webserviceBean.getWarehouseToObject(
                                spareObject.getClassName(), 
                                spareObject.getId(), 
                                Page.getCurrent().getWebBrowser().getAddress(),
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                        if (warehouseObject != null) {

                            RemoteObjectLight physicalNode = webserviceBean.getPhysicalNodeToObjectInWarehouse(
                                    spareObject.getClassName(), 
                                    spareObject.getId(), 
                                    Page.getCurrent().getWebBrowser().getAddress(),
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                            if (physicalNode != null) {

                                List<RemoteObjectLight> parents = webserviceBean.getParentsUntilFirstOfClass(
                                        physicalNode.getClassName(),
                                        physicalNode.getId(),
                                        "City", //NOI18N
                                        Page.getCurrent().getWebBrowser().getAddress(),
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                                // A Building can be a physical Node then this is include to get a building
                                parents.add(physicalNode);

                                for (RemoteObjectLight parent : parents) {

                                    switch(parent.getClassName()) {
                                        case "Rack": //NOI18N
                                            rackObject = parent;
                                        break;
                                        case "Building": //NOI18N
                                            buildingObject = parent;
                                        break;
                                        case "City": //NOI18N
                                            cityObject = parent;                     
                                        break;
                                    }
                                }
                            }                       
                        }
                    } catch (ServerSideException ex) {
                        //Catch another unexpected exception and notify
                        Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    }
                }
            }
        }
        
        public RemoteObjectLight getRackObject() {
            return rackObject;
        }
        
        public String getName() {
            return spareObject != null && spareObject.getName() != null ? spareObject.getName() : "";
        }
        
        public String getVendor() {
            if (spareObject != null) {
                try {
                    String vendor = webserviceBean.getAttributeValueAsString(
                        spareObject.getClassName(), 
                        spareObject.getId(), 
                        "vendor", //NOI18N
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                    
                    return vendor != null ? vendor : "";
                } catch (ServerSideException ex) {

                }
            }
            return "";
        }
        
        public String getState() {
            if (spareObject != null) {
                try {
                    String state = webserviceBean.getAttributeValueAsString(
                        spareObject.getClassName(), 
                        spareObject.getId(), "state", //NOI18N
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                    
                    return state != null ? state : "";
                } catch (ServerSideException ex) {
                }
            }
            return "";
        }
        
        public String getPosition() {
            if (spareObject != null) {
                try {
                    String position = webserviceBean.getAttributeValueAsString(
                            spareObject.getClassName(), 
                            spareObject.getId(), 
                            "position", //NOI18N
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                    
                    return position != null ? position : "";
                } catch (ServerSideException ex) {

                }
            }
            return "";
        }
        
        public String getRackName() {
            return rackObject != null && rackObject.getName() != null ? rackObject.getName() : "";
        }
        
        public String getWarehouseName() {
            return warehouseObject != null && warehouseObject.getName() != null ? warehouseObject.getName() : "";
        }
        
        public String getBuildingName() {
            return buildingObject != null && buildingObject.getName() != null ? buildingObject.getName() : "";
        }
        
        public String getCityName() {
            return cityObject != null && cityObject.getName() != null ? cityObject.getName() : "";
        }
        
        public String getRackViewButtonCaption() {
        return "<span class=\"v-icon\" style=\"font-family: " //NOI18N
            + VaadinIcons.SERVER.getFontFamily() 
            + "\">&#x" //NOI18N
            + Integer.toHexString(VaadinIcons.SERVER.getCodepoint())
            + ";</span>"; //NOI18N
        }
    }
    
}
