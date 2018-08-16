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
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.themes.ValoTheme;
import java.util.List;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.web.procmanager.rackview.ComponentDevice;
import org.kuwaiba.web.procmanager.rackview.ComponentRackView;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentConnectionSource extends VerticalLayout {
    private final WebserviceBean webserviceBean;
    private RemoteObject selectedSourceDevice;
    
    public ComponentConnectionSource(List<RemoteObject> deviceList, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;
        
        setWidth(100, Unit.PERCENTAGE);
        setHeightUndefined();
        setStyleName("selector");
        initializeComponent(deviceList);
    }   
    
    public RemoteObject getSelectedSourceDevice() {
        return selectedSourceDevice;
    }
    
    private void initializeComponent(List<RemoteObject> deviceList) {
        setWidth(100, Unit.PERCENTAGE);
        setHeightUndefined();
        
        if (deviceList == null) {
            addComponent(new Label("There are no Devices to show"));
            return;
        }
                
        for (RemoteObject device : deviceList) {
            
            ComponentDevice componentDevice = new ComponentDevice(device, webserviceBean);
            addComponent(componentDevice);
            DragSourceExtension<ComponentDevice> dragSource = new DragSourceExtension<>(componentDevice);
            dragSource.setEffectAllowed(EffectAllowed.MOVE);
            
            if (deviceList.indexOf(device) % 2 == 0)
                componentDevice.addStyleName("deviceeven");
                        
            componentDevice.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                @Override
                public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                    
                    
                    int componentCounter = ComponentConnectionSource.this.getComponentCount();
                    for (int i = 0; i < componentCounter; i += 1) {
                        ComponentConnectionSource.this.getComponent(i).removeStyleName("selected");
                    }                    
                    componentDevice.addStyleName("selected");
                    selectedSourceDevice = componentDevice.getDevice();
                }
            });
        }
                
    }
    /*
    public class ComponentDevice extends VerticalLayout {
        private Label lblDevice;
        private Image imgDevice;
        private final RemoteObject device;
        
        public ComponentDevice(RemoteObject device) {
            this.device = device;
            initializeComponent();
        }
        
        private void initializeComponent() {
            lblDevice = new Label(device.getName() + " [" + device.getClassName() + "]");
            lblDevice.addStyleName(ValoTheme.LABEL_LARGE);
            lblDevice.addStyleName(ValoTheme.LABEL_BOLD);
                                
            addComponent(lblDevice);
            
            int rackUnits = device.getAttribute("rackUnits") != null ? Integer.valueOf(device.getAttribute("rackUnits")) : 0;
            
            if (rackUnits > 0) {
                imgDevice = ComponentRackView.getImage(device, rackUnits, webserviceBean);
                
                if (imgDevice != null)
                    addComponent(imgDevice);
            }
        }
        
        public Image getImgDevice() {
            return imgDevice;
        }
        
        public RemoteObject getDevice() {
            return device;
        }
    }
    */
}
