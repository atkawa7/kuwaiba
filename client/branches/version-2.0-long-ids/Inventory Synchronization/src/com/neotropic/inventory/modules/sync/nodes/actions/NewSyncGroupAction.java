/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sync.nodes.actions;

import com.neotropic.inventory.modules.sync.nodes.SyncGroupRootNode;
import com.neotropic.inventory.modules.sync.nodes.SyncGroupRootNode.SyncGroupRootChildren;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.communications.core.LocalPrivilege;
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Action to create a new Sync Group
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
class NewSyncGroupAction extends GenericInventoryAction {
    
    public NewSyncGroupAction() {
        putValue(NAME, "New Sync Group");
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {
         Iterator<? extends SyncGroupRootNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(SyncGroupRootNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        SyncGroupRootNode selectedNode = selectedNodes.next();
                 
        JTextField txtSyncGroupName = new JTextField();
        txtSyncGroupName.setName("txtSyncGroupName");
        txtSyncGroupName.setColumns(10);
        
        JComplexDialogPanel pnlPoolProperties = new JComplexDialogPanel(
            new String[] {I18N.gm("sync_group_name")}, 
            new JComponent[] {txtSyncGroupName});
             
        if (JOptionPane.showConfirmDialog(null, pnlPoolProperties, I18N.gm("new_sync_group"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
    
            LocalSyncGroup newSyncGroup = CommunicationsStub.getInstance().createSyncGroup(
                ((JTextField) pnlPoolProperties.getComponent("txtSyncGroupName")).getText());
            
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
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
