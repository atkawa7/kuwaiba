/**
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.inventory.modules.ipam.nodes.actions;

import com.neotropic.inventory.modules.ipam.nodes.IpNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.actions.Presenter;

/**
 * Release an IP Address from a device
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ReleaseFromDeviceAction extends GenericObjectNodeAction implements Presenter.Popup {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (CommunicationsStub.getInstance().releaseIPfromDevice(object.getClassName(), 
                object.getOid(), Long.valueOf(((JMenuItem)e.getSource()).getName())))
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUCCESS"));
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }

    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuServices = new JMenu(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELEASE_DEVICE"));
        LocalObjectLight[] services = CommunicationsStub.getInstance().getSpecialAttribute(object.getClassName(), 
                object.getOid(), "uses");
        
        if (services != null) {
        
            if (services.length == 0)
                mnuServices.setEnabled(false);
            else {
                for (LocalObjectLight service : services){
                    JMenuItem smiServices = new JMenuItem(service.toString());
                    smiServices.setName(String.valueOf(service.getOid()));
                    smiServices.addActionListener(this);
                    mnuServices.add(smiServices);
                }
            }
            return mnuServices;
        } else {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        } 
    }
}
