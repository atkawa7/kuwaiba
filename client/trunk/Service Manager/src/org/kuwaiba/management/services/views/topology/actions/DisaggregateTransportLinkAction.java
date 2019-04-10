/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.management.services.views.topology.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.kuwaiba.management.services.views.topology.TopologyViewScene;

/**
 * Replaces the selected transport link with the containers within (if any). If there isn't any, a message will be displayed.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DisaggregateTransportLinkAction extends GenericInventoryAction {
    private TopologyViewScene scene;
    public DisaggregateTransportLinkAction(TopologyViewScene scene) {
        this.putValue(NAME, "Disaggregate Transport Link"); 
        this.scene = scene;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> selectedLinks = new ArrayList();
        
        for (Object selectedObject : scene.getSelectedObjects()) 
            selectedLinks.add((LocalObjectLight)selectedObject);
        
        scene.expandTransportLinks(selectedLinks);
    }
}