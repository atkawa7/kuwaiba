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

import com.neotropic.inventory.modules.ipam.windows.BDIsInterfaceFrame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Actions to relate a Service Instance to a BridgeDomain
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateIPToBDIInterface extends GenericObjectNodeAction{

    public RelateIPToBDIInterface(){
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELATE_TO_BDIS"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> interfaces = new ArrayList<>();
        List<LocalObjectLight> objects = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_BRIDGEDOMAININTERFACE);
        if(objects != null){
            for(LocalObjectLight o : objects)
                interfaces.add(o);
        }      
        Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        Collection<? extends LocalObjectLight> lookupResult = selectedNodes.allInstances();
        List<LocalObjectLight> selectedObjects = new ArrayList<>();
        Iterator<? extends LocalObjectLight> iterator = lookupResult.iterator();

        while (iterator.hasNext())
            selectedObjects.add((LocalObjectLight)iterator.next());

        BDIsInterfaceFrame frame = new BDIsInterfaceFrame(selectedObjects, interfaces);
        frame.setVisible(true);
    }
    
    @Override
    public String getValidator() {
        return Constants.VALIDATOR_LOGICAL_SET;
    }
}
