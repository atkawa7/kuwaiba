/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action releases de relationship between the object and the service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseFromServiceAction extends GenericObjectNodeAction {
    
    public ReleaseFromServiceAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_RELEASE_ELEMENT"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        if (CommunicationsStub.getInstance().releaseObjectFromService(objectClassName, 
                objectId, -1))
            nu.showSimplePopup("Success", NotificationUtil.INFO, "Element released successfully");
        else
            nu.showSimplePopup("Error", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
    }

    @Override
    public String getValidator() {
        return null;
    }
}
