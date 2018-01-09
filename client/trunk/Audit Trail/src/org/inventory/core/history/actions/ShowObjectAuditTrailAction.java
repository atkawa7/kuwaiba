/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.history.windows.ObjectAuditTrailTopComponent;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Retrieves the activity log related to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public final class ShowObjectAuditTrailAction extends GenericObjectNodeAction {
    private CommunicationsStub com;

    public ShowObjectAuditTrailAction() {
        putValue(NAME, I18N.gm("audit_trail"));
        com = CommunicationsStub.getInstance();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        LocalObjectLight selectedObject = selectedObjects.get(0);
        LocalApplicationLogEntry[] entries = com.getBusinessObjectAuditTrail(selectedObject.getClassName(), selectedObject.getOid());
        if (entries == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        else{
            ObjectAuditTrailTopComponent tc = (ObjectAuditTrailTopComponent) WindowManager.getDefault()
                .findTopComponent("ObjectAuditTrailTopComponent_" + selectedObject.getOid());
            
            if (tc == null) {
                tc = new ObjectAuditTrailTopComponent(selectedObject);
                tc.open();
            } else {
                if (tc.isOpened())
                    tc.requestAttention(true);
                else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                       //so we will reuse the instance, refreshing the vierw first
                    tc.refresh();
                    tc.open();
                }
            }
            tc.requestActive();
        }
    }

    @Override
    public String[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_AUDIT_TRAIL, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}