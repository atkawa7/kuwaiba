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
import org.openide.util.Lookup;

/**
 * This action allows to connect the links (cables, fibers) inside a container
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ConnectLinksAction extends AbstractAction {
    private String objectClass;
    private long objectId;
    private NotificationUtil nu;
    public ConnectLinksAction(String objectClass, long objectId) {
        this.objectClass = objectClass;
        this.objectId = objectId;
        this.nu = Lookup.getDefault().lookup(NotificationUtil.class);
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_RELEASE_MIRROR_PORT"));
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        //LocalObjectLight[] links = CommunicationsStub.getInstance().getObjectSpecialChildren(objectClass, objectId);
        //LocalObjectLight[] containerEndpoints = CommunicationsStub.getInstance().getConnectionEndpoints(objectClass, objectId);
        
        if (CommunicationsStub.getInstance().releaseMirrorPort(objectClass, objectId))
            nu.showSimplePopup("Success", NotificationUtil.INFO, "Miror port released successfully");
        else
            nu.showSimplePopup("Error", NotificationUtil.ERROR,CommunicationsStub.getInstance().getError());        
    }
    
}
