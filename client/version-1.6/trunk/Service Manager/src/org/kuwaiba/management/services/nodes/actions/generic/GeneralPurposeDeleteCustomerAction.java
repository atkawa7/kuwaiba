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
 *  under the License.
 */
package org.kuwaiba.management.services.nodes.actions.generic;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
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
                        
            if (CommunicationsStub.getInstance().deleteObject(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getOid())) 
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                        NotificationUtil.INFO_MESSAGE, "The customer was deleted successfully");
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public String[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String[] { Constants.CLASS_GENERICCUSTOMER };
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}
