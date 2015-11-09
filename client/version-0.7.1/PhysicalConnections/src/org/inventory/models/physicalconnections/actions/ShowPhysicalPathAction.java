/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.models.physicalconnections.windows.PhysicalPathTopComponent;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action shows the physical trace from a port
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ShowPhysicalPathAction extends GenericObjectNodeAction {
    
    public ShowPhysicalPathAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_SHOW_PHYSICAL_PATH"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight[] trace = CommunicationsStub.getInstance().getPhysicalPath(object.getClassName(), object.getOid());
        if (trace == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            PhysicalPathTopComponent tc = new PhysicalPathTopComponent(object, trace);
            tc.open();
            tc.requestActive();
        }
    }

    @Override
    public String getValidator() {
        return Constants.VALIDATOR_PHYSICAL_ENDPOINT;
    }
}
