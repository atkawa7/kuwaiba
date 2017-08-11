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

import com.neotropic.inventory.modules.ipam.windows.VlansFrame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import static javax.swing.Action.NAME;
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
 * Relates a subnet with a VLAN
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class RelateSubnetToVlanAction extends GenericInventoryAction {
    
    private static RelateSubnetToVlanAction instance;
    
    private RelateSubnetToVlanAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELATE_VLAN"));
    }
    
    public static RelateSubnetToVlanAction getInstance() {
        return instance == null ? instance = new RelateSubnetToVlanAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> vlans = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_VLAN);
        Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        
        if (vlans ==  null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        else {
            Collection<? extends LocalObjectLight> lookupResult = selectedNodes.allInstances();
            List<LocalObjectLight> selectedObjects = new ArrayList<>();
            
            Iterator<? extends LocalObjectLight> iterator = lookupResult.iterator();
            
            while (iterator.hasNext())
                selectedObjects.add((LocalObjectLight)iterator.next());
            
            if (vlans.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no VLANs created. Create at least one using the Navigation Tree", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                VlansFrame frame = new VlansFrame(selectedObjects, vlans);
                frame.setVisible(true);
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
