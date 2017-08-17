/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import com.neotropic.inventory.modules.ipam.windows.BDIsInterfaceFrame;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Actions to relate a Service Instance to a BridgeDomain
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateIPToBDIInterfaceAction extends GenericObjectNodeAction {

    public RelateIPToBDIInterfaceAction(){
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELATE_TO_BDIS"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> bdis = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_BRIDGEDOMAININTERFACE);
        
        if (bdis != null) {
            if (bdis.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no Bridge Domain Interfaces created. Create at least one using the Navigation Tree", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                BDIsInterfaceFrame frame = new BDIsInterfaceFrame(selectedObjects, bdis);
                frame.setVisible(true);
            }
        } else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }
    
    @Override
    public String getValidator() {
        return Constants.VALIDATOR_LOGICAL_SET;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
