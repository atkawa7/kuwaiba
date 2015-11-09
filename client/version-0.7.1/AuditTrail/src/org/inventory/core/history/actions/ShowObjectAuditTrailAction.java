/*
 *  Copyright 2010-2015, 2013 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.history.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalApplicationLogEntry;
import org.inventory.core.history.windows.ObjectAuditTrailTopComponent;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Retrieves the activity log related to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public final class ShowObjectAuditTrailAction extends GenericObjectNodeAction {
    private CommunicationsStub com;

    public ShowObjectAuditTrailAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/core/history/Bundle").getString("LBL_AUDIT_TRAIL"));
        com = CommunicationsStub.getInstance();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        LocalApplicationLogEntry[] entries = com.getBusinessObjectAuditTrail(object.getClassName(), object.getOid());
        if (entries == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else{
            TopComponent tc = new ObjectAuditTrailTopComponent(object, entries);
            tc.open();
            tc.requestActive();
        }
    }

    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }
}