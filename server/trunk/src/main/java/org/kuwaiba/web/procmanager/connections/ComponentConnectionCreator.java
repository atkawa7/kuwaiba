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

import com.vaadin.event.LayoutEvents;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Optional;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.procmanager.rackview.ComponentDevice;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentConnectionCreator extends VerticalLayout {
    private WebserviceBean webserviceBean;
    
    public ComponentConnectionCreator(
        ComponentConnectionSource componentConnectionSource, WebserviceBean webserviceBean/*, 
        ComponentConnectionTarget componentConnectionTarget*/) {
        this.webserviceBean = webserviceBean;        
////        setWidth(100, Unit.PERCENTAGE);
////        setHeightUndefined();
        setSizeFull();
//        setSpacing(false);
        initializeComponent(componentConnectionSource/*, componentConnectionTarget*/);                        
    }
        
    private void initializeComponent(
        ComponentConnectionSource componentConnectionSource/*, 
        ComponentConnectionTarget componentConnectionTarget*/) {
        
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
                
        Panel leftPanel = new Panel("Devices");
                
        leftPanel.setContent(componentConnectionSource);
        
////        componentConnectionSource.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
////            @Override
////            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
////                if (componentConnectionSource.getSelectedSourceDevice() != null)
////                    Notification.show("aaa", Notification.Type.ERROR_MESSAGE);
////            }
////        });
                
        leftPanel.setSizeFull();
        
        Panel rightPanel = new Panel();
        
        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setSizeFull();
        
        rightPanel.setContent(rightVerticalLayout);
        rightPanel.setSizeFull();
        
        Panel topRightPanel = new Panel("Source Device");
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
                
        Panel bottomRightPanel = new Panel("Target Device");
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
                        
        horizontalLayout.addComponent(leftPanel);
        horizontalLayout.addComponent(rightPanel);
        
        horizontalLayout.setExpandRatio(leftPanel, 0.50f);
        horizontalLayout.setExpandRatio(rightPanel, 0.50f);
        
        
        addComponent(horizontalLayout);
        addComponent(verticalLayout);
        
        setExpandRatio(horizontalLayout, 0.95f);
        setExpandRatio(verticalLayout, 0.05f);
        
    }
}
