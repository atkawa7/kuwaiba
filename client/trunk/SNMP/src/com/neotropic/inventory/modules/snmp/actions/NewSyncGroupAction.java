/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package com.neotropic.inventory.modules.snmp.actions;

import com.neotropic.inventory.modules.snmp.nodes.SyncGroupRootNode;
import com.neotropic.inventory.modules.snmp.nodes.SyncGroupRootNode.SyncGroupRootChildren;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Action to create a new Sync Group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class NewSyncGroupAction extends GenericInventoryAction {
    private static NewSyncGroupAction instance;
    
    private NewSyncGroupAction() {
        putValue(NAME, "New Sync Group");
    }
    
    public static NewSyncGroupAction getInstance() {
        return instance == null ? instance = new NewSyncGroupAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SyncGroupRootNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(SyncGroupRootNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        SyncGroupRootNode selectedNode = selectedNodes.next();
        
        JTextField txtsyncGroupName = new JTextField();
        txtsyncGroupName.setName("txtSyncGroupName");
        txtsyncGroupName.setColumns(10);
        
        JTextField txtsyncProviderName = new JTextField();
        txtsyncProviderName.setText("SNMP Provider");
        txtsyncProviderName.setName("txtSyncProviderName");
        txtsyncProviderName.setColumns(10);
        txtsyncProviderName.setEnabled(false);
        
        JComplexDialogPanel pnlPoolProperties = new JComplexDialogPanel(
            new String[] {I18N.gm("sync_group_name"), I18N.gm("sync_provider")}, 
            new JComponent[] {txtsyncGroupName, txtsyncProviderName});
        
        if (JOptionPane.showConfirmDialog(null, pnlPoolProperties, I18N.gm("new_sync_group"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            LocalSyncGroup newSyncGroup = CommunicationsStub.getInstance().createSyncGroup(
                ((JTextField) pnlPoolProperties.getComponent("txtSyncGroupName")).getText(),
                ((JTextField) pnlPoolProperties.getComponent("txtSyncProviderName")).getText());
            
            if (newSyncGroup == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                ((SyncGroupRootChildren) selectedNode.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, I18N.gm("new_sync_group_created_successfully"));
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_FAVORITES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
