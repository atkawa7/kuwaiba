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
package org.kuwaiba.web.procmanager.rackview;

import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Collections;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentRackSelector extends VerticalLayout {
    private final WebserviceBean webserviceBean;
    
    public ComponentRackSelector(ComponentDeviceList componentDeviceList, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;
        initializeComponent(componentDeviceList, webserviceBean);                        
    }
    
    private List<RemoteObjectLight> getItems(String parentClassName, String parentId, String childClassName) {
        try {
            return webserviceBean.getChildrenOfClassLightRecursive(
                parentId, 
                parentClassName, 
                childClassName, 
                0, 
                Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
////        try {
////            List<StringPair> scriptQueryParameters = new ArrayList();
////            scriptQueryParameters.add(new StringPair("parentId", String.valueOf(parentId)));
////            scriptQueryParameters.add(new StringPair("parentClassName", parentClassName));
////            scriptQueryParameters.add(new StringPair("childClassName", childClassName));
////            
////            webserviceBean.updateScriptQueryParameters(
////                    "getObjectChildrenRecursive",
////                    scriptQueryParameters,
////                    Page.getCurrent().getWebBrowser().getAddress(),
////                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
////            
////            RemoteScriptQueryResultCollection result = webserviceBean.executeScriptQueryCollection(
////                    "getObjectChildrenRecursive",
////                    Page.getCurrent().getWebBrowser().getAddress(),
////                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
////            
////            return (List<RemoteObjectLight>) result.getResults();
////            
////        } catch (ServerSideException ex) {
////            Notifications.showError(ex.getMessage());
////        }
        return null;
    }
        
    private void initializeComponent(ComponentDeviceList componentDeviceList, WebserviceBean webserviceBean) {
        setSizeFull();
        
        HorizontalLayout horizontalLayout = new HorizontalLayout();        
        horizontalLayout.setSpacing(false);
        
        horizontalLayout.setSizeFull();
                
        Panel leftPanel = new Panel("Devices");
                
        leftPanel.setContent(componentDeviceList);
                
        leftPanel.setSizeFull();
        
        Panel rightPanel = new Panel();
        
        Label lblCity = new Label("City");
        lblCity.addStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblBuilding = new Label("Building");
        lblBuilding.addStyleName(ValoTheme.LABEL_BOLD);
        
        Label lblRack = new Label("Rack");
        lblRack.addStyleName(ValoTheme.LABEL_BOLD);
        
        ComboBox<RemoteObjectLight> cmbCity = new ComboBox();
        ComboBox<RemoteObjectLight> cmbBuildings = new ComboBox();        
        ComboBox<RemoteObjectLight> cmbRacks = new ComboBox();
        
        ItemCaptionGenerator itemCaptionGenerator = new ItemCaptionGenerator<RemoteObjectLight>() {
                
            @Override
            public String apply(RemoteObjectLight item) {
                return item != null ? item.getName() : null;
            }
        };
        cmbCity.setItemCaptionGenerator(itemCaptionGenerator);
        cmbBuildings.setItemCaptionGenerator(itemCaptionGenerator);
        cmbRacks.setItemCaptionGenerator(itemCaptionGenerator);
                
        Button btnCreateBuilding = new Button();
        btnCreateBuilding.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnCreateBuilding.setIcon(VaadinIcons.PLUS);
        btnCreateBuilding.setDescription("Create Building");
        
        Button btnCreateRack = new Button();
        btnCreateRack.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btnCreateRack.setIcon(VaadinIcons.PLUS);
        btnCreateRack.setDescription("Create Rack");
        
        cmbBuildings.setEnabled(false);
        btnCreateBuilding.setEnabled(false);
        cmbRacks.setEnabled(false);
        btnCreateRack.setEnabled(false);
        
        btnCreateBuilding.addClickListener(new ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Window window = new Window();                
                window.setCaptionAsHtml(true);
                window.setCaption("<b>Create Building</b>");
                window.setDraggable(true);
                window.setModal(true);
                window.center();
                
                VerticalLayout content = new VerticalLayout();                
                content.setWidth(720, Unit.PIXELS);
                content.setHeightUndefined();
                content.setSpacing(true);
                
                GridLayout lytGrid = new GridLayout();
                lytGrid.setSpacing(true);
                lytGrid.setRows(2);
                lytGrid.setColumns(2);
                
                HorizontalLayout lytHorizontal = new HorizontalLayout();
                                
                content.addComponent(lytGrid);
                content.addComponent(lytHorizontal);
                
                content.setComponentAlignment(lytGrid, Alignment.MIDDLE_CENTER);
                content.setComponentAlignment(lytHorizontal, Alignment.MIDDLE_CENTER);
                
                TextField txtNewBuildingName = new TextField();
                
                lytGrid.addComponent(new Label("City"));
                lytGrid.addComponent(new Label(cmbCity != null && cmbCity.getValue() != null ? "<b>" + cmbCity.getValue().getName() + "</b>" : "", ContentMode.HTML));
                lytGrid.addComponent(new Label("New Bulding Name"));
                lytGrid.addComponent(txtNewBuildingName);
                
                Button btnOk = new Button("OK");
                btnOk.setWidth(120, Unit.PIXELS);
                
                Button btnCancel = new Button("Cancel");
                btnCancel.setWidth(120, Unit.PIXELS);
                
                lytHorizontal.addComponent(btnOk);                
                lytHorizontal.addComponent(btnCancel);
                
                btnOk.setEnabled(false);
                
                txtNewBuildingName.addValueChangeListener(new ValueChangeListener<String>() {
                    @Override
                    public void valueChange(HasValue.ValueChangeEvent<String> event) {
                        if (txtNewBuildingName.getValue() != null && !txtNewBuildingName.getValue().isEmpty())
                            btnOk.setEnabled(true);
                        else
                            btnOk.setEnabled(false);
                    }
                });
                
                btnOk.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        if (cmbCity != null && cmbCity.getValue() != null &&
                            txtNewBuildingName != null && txtNewBuildingName.getValue() != null && !txtNewBuildingName.getValue().equals("")) {
                                                                                    
                            RemoteObjectLight rol = cmbCity.getValue();
                            String name = txtNewBuildingName.getValue();
                                                        
                            try {
                                webserviceBean.createObject("Building", rol.getClassName(), rol.getId(), new String[] {"name"}, new String[] {name}, null, 
                                    Page.getCurrent().getWebBrowser().getAddress(), 
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getMessage());
                            }
                        }
                        window.close();
                    }
                });
                                    
                btnCancel.addClickListener(new ClickListener(){
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        window.close();
                    }
                });
                window.setContent(content);
                
                UI.getCurrent().addWindow(window);
            }
        });
        
        btnCreateRack.addClickListener(new ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Window window = new Window();                
                window.setCaptionAsHtml(true);
                window.setCaption("<b>Create Rack</b>");
                window.setDraggable(true);
                window.setModal(true);
                window.center();
                
                VerticalLayout content = new VerticalLayout();                
                content.setWidth(720, Unit.PIXELS);
                content.setHeightUndefined();
                content.setSpacing(true);
                
                GridLayout lytGrid = new GridLayout();
                lytGrid.setSpacing(true);
                lytGrid.setRows(3);
                lytGrid.setColumns(2);
                
                HorizontalLayout lytHorizontal = new HorizontalLayout();
                                
                content.addComponent(lytGrid);
                content.addComponent(lytHorizontal);
                
                content.setComponentAlignment(lytGrid, Alignment.MIDDLE_CENTER);
                content.setComponentAlignment(lytHorizontal, Alignment.MIDDLE_CENTER);
                
                TextField txtNewRackName = new TextField();
                TextField txtRackNameUnits = new TextField();
                
                lytGrid.addComponent(new Label("Building"));
                lytGrid.addComponent(new Label(cmbBuildings != null && cmbBuildings.getValue() != null ? "<b>" + cmbBuildings.getValue().getName() + "</b>" : "", ContentMode.HTML));
                lytGrid.addComponent(new Label("New Rack Name"));
                lytGrid.addComponent(txtNewRackName);
                lytGrid.addComponent(new Label("Rack Units"));
                lytGrid.addComponent(txtRackNameUnits);
                
                Button btnOk = new Button("OK");
                btnOk.setWidth(120, Unit.PIXELS);
                
                Button btnCancel = new Button("Cancel");
                btnCancel.setWidth(120, Unit.PIXELS);
                
                lytHorizontal.addComponent(btnOk);                
                lytHorizontal.addComponent(btnCancel);
                
                btnOk.setEnabled(false);
                
                ValueChangeListener valueChangeListener = new ValueChangeListener<String>() {
                    @Override
                    public void valueChange(HasValue.ValueChangeEvent<String> event) {
                        if (txtNewRackName.getValue() != null && !txtNewRackName.getValue().isEmpty() &&
                            txtRackNameUnits.getValue() != null && !txtRackNameUnits.getValue().isEmpty())
                            btnOk.setEnabled(true);
                        else
                            btnOk.setEnabled(false);
                    }
                };
                
                txtNewRackName.addValueChangeListener(valueChangeListener);
                txtRackNameUnits.addValueChangeListener(valueChangeListener);
                
                btnOk.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        if (cmbBuildings != null && cmbBuildings.getValue() != null &&
                            txtNewRackName != null && txtNewRackName.getValue() != null && !txtNewRackName.getValue().equals("")) {
                                                                                    
                            RemoteObjectLight rol = cmbBuildings.getValue();
                            String name = txtNewRackName.getValue();
                            String rackUnits = txtRackNameUnits.getValue();
                            
                            try {
                                webserviceBean.createObject("Rack", rol.getClassName(), rol.getId(), new String[] {"name", "rackUnits"}, new String[] {name, rackUnits}, null, 
                                    Page.getCurrent().getWebBrowser().getAddress(), 
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getMessage());
                            }
                        }
                        window.close();
                    }
                });
                                    
                btnCancel.addClickListener(new ClickListener(){
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        window.close();
                    }
                });
                window.setContent(content);
                
                UI.getCurrent().addWindow(window);
            }
        });
        
        cmbCity.addValueChangeListener(new HasValue.ValueChangeListener<RemoteObjectLight>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<RemoteObjectLight> event) {
                if (event.getValue() != null) {
                    cmbBuildings.setEnabled(true);
                    btnCreateBuilding.setEnabled(true);
                }
                else {
                    cmbBuildings.setEnabled(false);
                    btnCreateBuilding.setEnabled(false);
                }
            }
        });
        cmbCity.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                try {
                    
                    List<RemoteObjectLight> cities = webserviceBean.getObjectsOfClassLight("City", -1, 
                        Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                    Collections.sort(cities);
                    cmbCity.setItems(cities);
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            }
        });  
        
        cmbBuildings.addValueChangeListener(new HasValue.ValueChangeListener<RemoteObjectLight>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<RemoteObjectLight> event) {
                
                if (event.getValue() != null) {
                    cmbRacks.setEnabled(true);
                    btnCreateRack.setEnabled(true);
                }
                else {
                    cmbRacks.setEnabled(false);
                    btnCreateRack.setEnabled(false);
                }
            }
        });
        cmbBuildings.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                if (cmbCity != null && cmbCity.getValue() != null && cmbBuildings != null) {
                    RemoteObjectLight city = cmbCity.getValue();
                    
                    List<RemoteObjectLight> buildings = getItems(city.getClassName(), city.getId(), "Building"); //NOI18N
                    Collections.sort(buildings);
                    cmbBuildings.setItems(buildings);
                }
            }
        });
        
        cmbRacks.addValueChangeListener(new HasValue.ValueChangeListener<RemoteObjectLight>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<RemoteObjectLight> event) {
                
                if (event.getValue() != null && webserviceBean != null)
                    rightPanel.setContent(new ComponentRackView(event.getValue(), webserviceBean));
            }
        });        
        cmbRacks.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                if (cmbBuildings != null && cmbBuildings.getValue() != null) {
                    RemoteObjectLight building = cmbBuildings.getValue();
                    List<RemoteObjectLight> racks = getItems(building.getClassName(), building.getId(), "Rack"); //NOI18N
                    Collections.sort(racks);
                    cmbRacks.setItems(racks);
                }
            }
        });
        
        rightPanel.setSizeFull();
        
        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setSizeFull();
        
        GridLayout grdRack = new GridLayout();
        grdRack.setSizeFull();
        
        grdRack.setRows(1);
        grdRack.setColumns(8);
        grdRack.addComponent(lblCity);
        grdRack.addComponent(cmbCity);
        grdRack.addComponent(lblBuilding);
        grdRack.addComponent(cmbBuildings);
        grdRack.addComponent(btnCreateBuilding);
        grdRack.addComponent(lblRack);
        grdRack.addComponent(cmbRacks);
        grdRack.addComponent(btnCreateRack);
        
        grdRack.setComponentAlignment(lblCity, Alignment.MIDDLE_CENTER);
        grdRack.setComponentAlignment(lblBuilding, Alignment.MIDDLE_CENTER);
        grdRack.setComponentAlignment(lblRack, Alignment.MIDDLE_CENTER);
                
        rightVerticalLayout.addComponent(grdRack);
        rightVerticalLayout.addComponent(rightPanel);
        
        rightVerticalLayout.setExpandRatio(grdRack, 0.05f);
        rightVerticalLayout.setExpandRatio(rightPanel, 0.95f);
        
        rightVerticalLayout.setComponentAlignment(grdRack, Alignment.MIDDLE_CENTER);
                                        
        horizontalLayout.addComponent(leftPanel);
        horizontalLayout.addComponent(rightVerticalLayout);
        
        horizontalLayout.setExpandRatio(leftPanel, 0.40f);
        horizontalLayout.setExpandRatio(rightVerticalLayout, 0.60f);
                        
        addComponent(horizontalLayout);
    }   
}
