/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author johnyortega
 */
    public class ComponentDevice extends VerticalLayout {
        private Label lblDevice;
        private Image imgDevice;
        private final WebserviceBean webserviceBean;        
        private final RemoteObject device;
        
        public ComponentDevice(RemoteObject device, WebserviceBean webserviceBean) {
            this.webserviceBean = webserviceBean;
            this.device = device;
            initializeComponent();
        }
        
        private void initializeComponent() {
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
