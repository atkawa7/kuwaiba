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
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Action to create a new Sync Group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class NewSyncDataSourceConfigurationAction extends GenericInventoryAction {
    private static NewSyncDataSourceConfigurationAction instance;
    
    private NewSyncDataSourceConfigurationAction() {
        putValue(NAME, "New DataSourceConfig");
    }
    
    public static NewSyncDataSourceConfigurationAction getInstance() {
        return instance == null ? instance = new NewSyncDataSourceConfigurationAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SyncGroupRootNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(SyncGroupRootNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        SyncGroupRootNode selectedNode = selectedNodes.next();
        
        
        
        JTextField txtDSCName = new JTextField();
        txtDSCName.setName("txtDSCName");
        txtDSCName.setColumns(10);
        
        JTextField txtObjId = new JTextField();
        txtObjId.setName("txtObjId");
        txtObjId.setColumns(10);
        
        JTextField txtObjClassName = new JTextField();
        txtObjClassName.setName("txtObjClassName");
        txtObjClassName.setColumns(10);
        
        JTextField txtObjName = new JTextField();
        txtObjName.setName("txtObjName");
        txtObjName.setColumns(10);
        
        JTextField txtIPAddress = new JTextField();
        txtIPAddress.setName("IPAddress");
        txtIPAddress.setColumns(10);
        
        JTextField txtPort = new JTextField();
        txtPort.setName("txtPort");
        txtPort.setColumns(10);
        
        JTextField txtCommunity = new JTextField();
        txtCommunity.setName("txtcomunity");
        txtCommunity.setColumns(10);
        
        
        JComplexDialogPanel pnlPoolProperties = new JComplexDialogPanel(
            new String[] {I18N.gm("sync_datasource_config_name"),
                I18N.gm("object_id"), I18N.gm("object_name"), 
                I18N.gm("object_class_name"), I18N.gm("ip_address"), 
                I18N.gm("port"), I18N.gm("Community")}, 
            new JComponent[] {txtDSCName, txtObjId, txtObjName, txtObjClassName, 
                txtIPAddress, txtPort, txtCommunity});
        
        if (JOptionPane.showConfirmDialog(null, pnlPoolProperties, I18N.gm("sync_datasource_config_name_config"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            LocalFavoritesFolder newFavorites = CommunicationsStub.getInstance().createFavoritesFolderForUser(
                ((JTextField) pnlPoolProperties.getComponent("txtFavoritesFolderName")).getText());
            
            if (newFavorites == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                ((SyncGroupRootChildren) selectedNode.getChildren()).addNotify();
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, I18N.gm("favorites_folder_created_successfully"));
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_FAVORITES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
