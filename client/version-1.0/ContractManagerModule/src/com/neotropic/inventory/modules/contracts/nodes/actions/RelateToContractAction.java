/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.inventory.modules.contracts.nodes.actions;

import com.neotropic.inventory.modules.contracts.windows.ContractFrame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows the user relate the current object to a service as a resource
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToContractAction extends GenericObjectNodeAction {

    public RelateToContractAction() {
        putValue(NAME, "Relate to Contract...");
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> contracts = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICCONTRACT);
        Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);

        if (contracts ==  null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            Iterator<? extends LocalObjectLight> lookupResult = selectedNodes.allInstances().iterator();
            List<LocalObjectLight> selectedObjects = new ArrayList<>();
            
            while (lookupResult.hasNext())
                selectedObjects.add(lookupResult.next());
            
            ContractFrame frame = new ContractFrame(selectedObjects, contracts);
            frame.setVisible(true);
        }
    }

    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }
}
