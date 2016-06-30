/*
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.applicationnodes.pools.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.applicationnodes.pools.PoolNode;
import org.inventory.navigation.applicationnodes.pools.PoolRootNode;

/**
 * Creates a new pool
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NewPoolAction extends AbstractAction{
    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;
    /**
     * Reference to the root node;
     */
    private PoolRootNode prn;

    public NewPoolAction(PoolRootNode prn){
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW_POOL"));
        com = CommunicationsStub.getInstance();
        this.prn = prn;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        
        JTextField txtName = new JTextField(), txtDescription =  new JTextField();
        txtName.setName("txtName"); //NOI18N
        txtDescription.setName("txtDescription"); //NOI18N
        
        LocalClassMetadataLight[] allMeta = com.getAllLightMeta(false);
        
        JComboBox<LocalClassMetadataLight> lstType = new JComboBox<>(allMeta);
        lstType.setName("lstType"); //NOI18N
        JComplexDialogPanel pnlMyDialog = new JComplexDialogPanel(
                new String[]{java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NAME"), java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DESCRIPTION"), java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_TYPE")},
                new JComponent []{txtName, txtDescription, lstType});

        if (JOptionPane.showConfirmDialog(null,
                pnlMyDialog,
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW_POOL"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
        
                    LocalPool newPool = com.createRootPool(((JTextField)pnlMyDialog.getComponent("txtName")).getText(), 
                            ((JTextField)pnlMyDialog.getComponent("txtDescription")).getText(), 
                            ((LocalClassMetadataLight)((JComboBox)pnlMyDialog.getComponent("lstType")).getSelectedItem()).getClassName(), 
                            LocalPool.POOL_TYPE_GENERAL_PURPOSE);
                    
                    if (newPool ==  null)
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    else{
                        prn.getChildren().add(new PoolNode[]{new PoolNode(newPool)});
                        NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATED"));
                    }
        }
    }
}