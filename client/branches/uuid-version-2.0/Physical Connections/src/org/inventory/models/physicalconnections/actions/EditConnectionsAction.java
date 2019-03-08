/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.models.physicalconnections.windows.EditConnectionsFrame;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action allows to modify the connections inside a container
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class EditConnectionsAction extends GenericObjectNodeAction {

    public EditConnectionsAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_CONNECT_LINKS"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0);
        
        HashMap<String, LocalObjectLight[]> specialAttributes = CommunicationsStub.getInstance().getSpecialAttributes(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getId());
        List<LocalObjectLight> parents = CommunicationsStub.getInstance().getParentsUntilFirstOfClass(selectedObject.getClassName(), selectedObject.getId(), "GenericLocation"); //NOI18N
        LocalObjectLight parent = parents.get(parents.size() - 1);
        
        if (specialAttributes == null || parent == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        
        LocalObjectLight endpointA = null;
        LocalObjectLight endpointB = null;
            
        if (specialAttributes.containsKey("endpointA")) //NOI18N
            endpointA = specialAttributes.get("endpointA")[0]; //NOI18N
            
        if (specialAttributes.containsKey("endpointB")) //NOI18N
            endpointB = specialAttributes.get("endpointB")[0]; //NOI18N
            
            
        EditConnectionsFrame frame = new EditConnectionsFrame(selectedObjects.get(0), endpointA != null ? endpointA : parent, endpointB != null ? endpointB : parent);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String[] {Constants.CLASS_GENERICPHYSICALCONTAINER};
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }
}