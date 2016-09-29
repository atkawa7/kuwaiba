/**
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
 */
package com.neotropic.inventory.modules.ipam.nodes.actions;

import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolChildren;
import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import org.inventory.communications.CommunicationsStub;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import java.awt.Dimension;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.util.Utilities;

/**
 * This action allows to create a pool of subnets
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CreateSubnetPoolAction extends GenericObjectNodeAction{
    
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    
    private SubnetPoolNode subnetPoolNode;
    
    public CreateSubnetPoolAction(SubnetPoolNode subnetPoolNode) {
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NEW_FOLDER"));
        com = CommunicationsStub.getInstance();
        this.subnetPoolNode = subnetPoolNode;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SubnetPoolNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetPoolNode.class).allInstances().iterator();
        String className = "";
        
        if (!selectedNodes.hasNext())
            return;
        
        while (selectedNodes.hasNext()) {
            SubnetPoolNode selectedNode = (SubnetPoolNode)selectedNodes.next();
            className = selectedNode.getSubnetPool().getClassName();
        }
        
        JTextField txtName = new JTextField(), txtDescription =  new JTextField();
        txtName.setName("txtName"); //NOI18N
        txtName.setPreferredSize(new Dimension(120, 18));
        txtDescription.setName("txtDescription"); //NOI18N
        
        JComplexDialogPanel pnlMyDialog = new JComplexDialogPanel(
                
                new String[]{java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NAME"), 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DESCRIPTION")},
                new JComponent []{txtName, txtDescription});
        
        if (JOptionPane.showConfirmDialog(null, pnlMyDialog,
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NEW_FOLDER"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
            
            LocalObjectLight newPool = com.createSubnetPool(subnetPoolNode.getSubnetPool().getOid(), 
                    className,
                    ((JTextField)pnlMyDialog.getComponent("txtName")).getText(), 
                    ((JTextField)pnlMyDialog.getComponent("txtDescription")).getText(), 3); //Type of pool module component. These pools are used in models and are in the lower levels of the pool containment hierarchy
            
            if (newPool ==  null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else{
                ((SubnetPoolChildren)subnetPoolNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_CREATED"));
            }
        }
    }

    @Override
    public String getValidator() {
        return null;
    }
}
