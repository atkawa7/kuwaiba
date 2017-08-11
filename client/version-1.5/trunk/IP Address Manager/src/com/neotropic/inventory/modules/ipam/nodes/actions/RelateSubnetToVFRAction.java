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

import com.neotropic.inventory.modules.ipam.windows.VFRsFrame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Relates a VRF with a subnet
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class RelateSubnetToVFRAction extends GenericInventoryAction {

    private static RelateSubnetToVFRAction instance;
    
    private RelateSubnetToVFRAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELATE_VRF"));
    }
    
    public static RelateSubnetToVFRAction getInstance() {
        return instance == null ? instance = new RelateSubnetToVFRAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> vfrInstances = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_VRFINSTANCE);
        
        if (vfrInstances ==  null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        else {
            Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
            Collection<? extends LocalObjectLight> lookupResult = selectedNodes.allInstances();
            List<LocalObjectLight> selectedObjects = new ArrayList<>();
            Iterator<? extends LocalObjectLight> iterator = lookupResult.iterator();
            
            while (iterator.hasNext())
                selectedObjects.add((LocalObjectLight)iterator.next());
            
            if (vfrInstances.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no VRF instances created. Create at least one using the Navigation Tree", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                VFRsFrame frame = new VFRsFrame(selectedObjects, vfrInstances);
                frame.setVisible(true);
            }
        }    
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
