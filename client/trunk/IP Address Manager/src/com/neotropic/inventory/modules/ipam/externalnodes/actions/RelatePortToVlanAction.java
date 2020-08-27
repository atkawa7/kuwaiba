/*
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.inventory.modules.ipam.externalnodes.actions;

import com.neotropic.inventory.modules.ipam.windows.VlansFrame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Actions to relate a Generic port to an IP address
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelatePortToVlanAction extends GenericObjectNodeAction {

    public RelatePortToVlanAction(){
        putValue(NAME, I18N.gm("relate_to_vlan"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        
        Collection<? extends LocalObjectLight> lookupResult = selectedNodes.allInstances();
        List<LocalObjectLight> selectedPorts = new ArrayList<>();
        Iterator<? extends LocalObjectLight> iterator = lookupResult.iterator();
            
        while (iterator.hasNext())
            selectedPorts.add((LocalObjectLight)iterator.next());
       
        LocalObjectLight parent = null;
        for (LocalObjectLight selectedPort : selectedPorts) {
            //we check that all the selected ports has the same parent
            List<LocalObjectLight> portParents = CommunicationsStub.getInstance().getParentsUntilFirstOfClass(selectedPort.getClassName(), selectedPort.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
            //we also check that the parent of the ports is a swtich or a router 
            if(!CommunicationsStub.getInstance().isSubclassOf(portParents.get(portParents.size()-1).getClassName(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT))
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, String.format("The parent of the port is not %s", Constants.CLASS_GENERICCOMMUNICATIONSELEMENT));    
            else{
                if(parent == null)
                    parent = portParents.get(portParents.size()-1);
                else if(!parent.equals(portParents.get(portParents.size()-1))){
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, "Select Ports only in the same Device");
                    return;
                }
            }
        }
        
        if(parent == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, "The port(s) parent could not be located");
        else{
            List<LocalObjectLight> vlans = CommunicationsStub.getInstance().getSpecialChildrenOfClassLight(parent.getClassName(), parent.getId(), Constants.CLASS_VLAN);

            if(vlans == null)
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else{
                VlansFrame frame = new VlansFrame(selectedPorts, vlans);
                frame.setVisible(true);
            }
        }
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String [] {Constants.CLASS_GENERICPORT};
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
