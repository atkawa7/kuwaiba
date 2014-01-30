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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.windows.PhysicalPathTopComponent;
import org.openide.util.Lookup;

/**
 * This action shows the physical trace from a port
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ShowPhysicalPathAction extends AbstractAction {
    private String objectClass;
    private long objectId;
    private NotificationUtil nu;
    public ShowPhysicalPathAction(String objectClass, long objectId) {
        this.objectClass = objectClass;
        this.objectId = objectId;
        this.nu = Lookup.getDefault().lookup(NotificationUtil.class);
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SHOW_PHYSICAL_PATH"));
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight[] trace = CommunicationsStub.getInstance().getPhysicalPath(objectClass, objectId);
        if (trace == null)
            nu.showSimplePopup("Error", NotificationUtil.ERROR,CommunicationsStub.getInstance().getError());
        else{
            PhysicalPathTopComponent tc = new PhysicalPathTopComponent(trace);
            tc.open();
            tc.requestActive();
        }
    }
    
}
