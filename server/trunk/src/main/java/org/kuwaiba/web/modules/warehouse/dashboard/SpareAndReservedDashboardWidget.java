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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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

        lblCounter.setValue(0 + " results");

        final Button btnFirst = new Button();
        btnFirst.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnFirst.setDescription("First");
        btnFirst.setIcon(VaadinIcons.ANGLE_DOUBLE_LEFT, "First");

        final Button btnPrevious = new Button();
        btnPrevious.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnPrevious.setDescription("Previous");
        btnPrevious.setIcon(VaadinIcons.ANGLE_LEFT, "Previous");

        final Button btnNext = new Button();
        btnNext.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnNext.setDescription("Next");
        btnNext.setIcon(VaadinIcons.ANGLE_RIGHT, "Next");

        final Button btnLast = new Button();
        btnLast.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnLast.setDescription("Last");
        btnLast.setIcon(VaadinIcons.ANGLE_DOUBLE_RIGHT, "Last");

        ValueChangeListener<String> valueChangeListener = new ValueChangeListener<String>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<String> event) {
                List<ObjectBean> objBeans = getSpareBeans(
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
                if (!objBeans.isEmpty()) {
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
        
        addComponent(verticalLayout);
        
        this.contentComponent = verticalLayout;
    }
            
    private void filterRemoteObjectsByAttribute(String attrName, String filter, List<RemoteObjectLight> remoteObjects, List<ObjectBean> remoteObjectBeans) {
        RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
        
        if (filter != null && !filter.isEmpty()) {
            for (RemoteObjectLight remoteObject : remoteObjects) {
                try {
                    String state = webserviceBean.getAttributeValueAsString(remoteObject.getClassName(), remoteObject.getId(), attrName, remoteSession.getIpAddress(), remoteSession.getSessionId());
                    if (state != null) {
                        if (state.toUpperCase().contains(filter != null ? filter.toUpperCase() : "")) {
                            if (!contains(remoteObjectBeans, remoteObject)) {
                                remoteObjectBeans.add(new ObjectBean(remoteObject, webserviceBean));
                            }
                        }
                    }
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            }
        }
    }
    
    private void filterRemoteObjectsByParent(String parentClassName, String filter, List<RemoteObjectLight> remoteObjects, List<ObjectBean> remoteObjectBeans) {
        RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
        
        if (filter != null && !filter.isEmpty()) {
            for (RemoteObjectLight remoteObject : remoteObjects) {
                try {
                    RemoteObjectLight city = webserviceBean.getFirstParentOfClass(remoteObject.getClassName(), remoteObject.getId(), parentClassName, remoteSession.getIpAddress(), remoteSession.getSessionId());

                    if (city == null) {
                        RemoteObjectLight physicalNode = webserviceBean.getPhysicalNodeToObjectInWarehouse(remoteObject.getClassName(), remoteObject.getId(), remoteSession.getIpAddress(), remoteSession.getSessionId());
                        if (physicalNode != null && !physicalNode.getClassName().equals(parentClassName))
                            city = webserviceBean.getFirstParentOfClass(physicalNode.getClassName(), physicalNode.getId(), parentClassName, remoteSession.getIpAddress(), remoteSession.getSessionId());
                        else
                            city = physicalNode;
                    }
                    if (city != null && city.getName() != null && city.getName().toUpperCase().contains(filter != null ? filter.toUpperCase() : "")) {
                        if (!contains(remoteObjectBeans, remoteObject)) {
                            remoteObjectBeans.add(new ObjectBean(remoteObject, webserviceBean));
                        }
                    }

                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            }
        }
    }
    
    private List<ObjectBean> getSpareBeans(
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
        
        List<RemoteObjectLight> remoteObjects = getSpareAndReservedObjects();
        
        filterRemoteObjectsByAttribute("name", filterName, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByAttribute("vendor", filterVendor, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByAttribute("state", filterState, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByAttribute("position", filterPosition, remoteObjects, filteredItems); //NOI18N
        
        filterRemoteObjectsByParent("Rack", filterRack, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByParent("Warehouse", filterWarehouse, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByParent("VirtualWarehouse", filterWarehouse, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByParent("Room", filterRoom, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByParent("Building", filterBuilding, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByParent("City", filterCity, remoteObjects, filteredItems); //NOI18N
        filterRemoteObjectsByParent("Country", filterCountry, remoteObjects, filteredItems); //NOI18N
        
        return filteredItems;
    }
    
    private boolean contains(List<ObjectBean> objectBeanList, RemoteObjectLight remoteObject) {
        for (ObjectBean objectBean : objectBeanList) {
            if (objectBean.getSpareObject().getId().equals(remoteObject.getId()))
                return true;
        }
        return false;
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
        private final RemoteObjectLight spareObject;
        private RemoteObjectLight rackObject;
        private RemoteObjectLight warehouseObject;
        private RemoteObjectLight roomObject;
        private RemoteObjectLight buildingObject;
        private RemoteObjectLight cityObject;
        private RemoteObjectLight countryObject;
        
        private HashMap<String, String> attrValues;
                        
        public ObjectBean(RemoteObjectLight spareObject, WebserviceBean webserviceBean) {
            this.spareObject = spareObject;
                        
            if (spareObject != null && webserviceBean != null) {
                RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
                
                try {
                    List<RemoteObjectLight> parents = webserviceBean.getParentsUntilFirstOfClass(
                            spareObject.getClassName(),
                            spareObject.getId(),
                            "Country", //NOI18N
                            remoteSession.getIpAddress(),
                            remoteSession.getSessionId()); //NOI18N
                    if (parents != null && !parents.isEmpty() && parents.get(parents.size() - 1).getClassName().equals("Country")) {
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
                    else {
                        for (RemoteObjectLight parent : parents) {
                            if (parent.getClassName().equals("Warehouse") || 
                                parent.getClassName().equals("VirtualWarehouse")) {
                                
                                warehouseObject = parent;
                            }
                        }
                        if (warehouseObject != null) {

                            RemoteObjectLight physicalNode = webserviceBean.getPhysicalNodeToObjectInWarehouse(
                                    spareObject.getClassName(), 
                                    spareObject.getId(), 
                                    remoteSession.getIpAddress(),
                                    remoteSession.getSessionId()); //NOI18N

                            if (physicalNode != null) {

                                parents = webserviceBean.getParentsUntilFirstOfClass(
                                        physicalNode.getClassName(),
                                        physicalNode.getId(),
                                        "Country", //NOI18N
                                        remoteSession.getIpAddress(),
                                        remoteSession.getSessionId()); //NOI18N
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
                    }
                    attrValues = webserviceBean.getAttributeValuesAsString(
                        spareObject.getClassName(), spareObject.getId(), 
                        remoteSession.getIpAddress(), remoteSession.getSessionId());
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            }
        }
        
        public RemoteObjectLight getSpareObject() {
            return spareObject;
        }
                
        public RemoteObjectLight getRackObject() {
            return rackObject;
        }
        
        public String getName() {
            return spareObject != null && spareObject.getName() != null ? spareObject.getName() : "";
        }
        
        public String getVendor() {
            if (spareObject != null && attrValues != null) {
                String vendor = attrValues.get("vendor"); //NOI18N
                return vendor != null ? vendor : "";
            }
            return "";
        }
        
        public String getState() {
            if (spareObject != null && attrValues != null) {
                String state = attrValues.get("state"); //NOI18N
                return state != null ? state : "";
            }
            return "";
        }
        
        public String getPosition() {
            if (spareObject != null && attrValues != null) {
                String position = attrValues.get("position"); //NOI18N
                return position != null ? position : "";
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

        @Override
        public int hashCode() {
            return Objects.hashCode(this.spareObject);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ObjectBean other = (ObjectBean) obj;
            if (!Objects.equals(this.spareObject, other.spareObject)) {
                return false;
            }
            return true;
        }
    }
    
}