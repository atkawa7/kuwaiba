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

package org.kuwaiba.management.services.nodes.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.kuwaiba.management.services.nodes.CustomerChildren;
import org.kuwaiba.management.services.nodes.CustomerNode;
import org.openide.util.Utilities;

/**
 * Creates a new services pool
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class CreateServicePoolAction extends GenericInventoryAction {
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
   
  
    public CreateServicePoolAction() {
        com = CommunicationsStub.getInstance();
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_SERVICES_POOL"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        Iterator<? extends CustomerNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(CustomerNode.class).allInstances().iterator();
            
        if (!selectedNodes.hasNext())
            return;
        
        CustomerNode customerNode = selectedNodes.next();
        
        JTextField txtName = new JTextField(), txtDescription =  new JTextField();
        txtName.setName("txtName"); //NOI18N
        txtName.setPreferredSize(new Dimension(120, 18));
        txtDescription.setName("txtDescription"); //NOI18N
        
        JComplexDialogPanel pnlMyDialog = new JComplexDialogPanel(
                new String[]{java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_NAME"), 
                    java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_DESCRIPTION")},
                new JComponent []{txtName, txtDescription});
        
        if (JOptionPane.showConfirmDialog(null, pnlMyDialog,
                java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_SERVICES_POOL"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
            
            LocalObjectLight newPool = com.createPoolInObject(customerNode.getObject().getClassName(), 
                                            customerNode.getObject().getOid(), 
                                            ((JTextField)pnlMyDialog.getComponent("txtName")).getText(), 
                                            ((JTextField)pnlMyDialog.getComponent("txtDescription")).getText(), 
                                            Constants.CLASS_GENERICSERVICE, LocalPool.POOL_TYPE_MODULE_COMPONENT);
                    
            if (newPool ==  null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((CustomerChildren)customerNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATED"));
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
