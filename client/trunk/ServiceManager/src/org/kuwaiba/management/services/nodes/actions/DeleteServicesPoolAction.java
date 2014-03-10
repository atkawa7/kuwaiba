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

package org.kuwaiba.management.services.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.nodes.ServicesPoolNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Action to delete a pool of service
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class DeleteServicesPoolAction extends AbstractAction{
    
    private ServicesPoolNode servicesPoolNode;

    public DeleteServicesPoolAction(ServicesPoolNode servicesPoolNode) {
        this.servicesPoolNode = servicesPoolNode;
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_DELETE_SERVICES_POOL"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_DELETE_POOL"),
                java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CONFIRMATION"),JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){

            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            if (CommunicationsStub.getInstance().deletePool(servicesPoolNode.getPool().getOid())){
                servicesPoolNode.getParentNode().getChildren().remove(new Node[]{servicesPoolNode});
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_DELETION_TITLE"), NotificationUtil.INFO, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_DELETION_TEXT_OK"));
            }
            else
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_DELETION_TEXT_ERROR"), NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        }
    }
    
    
    
}
