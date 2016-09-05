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
package com.neotropic.inventory.modules.mpls.actions;

import com.neotropic.inventory.modules.mpls.windows.DevicesFrame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Relates a GenericNetworkElement with a BridgeDomain
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToNetworkDeviceAction extends GenericObjectNodeAction{

    public RelateToNetworkDeviceAction(){
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/mpls/Bundle").getString("LBL_RELATE_DEVICE"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> devices = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICNETWORKELEMENT);
        Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        
        if (devices ==  null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        else{
            Collection<? extends LocalObjectLight> lookupResult = selectedNodes.allInstances();
            List<LocalObjectLight> selectedObjects = new ArrayList<>();
            Iterator<? extends LocalObjectLight> iterator = lookupResult.iterator();
            
            while (iterator.hasNext())
                selectedObjects.add((LocalObjectLight)iterator.next());

            DevicesFrame frame = new DevicesFrame(selectedObjects, devices);
            frame.setVisible(true);
        }    

    }
    
    @Override
    public String getValidator() {
        return Constants.VALIDATOR_NETWORK_ELEMENT_LOGICAL_CONFIGURATION;
    }    
}
