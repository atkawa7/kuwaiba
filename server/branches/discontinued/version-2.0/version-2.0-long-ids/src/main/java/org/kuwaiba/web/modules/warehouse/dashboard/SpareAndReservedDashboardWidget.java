/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
import com.vaadin.data.provider.DataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
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
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.procmanager.MiniAppRackView;
import org.vaadin.teemusa.gridextensions.paging.PagedDataProvider;
import org.vaadin.teemusa.gridextensions.paging.PagingControls;

/**
 * A widget that displays spare and reserved inventory objects and allows see its rack view
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SpareAndReservedDashboardWidget extends AbstractDashboardWidget {
    private final WebserviceBean webserviceBean;
    
    private final RemoteObjectLight selectedObject;
        
    public SpareAndReservedDashboardWidget(WebserviceBean webserviceBean) {
        super("Spare and Reserved");
        this.webserviceBean = webserviceBean;        
        selectedObject = null;
        
        setSizeFull();
        setSpacing(false);
        this.createContent();
    }
    
    public SpareAndReservedDashboardWidget(AbstractDashboard parentDashboard, RemoteObjectLight selectedObject, WebserviceBean webserviceBean) {
        super(String.format("Spare and Reserved in %s", selectedObject), parentDashboard);
        this.webserviceBean = webserviceBean;
        this.selectedObject = selectedObject;
        
        setSizeFull();
        setSpacing(false);
        this.createCover();
    }
    
    @Override
    public void createCover() { 
        VerticalLayout lytViewsWidgetCover = new VerticalLayout();
        Label lblText = new Label("Spare and Reserved");
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
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(false);
        verticalLayout.setSizeFull();
        Label lblCounter = new Label(); // Show the number of devices in the table
        lblCounter.addStyleName(ValoTheme.LABEL_HUGE);
        
        List<RemoteObjectLight> spareAndReservedObjects = getSpareAndReservedObjects();
        
        if (spareAndReservedObjects != null && !spareAndReservedObjects.isEmpty()) {
            
            List<ObjectBean> objectBeans = new ArrayList();
            
            for (RemoteObjectLight spareAndReservedObject : spareAndReservedObjects) {
                objectBeans.add(new ObjectBean(spareAndReservedObject, webserviceBean));
            }
            final String columnName = "name"; //NOI18N
            final String columnVendor = "vendor"; //NOI18N
            final String columnState = "state"; //NOI18N
            final String columnPosition = "position"; //NOI18N
            final String columnRack = "rack"; //NOI18N
            final String columnWarehouse = "warehouse"; //NOI18N
            final String columnRoom = "room"; //NOI18N
            final String columnBuilding = "building"; //NOI18N
            final String columnCity = "city"; //NOI18N
            final String columnCountry = "country"; //NOI18N
            
            ButtonRenderer buttonRenderer = new ButtonRenderer(new RendererClickListener<ObjectBean>() {

                @Override
                public void click(ClickableRenderer.RendererClickEvent<ObjectBean> event) {
                    ObjectBean processInstanceBean = (ObjectBean) event.getItem();
                    
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
                        Notification.show("The rack view can not be generated", Notification.Type.ERROR_MESSAGE);
                    }
                }
            });
            buttonRenderer.setHtmlContentAllowed(true);
                        
            Grid<ObjectBean> grid = new Grid();
            grid.setWidth(95, Unit.PERCENTAGE);
            grid.setHeight(600, Unit.PIXELS);            
            
            grid.addColumn(ObjectBean::getName).setCaption("Name").setId(columnName);
            grid.addColumn(ObjectBean::getVendor).setCaption("Vendor").setId(columnVendor);
            grid.addColumn(ObjectBean::getState).setWidth(80).setCaption("State").setId(columnState);
            grid.addColumn(ObjectBean::getPosition).setWidth(80).setCaption("Position").setId(columnPosition);
            grid.addColumn(ObjectBean::getRackName).setWidth(120).setCaption("Rack").setId(columnRack);
            grid.addColumn(ObjectBean::getRackViewButtonCaption, buttonRenderer).
                setMinimumWidth(50f).
                setMaximumWidth(50f).
                setDescriptionGenerator(e -> "<b>Rack View</b>", ContentMode.HTML);
            grid.addColumn(ObjectBean::getWarehouseName).setCaption("Warehouse").setId(columnWarehouse);
            grid.addColumn(ObjectBean::getRoomName).setCaption("Room").setId(columnRoom);
            grid.addColumn(ObjectBean::getBuildingName).setCaption("Building").setId(columnBuilding);
            grid.addColumn(ObjectBean::getCityName).setCaption("City").setId(columnCity);
            grid.addColumn(ObjectBean::getCountryName).setCaption("Country").setId(columnCountry);
            
            TextField txtName = new TextField();
            TextField txtVendor = new TextField();
            TextField txtState = new TextField();
            TextField txtPosition = new TextField();
            txtPosition.setWidth("40px");
            TextField txtRack = new TextField();
            txtRack.setWidth("60px");
            TextField txtWarehouse = new TextField();
            TextField txtRoom = new TextField();
            TextField txtBuilding = new TextField();
            TextField txtCity = new TextField();
            TextField txtCountry = new TextField();
            
            HeaderRow headerRow = grid.appendHeaderRow();
            
            headerRow.getCell(columnName).setComponent(txtName);
            headerRow.getCell(columnVendor).setComponent(txtVendor);
            headerRow.getCell(columnState).setComponent(txtState);
            headerRow.getCell(columnPosition).setComponent(txtPosition);
            headerRow.getCell(columnRack).setComponent(txtRack);            
            headerRow.getCell(columnWarehouse).setComponent(txtWarehouse);
            headerRow.getCell(columnRoom).setComponent(txtRoom);
            headerRow.getCell(columnBuilding).setComponent(txtBuilding);
            headerRow.getCell(columnCity).setComponent(txtCity);
            headerRow.getCell(columnCountry).setComponent(txtCountry);
            
            final VerticalLayout controls = new VerticalLayout();
            
            List<ObjectBean> objBeans = getSpareBeans(objectBeans.iterator(), 
                txtName.getValue(), 
                txtVendor.getValue(), 
                txtState.getValue(), 
                txtPosition.getValue(), 
                txtRack.getValue(), 
                txtWarehouse.getValue(),
                txtRoom.getValue(),
                txtBuilding.getValue(), 
                txtCity.getValue(),
                txtCountry.getValue());
            
            lblCounter.setValue(objBeans.size() + " results");
            
            PagedDataProvider<ObjectBean, SerializablePredicate<ObjectBean>> dataProvider = new PagedDataProvider<>(
                        DataProvider.ofCollection(objBeans));
            grid.setDataProvider(dataProvider);
            PagingControls pagingControls = dataProvider.getPagingControls();
            pagingControls.setPageLength(15);
            
            final Button btnFirst = new Button();
            btnFirst.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btnFirst.setDescription("First");
            btnFirst.setIcon(VaadinIcons.ANGLE_DOUBLE_LEFT, "First");
            btnFirst.addClickListener(e -> pagingControls.setPageNumber(0));
            
            final Button btnPrevious = new Button();
            btnPrevious.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btnPrevious.setDescription("Previous");
            btnPrevious.setIcon(VaadinIcons.ANGLE_LEFT, "Previous");
            btnPrevious.addClickListener(e -> pagingControls.previousPage());
            
            final Button btnNext = new Button();
            btnNext.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btnNext.setDescription("Next");
            btnNext.setIcon(VaadinIcons.ANGLE_RIGHT, "Next");
            btnNext.addClickListener(e -> pagingControls.nextPage());
            
            final Button btnLast = new Button();
            btnLast.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btnLast.setDescription("Last");
            btnLast.setIcon(VaadinIcons.ANGLE_DOUBLE_RIGHT, "Last");
            btnLast.addClickListener(e -> pagingControls.setPageNumber(pagingControls.getPageCount() - 1));
            
            ValueChangeListener<String> valueChangeListener = new ValueChangeListener<String>() {
                @Override
                public void valueChange(HasValue.ValueChangeEvent<String> event) {

                    if (objectBeans != null) {
                        List<ObjectBean> objBeans = getSpareBeans(objectBeans.iterator(), 
                            txtName.getValue(), 
                            txtVendor.getValue(), 
                            txtState.getValue(), 
                            txtPosition.getValue(), 
                            txtRack.getValue(), 
                            txtWarehouse.getValue(),
                            txtRoom.getValue(),
                            txtBuilding.getValue(), 
                            txtCity.getValue(),
                            txtCountry.getValue());
                        
                        lblCounter.setValue(objBeans.size() + " results");
                        
                        PagedDataProvider<ObjectBean, SerializablePredicate<ObjectBean>> dataProvider = new PagedDataProvider<>(
                                    DataProvider.ofCollection(objBeans));
                        grid.setDataProvider(dataProvider);
                        PagingControls pagingControls = dataProvider.getPagingControls();
                        pagingControls.setPageLength(15);
                        
                        btnFirst.addClickListener(e -> pagingControls.setPageNumber(0));
                        btnPrevious.addClickListener(e -> pagingControls.previousPage());
                        btnNext.addClickListener(e -> pagingControls.nextPage());
                        btnLast.addClickListener(e -> pagingControls.setPageNumber(pagingControls.getPageCount() - 1));
                    }
                }
            };
            txtName.addValueChangeListener(valueChangeListener);
            txtVendor.addValueChangeListener(valueChangeListener);
            txtState.addValueChangeListener(valueChangeListener);
            txtPosition.addValueChangeListener(valueChangeListener);
            txtRack.addValueChangeListener(valueChangeListener);
            txtWarehouse.addValueChangeListener(valueChangeListener);
            txtRoom.addValueChangeListener(valueChangeListener);
            txtBuilding.addValueChangeListener(valueChangeListener);
            txtCity.addValueChangeListener(valueChangeListener);
            txtCountry.addValueChangeListener(valueChangeListener);
            
            HorizontalLayout pages = new HorizontalLayout();
            pages.addComponent(btnFirst);
            pages.addComponent(btnPrevious);
            pages.addComponent(btnNext);
            pages.addComponent(btnLast);
            
            controls.addComponents(pages);
            controls.setComponentAlignment(pages, Alignment.MIDDLE_CENTER);
            controls.setSizeFull();
            
            VerticalLayout vly = new VerticalLayout();
            vly.addComponent(lblCounter);
            vly.setComponentAlignment(lblCounter, Alignment.MIDDLE_CENTER);
            vly.setSizeFull();
            
            verticalLayout.addComponent(vly);
            verticalLayout.addComponent(grid);
            verticalLayout.addComponent(controls);
            verticalLayout.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);
            verticalLayout.setComponentAlignment(controls, Alignment.MIDDLE_CENTER);
            verticalLayout.setExpandRatio(vly, 0.2f);
            verticalLayout.setExpandRatio(grid, 0.7f);
            verticalLayout.setExpandRatio(controls, 0.1f);
        }
        addComponent(verticalLayout);
                        
        this.contentComponent = verticalLayout;
    }
    
    private List<ObjectBean> getSpareBeans(Iterator<ObjectBean> iterator, 
        String filterName, 
        String filterVendor, 
        String filterState, 
        String filterPosition, 
        String filterRack, 
        String filterWarehouse, 
        String filterRoom,
        String filterBuilding, 
        String filterCity, 
        String filterCountry) {                
        
        List<ObjectBean> filteredItems = new ArrayList();

        while (iterator.hasNext()) {
            ObjectBean spereBean = iterator.next();
            
            boolean flagName = spereBean.getName().toUpperCase().contains(filterName != null ? filterName.toUpperCase() : "");
            boolean flagVendor = spereBean.getVendor().toUpperCase().contains(filterVendor != null ? filterVendor.toUpperCase() : "");
            boolean flagState = spereBean.getState().toUpperCase().contains(filterState != null ? filterState.toUpperCase() : "");
            boolean flagPosition = spereBean.getPosition().toUpperCase().contains(filterPosition != null ? filterPosition.toUpperCase() : "");
            boolean flagRack = spereBean.getRackName().toUpperCase().contains(filterRack != null ? filterRack.toUpperCase() : "");
            boolean flagWarehouse = spereBean.getWarehouseName().toUpperCase().contains(filterWarehouse != null ? filterWarehouse.toUpperCase() : "");
            boolean flagRoom = spereBean.getRoomName().toUpperCase().contains(filterRoom != null ? filterRoom.toUpperCase() : "");
            boolean flagBuilding = spereBean.getBuildingName().toUpperCase().contains(filterBuilding != null ? filterBuilding.toUpperCase() : "");
            boolean flagCity = spereBean.getCityName().toUpperCase().contains(filterCity != null ? filterCity.toUpperCase() : "");
            boolean flagCountry = spereBean.getCountryName().toUpperCase().contains(filterCountry != null ? filterCountry.toUpperCase() : "");
            
            if (flagName && flagVendor && flagState && flagPosition && flagRack && flagWarehouse && flagRoom && flagBuilding && flagCity && flagCountry)
                filteredItems.add(spereBean);
        }
        return filteredItems;
    }
        
    private List<RemoteObjectLight> getSpareAndReservedObjects() {
        RemoteSession remoteSession = ((RemoteSession) UI.getCurrent().getSession().getAttribute("session"));
        List<RemoteObjectLight> result = new ArrayList();
                
        if (selectedObject != null) {
            List<RemoteObjectLight> children = getObjectChildrenRecursive(selectedObject);

            for (RemoteObjectLight child : children) {
                try {
                    String attributeValue = webserviceBean.getAttributeValueAsString(
                        child.getClassName(),
                        child.getId(), "state", //NOI18N
                        remoteSession.getIpAddress(),
                        remoteSession.getSessionId()); //NOI18N

                    if (attributeValue != null) {
                        if (webserviceBean.isSubclassOf(child.getClassName(), "ConfigurationItem", remoteSession.getIpAddress(), remoteSession.getSessionId()) || 
                            webserviceBean.isSubclassOf(child.getClassName(), "GenericPhysicalLink", remoteSession.getIpAddress(), remoteSession.getSessionId()))
                            result.add(child);
                    }
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            }
            return result;
        }
        else {
            try {
                List<RemoteObjectLight> operationalStates = webserviceBean.getListTypeItems(
                    "OperationalState", //NOI18N
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                for (RemoteObjectLight operationalState : operationalStates) {
                    
                    List<RemoteObjectLight> objects = webserviceBean.getListTypeItemUses(
                        operationalState.getClassName(), operationalState.getId(), -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
                    for (RemoteObjectLight object : objects) {
                        if (webserviceBean.isSubclassOf(object.getClassName(), "ConfigurationItem", remoteSession.getIpAddress(), remoteSession.getSessionId()) || 
                            webserviceBean.isSubclassOf(object.getClassName(), "GenericPhysicalLink", remoteSession.getIpAddress(), remoteSession.getSessionId()))
                            result.add(object);
                    }
                    
                }
                return result;
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }
        return null;
    }
    
    private List<RemoteObjectLight> getObjectChildrenRecursive(RemoteObjectLight parent) {
        List<RemoteObjectLight> result = new ArrayList();
        
        if (parent != null) {
            try {
                List<RemoteObjectLight> children = webserviceBean.getObjectChildren(parent.getClassName(), parent.getId(), -1,
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
                if (children != null && !children.isEmpty()) {
                    
                    result.addAll(children);

                    for (RemoteObjectLight child : children)
                        result.addAll(getObjectChildrenRecursive(child));
                }
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
        }
        return result;                                                        
    }
    
    private class ObjectBean {
        private final WebserviceBean webserviceBean;
        private final RemoteObjectLight spareObject;
        private RemoteObjectLight rackObject;
        private RemoteObjectLight warehouseObject;
        private RemoteObjectLight roomObject;
        private RemoteObjectLight buildingObject;
        private RemoteObjectLight cityObject;
        private RemoteObjectLight countryObject;
                        
        public ObjectBean(RemoteObjectLight spareObject, WebserviceBean webserviceBean) {
            this.spareObject = spareObject;
            this.webserviceBean = webserviceBean;
            
            if (spareObject != null && webserviceBean != null) {
                
                try {
                    List<RemoteObjectLight> parents = webserviceBean.getParentsUntilFirstOfClass(
                            spareObject.getClassName(),
                            spareObject.getId(),
                            "Country", //NOI18N
                            Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N

                    for (RemoteObjectLight parent : parents) {

                        switch(parent.getClassName()) {
                            case "Rack": //NOI18N
                                rackObject = parent;
                            break;
                            case "Room": //NOI18N
                                roomObject = parent;                                
                            break;
                            case "Building": //NOI18N
                                buildingObject = parent;
                            break;
                            case "City": //NOI18N
                                cityObject = parent;                     
                            break;    
                            case "Country": //NOI18N
                                countryObject = parent;
                            break;
                        }
                    }
                } catch (Exception exception) {
                }
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
                                    "Country", //NOI18N
                                    Page.getCurrent().getWebBrowser().getAddress(),
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                            // A Building can be a physical Node then this is include to get a building
                            parents.add(physicalNode);

                            for (RemoteObjectLight parent : parents) {

                                switch(parent.getClassName()) {
                                    case "Rack": //NOI18N
                                        rackObject = parent;
                                    break;
                                    case "Room": //NOI18N
                                        roomObject = parent;                                
                                    break;
                                    case "Building": //NOI18N
                                        buildingObject = parent;
                                    break;
                                    case "City": //NOI18N
                                        cityObject = parent;                     
                                    break;
                                    case "Country": //NOI18N
                                        countryObject = parent;
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
        
        public String getRoomName() {
            return roomObject != null && roomObject.getName() != null ? roomObject.getName() : "";
        }
        
        public String getBuildingName() {
            return buildingObject != null && buildingObject.getName() != null ? buildingObject.getName() : "";
        }
        
        public String getCityName() {
            return cityObject != null && cityObject.getName() != null ? cityObject.getName() : "";
        }
        
        public String getCountryName() {
             return countryObject != null && countryObject.getName() != null ? countryObject.getName() : "";
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