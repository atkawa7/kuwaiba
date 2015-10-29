/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.kuwaiba.management.services.nodes.CustomerChildren;
import org.kuwaiba.management.services.nodes.CustomerNode;
import org.kuwaiba.management.services.nodes.ServiceManagerRootNode;
import org.kuwaiba.management.services.nodes.ServicesPoolNode;

/**
 * Creates a new services pool
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class CreateServicesPoolAction extends GenericObjectNodeAction {
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    /**
     * Reference to the root node;
     */
    ServiceManagerRootNode rootNode;
    /**
     * Reference to a customer node;
     */
    CustomerNode customerNode;
    
    public CreateServicesPoolAction(ServiceManagerRootNode rootNode) {
        this.rootNode = rootNode;
        com = CommunicationsStub.getInstance();
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_SERVICES_POOL"));
    }

    public CreateServicesPoolAction(CustomerNode customerNode) {
        this.customerNode =  customerNode;
        com = CommunicationsStub.getInstance();
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_SERVICES_POOL"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

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
            
            LocalObjectLight newPool = com.createPool(customerNode.getObject().getOid(), ((JTextField)pnlMyDialog.getComponent("txtName")).getText(), 
                    ((JTextField)pnlMyDialog.getComponent("txtDescription")).getText(), "GenericService");
                    
            if (newPool ==  null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else{
                if(object != null){
                    if (!((CustomerChildren)customerNode.getChildren()).isCollapsed())
                        customerNode.getChildren().add(new ServicesPoolNode[]{new ServicesPoolNode(newPool)});
                    NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATED"));
                }
            }
        }
    }

    @Override
    public String getValidator() {
        return null;
    }
}
