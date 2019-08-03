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

import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DragSourceExtension;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;

/**
 * Layout to show a list of available devices
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentDeviceList extends VerticalLayout {
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;
    private List<RemoteObject> devices;
    
    public ComponentDeviceList(List<RemoteObject> deviceList, WebserviceBean webserviceBean, RemoteSession remoteSession) {
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
        
        setWidth(100, Unit.PERCENTAGE);
        setHeightUndefined();
        setStyleName("selector");
        initializeComponent(deviceList);
    }
    
    public List<RemoteObject> getDevices() {
        return devices;        
    }
    
    public void initializeComponent(List<RemoteObject> deviceList) {
        this.devices = deviceList;
        removeAllComponents();
        
        setWidth(100, Unit.PERCENTAGE);
        setHeightUndefined();
        
        if (deviceList == null) {
            addComponent(new Label("There are no Devices to show"));
            return;
        }
                
        for (RemoteObject device : deviceList) {
            
            try {
                if (!webserviceBean.hasAttribute(device.getClassName(), "rackUnits", Page.getCurrent().getWebBrowser().getAddress(), remoteSession.getSessionId()) ||
                    !webserviceBean.hasAttribute(device.getClassName(), "position", Page.getCurrent().getWebBrowser().getAddress(), remoteSession.getSessionId())) {
                    
                    Notifications.showError(String.format("The class %s does not have the attributes rackUnits or position", device.getClassName()));
                    
                    continue;
                }
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            
            ComponentDevice componentDevice = new ComponentDevice(device, webserviceBean);
            
            addComponent(componentDevice);
            DragSourceExtension<ComponentDevice> dragSource = new DragSourceExtension<>(componentDevice);
            dragSource.setEffectAllowed(EffectAllowed.MOVE);
            
            if (deviceList.indexOf(device) % 2 == 0)
                componentDevice.addStyleName("deviceeven");
            
            componentDevice.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                @Override
                public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                    
                    
                    int componentCounter = ComponentDeviceList.this.getComponentCount();
                    for (int i = 0; i < componentCounter; i += 1) {
                        ComponentDeviceList.this.getComponent(i).removeStyleName("selected");
                    }
                    componentDevice.addStyleName("selected");
                }
            });
        }
    }
}
