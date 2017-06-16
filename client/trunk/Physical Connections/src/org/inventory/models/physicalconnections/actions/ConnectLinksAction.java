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
package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.models.physicalconnections.windows.ConnectLinksFrame;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows to connect the links (cables, fibers) inside a container
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class ConnectLinksAction extends GenericObjectNodeAction {

    public ConnectLinksAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_CONNECT_LINKS"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        List<LocalObjectLight> links = CommunicationsStub.getInstance().getObjectSpecialChildren(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getOid());
        if (links == null){
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        LocalObjectLight[] containerEndpoints = CommunicationsStub.getInstance().getConnectionEndpoints(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getOid());
        if (containerEndpoints == null){
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        
        if (containerEndpoints[0] == null || containerEndpoints[1] == null){
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, String.format(
                    "Container %s is missing one of its endpoints", selectedObjects.get(0)));
            return;
        }
        ConnectLinksFrame frame = new ConnectLinksFrame(containerEndpoints[0],containerEndpoints[1], links);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public String getValidator() {
        return Constants.VALIDATOR_PHYSICAL_CONTAINER;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}