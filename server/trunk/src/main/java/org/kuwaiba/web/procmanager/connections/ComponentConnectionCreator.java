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
package org.kuwaiba.web.procmanager.connections;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.procmanager.DynamicComponent;
import org.kuwaiba.web.procmanager.rackview.ComponentDevice;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentConnectionCreator extends VerticalLayout {
    private final WebserviceBean webserviceBean;
    
    public ComponentConnectionCreator(
        ComponentConnectionSource componentConnectionSource, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;        
        setSizeFull();
        initializeComponent(componentConnectionSource);                        
    }
    
    @Override
    public final void setSizeFull() {
        super.setSizeFull();
    }
        
    private void initializeComponent(ComponentConnectionSource componentConnectionSource) {
        List<RemoteObject> deviceList = componentConnectionSource.getDeviceList();
        
        VerticalLayout verticalLayout = new VerticalLayout();
        
        Button btnConnect = new Button("Connect");
        btnConnect.setIcon(VaadinIcons.PLUG);
        btnConnect.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnConnect.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
        btnConnect.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                
                RemoteObject selectedSourceDevice = componentConnectionSource.getSelectedSourceDevice();
                RemoteObjectLight selectedTargetDevice = null;//componentConnectionTarget.getSelectedTargetDevice();
                
////                if (selectedSourceDevice != null && selectedSourceDevice != null) {
////                    return;
////                }
////                if (selectedTargetDevice == null) {
////                    Notification.show("Select Target Device", Notification.Type.ERROR_MESSAGE);
////                }
////                if (selectedSourceDevice == null) {
////                    Notification.show("Select Source Device", Notification.Type.ERROR_MESSAGE);
////                }
            }
        } );
        verticalLayout.addComponent(btnConnect);
        verticalLayout.setComponentAlignment(btnConnect, Alignment.MIDDLE_CENTER);
                
        HorizontalLayout horizontalLayout = new HorizontalLayout();        
        horizontalLayout.setSpacing(false);
        
        horizontalLayout.setSizeFull();
                
////        Panel leftPanel = new Panel("Devices");
                
////        leftPanel.setContent(componentConnectionSource);
        
////        componentConnectionSource.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
////            @Override
////            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
////                if (componentConnectionSource.getSelectedSourceDevice() != null)
////                    Notification.show("aaa", Notification.Type.ERROR_MESSAGE);
////            }
////        });
                
////        leftPanel.setSizeFull();
        
        Panel rightPanel = new Panel();
        
        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setSizeFull();
        
        rightPanel.setContent(rightVerticalLayout);
        rightPanel.setSizeFull();
        
        Panel topRightPanel = new Panel();
        topRightPanel.setSizeFull();
        
        DropTargetExtension<Panel> topDropTarget = new DropTargetExtension<>(topRightPanel);
        topDropTarget.setDropEffect(DropEffect.MOVE);

        topDropTarget.addDropListener(new DropListener<Panel>() {
            @Override
            public void drop(DropEvent<Panel> event) {
                
                Optional<AbstractComponent> dragSource = event.getDragSourceComponent();

                if (dragSource.isPresent() &&  dragSource.get() instanceof ComponentDevice) {
                    
                    topRightPanel.setContent(new ComponentConnectionTarget(
                        ((ComponentDevice) dragSource.get()).getDevice(), 
                        webserviceBean));
                }
            }
        });        
        topRightPanel.setContent(new ComponentConnectionTarget(null, webserviceBean));
                
        Panel bottomRightPanel = new Panel();
        bottomRightPanel.setSizeFull();
        
        DropTargetExtension<Panel> bottomDropTarget = new DropTargetExtension<>(bottomRightPanel);
        bottomDropTarget.setDropEffect(DropEffect.MOVE);

        bottomDropTarget.addDropListener(new DropListener<Panel>() {
            @Override
            public void drop(DropEvent<Panel> event) {
                
                Optional<AbstractComponent> dragSource = event.getDragSourceComponent();

                if (dragSource.isPresent() &&  dragSource.get() instanceof ComponentDevice) {
                    bottomRightPanel.setContent(new ComponentConnectionTarget(
                        ((ComponentDevice) dragSource.get()).getDevice(), 
                        webserviceBean));
                }
            }
        });
        bottomRightPanel.setContent(new ComponentConnectionTarget(null, webserviceBean));
        
        rightVerticalLayout.addComponent(topRightPanel);
        rightVerticalLayout.addComponent(bottomRightPanel);
        
        rightVerticalLayout.setExpandRatio(topRightPanel, 0.50f);
        rightVerticalLayout.setExpandRatio(bottomRightPanel, 0.50f);
                        
////        horizontalLayout.addComponent(leftPanel);
////        horizontalLayout.addComponent(rightPanel);

////        horizontalLayout.setExpandRatio(leftPanel, 0.50f);
////        horizontalLayout.setExpandRatio(rightPanel, 0.50f);
        //Start Connections
        Panel pnl = new Panel("Connections");
        pnl.setSizeFull();
        
        VerticalLayout lytConnection = new VerticalLayout();
        lytConnection.setWidth(100, Unit.PERCENTAGE);
        lytConnection.setHeightUndefined();
        
        Button btnAddEndpoints = new Button("Add Enpoints");
        
        lytConnection.addComponent(btnAddEndpoints);
        lytConnection.setComponentAlignment(btnAddEndpoints, Alignment.MIDDLE_CENTER);
        
        Grid<EndpointBean> grdEndpoints = new Grid();
        
        grdEndpoints.addColumn(EndpointBean::getEndpointA).setCaption("Endpoint A");
        grdEndpoints.addColumn(EndpointBean::getEndpointB).setCaption("Endpoint B");
        
        btnAddEndpoints.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                List<RemoteObjectLight> endpoitsA = ((ComponentConnectionTarget) topRightPanel.getContent()).getSelectItems();
                List<RemoteObjectLight> endpoitsB = ((ComponentConnectionTarget) bottomRightPanel.getContent()).getSelectItems();
                
                if (endpoitsA.size() == endpoitsB.size()) {
                
                    int size = endpoitsA.size();

                    List<EndpointBean> endpoints = new ArrayList();

                    for (int i = 0; i < size; i += 1) {
                        endpoints.add(new EndpointBean(endpoitsA.get(i), endpoitsB.get(i)));
                    }
                    grdEndpoints.setItems(endpoints);
                } 
                else {
                    Notification.show("The endpoints no has the same size", Notification.Type.ERROR_MESSAGE);
                }
            }
        });
        
        lytConnection.addComponent(grdEndpoints);
        lytConnection.setComponentAlignment(grdEndpoints, Alignment.MIDDLE_CENTER);
                        
        pnl.setContent(lytConnection);
        //End Connections
        
        
        
        topRightPanel.addStyleName(ValoTheme.PANEL_WELL);
        bottomRightPanel.addStyleName(ValoTheme.PANEL_WELL);
        // Source Side
        VerticalLayout sourceLayout = new VerticalLayout();
        sourceLayout.setSizeFull();
        
        Panel pnlSourceDevices = new Panel("Select a device to set the Endpoint A");
        pnlSourceDevices.setSizeFull();
        
        VerticalLayout vlySourceDevices = new VerticalLayout();
        vlySourceDevices.setWidth(100, Unit.PERCENTAGE);
        vlySourceDevices.setHeightUndefined();
        
        for (RemoteObject device : deviceList) {
            vlySourceDevices.addComponent(new Label(device.toString()));
        }
        pnlSourceDevices.setContent(vlySourceDevices);
                
        Button btnResetSource = new Button("Reset Tree");
        btnResetSource.setIcon(VaadinIcons.REPLY);
        
        sourceLayout.addComponent(pnlSourceDevices);
        sourceLayout.addComponent(btnResetSource);
        sourceLayout.addComponent(topRightPanel);
                
        sourceLayout.setExpandRatio(pnlSourceDevices, 0.30f);
        sourceLayout.setExpandRatio(topRightPanel, 0.65f);
        sourceLayout.setExpandRatio(btnResetSource, 0.05f);
                
        sourceLayout.setComponentAlignment(pnlSourceDevices, Alignment.MIDDLE_CENTER);
        sourceLayout.setComponentAlignment(topRightPanel, Alignment.MIDDLE_CENTER);
        sourceLayout.setComponentAlignment(btnResetSource, Alignment.MIDDLE_RIGHT);
        // Target Side
        VerticalLayout targetLayout = new VerticalLayout();
        targetLayout.setSizeFull();
        
        Panel pnlTargetDevices = new Panel("Select a device to set the Endpoint B");
        pnlTargetDevices.setSizeFull();
        
        VerticalLayout vlyTargetDevices = new VerticalLayout();
        vlyTargetDevices.setWidth(100, Unit.PERCENTAGE);
        vlyTargetDevices.setHeightUndefined();
        
        for (RemoteObject device : deviceList) {
            vlyTargetDevices.addComponent(new Label(device.toString()));
        }
        pnlTargetDevices.setContent(vlyTargetDevices);
        
        Button btnResetTarget = new Button("Reset Tree");
        btnResetTarget.setIcon(VaadinIcons.REPLY);
        
        targetLayout.addComponent(pnlTargetDevices);
        targetLayout.addComponent(btnResetTarget);
        targetLayout.addComponent(bottomRightPanel);
                
        targetLayout.setExpandRatio(pnlTargetDevices, 0.30f);
        targetLayout.setExpandRatio(bottomRightPanel, 0.65f);
        targetLayout.setExpandRatio(btnResetTarget, 0.05f);
        
        targetLayout.setComponentAlignment(pnlTargetDevices, Alignment.MIDDLE_CENTER);
        targetLayout.setComponentAlignment(bottomRightPanel, Alignment.MIDDLE_CENTER);
        targetLayout.setComponentAlignment(btnResetTarget, Alignment.MIDDLE_RIGHT);
////        horizontalLayout.addComponent(leftPanel);
        horizontalLayout.addComponent(sourceLayout);
        horizontalLayout.addComponent(pnl);
        horizontalLayout.addComponent(targetLayout);        
        
////        horizontalLayout.setExpandRatio(leftPanel, 0.30f);        
        horizontalLayout.setExpandRatio(sourceLayout, 0.30f);
        horizontalLayout.setExpandRatio(pnl, 0.40f);        
        horizontalLayout.setExpandRatio(targetLayout, 0.30f);
        
////        DynamicComponent dynamicComponent = new DynamicComponent(leftPanel, horizontalLayout);
                
        addComponent(horizontalLayout);
        addComponent(verticalLayout);
        
        setExpandRatio(horizontalLayout, 0.95f);
        setExpandRatio(verticalLayout, 0.05f);
        
    }
}