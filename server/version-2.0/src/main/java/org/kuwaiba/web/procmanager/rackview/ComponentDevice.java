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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;

/**
 * Graphical representation of a device front view.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentDevice extends VerticalLayout {
    private Label lblDevice;
    private Image imgDevice;
    private final WebserviceBean webserviceBean;        
    private RemoteObject device;

    public ComponentDevice(RemoteObject device, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;
        this.device = device;
        initializeComponent();
    }
    
    public void setDevice(RemoteObject device) {
        this.device = device;        
    }
        
    public void initializeComponent() {
        removeAllComponents();
        
        lblDevice = new Label(device.getName() + " [" + device.getClassName() + "]");
        lblDevice.addStyleName(ValoTheme.LABEL_LARGE);
        lblDevice.addStyleName(ValoTheme.LABEL_BOLD);

        addComponent(lblDevice);
        setComponentAlignment(lblDevice, Alignment.MIDDLE_CENTER);

        int rackUnits = device.getAttribute("rackUnits") != null ? Integer.valueOf(device.getAttribute("rackUnits")) : 0;

        if (rackUnits > 0) {
            imgDevice = ComponentRackView.getImage(device, rackUnits, webserviceBean);

            if (imgDevice != null) {
                addComponent(imgDevice);
                setComponentAlignment(imgDevice, Alignment.MIDDLE_CENTER);
            }
        }
    }

    public Image getImgDevice() {
        return imgDevice;
    }

    public RemoteObject getDevice() {
        return device;
    }
}
