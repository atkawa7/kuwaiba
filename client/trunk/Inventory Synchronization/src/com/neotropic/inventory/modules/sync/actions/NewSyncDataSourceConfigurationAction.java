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
package com.neotropic.inventory.modules.sync.actions;

import com.neotropic.inventory.modules.sync.nodes.SyncGroupNode;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.util.Constants;
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
        putValue(NAME, I18N.gm("new_ds_config"));
    }
    
    public static NewSyncDataSourceConfigurationAction getInstance() {
        return instance == null ? instance = new NewSyncDataSourceConfigurationAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SyncGroupNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(SyncGroupNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        SyncGroupNode selectedNode = selectedNodes.next();
        
        List<LocalObjectLight> commDevices = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
        if (commDevices == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            return;
        }
        
        JTextField txtSyncDataSourceConfigName = new JTextField();
        txtSyncDataSourceConfigName.setName("txtSyncDataSourceConfigName");
        txtSyncDataSourceConfigName.setColumns(15);
        
        JComboBox<LocalObjectLight> cmbDevices = new JComboBox<>(commDevices.toArray(new LocalObjectLight[0]));
        cmbDevices.setName("cmbDevices");
        
        JTextField txtIPAddress = new JTextField();
        txtIPAddress.setName("txtIPAddress");
        txtIPAddress.setColumns(15);
        
        JTextField txtPort = new JTextField();
        txtPort.setName("txtPort");
        txtPort.setColumns(15);
        
        JTextField txtCommunity = new JTextField();
        txtCommunity.setName("txtcomunity");
        txtCommunity.setColumns(15);
        
        
        JComplexDialogPanel pnlSyncDataSourceProperties = new JComplexDialogPanel(
            new String[] {I18N.gm("sync_datasource_config_name"),
                I18N.gm("device"), I18N.gm("ip_address"), 
                I18N.gm("port"), I18N.gm("community")}, 
            new JComponent[] { txtSyncDataSourceConfigName, cmbDevices, txtIPAddress, txtPort, txtCommunity });
        
        if (JOptionPane.showConfirmDialog(null, pnlSyncDataSourceProperties, I18N.gm("new_ds_config"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            if (txtSyncDataSourceConfigName.getText().trim().isEmpty() || cmbDevices.getSelectedItem() == null ||
                    txtIPAddress.getText().trim().isEmpty() || txtPort.getText().trim().isEmpty() || txtCommunity.getText().trim().isEmpty())
                JOptionPane.showMessageDialog(null, I18N.gm("missing_fields"), I18N.gm("new_ds_config"), JOptionPane.ERROR_MESSAGE);
            else {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("ipAddress", txtIPAddress.getText()); //NOI18N
                parameters.put("port", txtPort.getText()); //NOI18N
                parameters.put("community", txtCommunity.getText()); //NOI18N
                parameters.put("deviceId", Long.toString(((LocalObjectLight)cmbDevices.getSelectedItem()).getOid())); //NOI18N
                parameters.put("deviceClass", ((LocalObjectLight)cmbDevices.getSelectedItem()).getClassName()); //NOI18N
                
                LocalSyncDataSourceConfiguration newSyncConfig = CommunicationsStub.getInstance().
                    createSyncDataSourceConfiguration(selectedNode.getLookup().lookup(LocalSyncGroup.class).getId(), 
                            txtSyncDataSourceConfigName.getText(), parameters);
            
                if (newSyncConfig == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
                } else {
                    ((SyncGroupNode.SyncGroupNodeChildren) selectedNode.getChildren()).addNotify();

                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                        NotificationUtil.INFO_MESSAGE, I18N.gm("new_sync_config_created_successfully"));
                }
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
