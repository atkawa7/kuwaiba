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
package com.neotropic.inventory.modules.ipam.nodes.actions;

import com.neotropic.inventory.modules.ipam.windows.SubnetsFrame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
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
public class RelateEndPointToIPAddressAction extends GenericObjectNodeAction {

    public RelateEndPointToIPAddressAction(){
        putValue(NAME, I18N.gm("relate_to_ip"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalPool> subnets = CommunicationsStub.getInstance().getSubnetPools("-1", null);
        Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        
        if(subnets == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            Collection<? extends LocalObjectLight> lookupResult = selectedNodes.allInstances();
            List<LocalObjectLight> selectedPorts = new ArrayList<>();
            Iterator<? extends LocalObjectLight> iterator = lookupResult.iterator();
            
            while (iterator.hasNext())
                selectedPorts.add((LocalObjectLight)iterator.next());
            
            SubnetsFrame frame = new SubnetsFrame(subnets, selectedPorts);
            frame.setVisible(true);
        }
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
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
