/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.inventory.views.rackview.actions;

import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.views.connections.ConnectionsInRackViewTopComponent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Action to show the rack view of a rack
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ShowConnectionsInRackViewAction extends GenericObjectNodeAction {
    
    public ShowConnectionsInRackViewAction() {
        putValue(NAME, "Show COnnections in Rack");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        for (LocalObjectLight rack : selectedObjects) {
            ConnectionsInRackViewTopComponent rackView = ((ConnectionsInRackViewTopComponent) WindowManager.
                getDefault().findTopComponent("ConnectionsInRackViewTopComponent_" + rack.getOid()));
            
            if (rackView == null) {
                rackView = new ConnectionsInRackViewTopComponent(rack);
                rackView.open();
            } else {
                if (rackView.isOpened())
                    rackView.requestAttention(true);
                else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                       //so we will reuse the instance, refreshing the vierw first
                    rackView.refresh();
                    rackView.open();
                }
            }
            rackView.requestActive();
        }
    }
    
    @Override
    public String getValidator() {
        return Constants.VALIDATOR_RACK;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ);
    }
    
}
