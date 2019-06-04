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
 *  under the License.
 */
package org.kuwaiba.management.services.nodes.actions.generic;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;

/**
 * General purpose version of the DeleteServiceAction, to be used outside the Service Manager module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GeneralPurposeDeleteServiceAction extends GenericInventoryAction {

    private static GeneralPurposeDeleteServiceAction instance;
    
    private GeneralPurposeDeleteServiceAction() {
        putValue(NAME, "Delete Service");
    }
    
    public static GeneralPurposeDeleteServiceAction getInstance() {
        return instance == null ? instance = new GeneralPurposeDeleteServiceAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        LocalObjectLight selectedObject = Utilities.actionsGlobalContext().lookup(LocalObjectLight.class);
        if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this service? All resources associated will be freed",
                "Delete Service",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        
            if (CommunicationsStub.getInstance().deleteObject(selectedObject.getClassName(), selectedObject.getId(), false))              
                NotificationUtil.getInstance().showSimplePopup("Success", 
                        NotificationUtil.INFO_MESSAGE, "The service was deleted successfully");
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return null;
    }
}
