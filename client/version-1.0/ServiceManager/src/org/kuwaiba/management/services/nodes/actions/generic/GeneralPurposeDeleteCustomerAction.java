/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * General purpose version of the DeleteCustomerAction. It doesn't try to refresh the tree
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class GeneralPurposeDeleteCustomerAction extends GenericObjectNodeAction {

    public GeneralPurposeDeleteCustomerAction() {
        putValue(NAME, "Delete Customer");
    }    
    
    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this customer? All services associated will be deleted too",
                "Delete Customer",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        
            if (CommunicationsStub.getInstance().deleteObject(object.getClassName(), object.getOid())) 
                NotificationUtil.getInstance().showSimplePopup("Success", 
                        NotificationUtil.INFO_MESSAGE, "The customer was deleted successfully");
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public String getValidator() {
        return "customer";
    }
}
