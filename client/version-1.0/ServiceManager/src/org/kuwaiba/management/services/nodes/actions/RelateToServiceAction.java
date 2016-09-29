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
package org.kuwaiba.management.services.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.windows.ServicesFrame;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows the user relate the current object to a service as a resource
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToServiceAction extends GenericObjectNodeAction {

    public RelateToServiceAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_RELATE_TO_SERVICE"));
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> services = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICSERVICE);
        Lookup.Result<LocalObjectLight> selectedNodes = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);

        if (services ==  null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            Collection lookupResult = selectedNodes.allInstances();
            LocalObjectLight[] selectedObjects = new LocalObjectLight[lookupResult.size()];
            int i = 0;
            for (Iterator it = lookupResult.iterator(); it.hasNext();) {
                selectedObjects[i] = (LocalObjectLight)it.next();
                i++;
            }
            ServicesFrame frame = new ServicesFrame(selectedObjects, services);
            frame.setVisible(true);
        }
    }

    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }
    
}
