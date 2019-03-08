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

import com.neotropic.inventory.modules.sync.nodes.SyncDataSourceConfigurationNode;
import com.neotropic.inventory.modules.sync.nodes.SyncGroupNode.SyncGroupNodeChildren;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Abstract action executed by DeleteSyncAction to delete a sync data source 
 * configuration.
 * Is not a GenericInventoryAction
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeleteSyncDataSourceConfigurationAction extends AbstractAction {
    private static DeleteSyncDataSourceConfigurationAction instance;
    
    private DeleteSyncDataSourceConfigurationAction() {}
    
    public static DeleteSyncDataSourceConfigurationAction getInstance() {
        return instance == null ? instance = new DeleteSyncDataSourceConfigurationAction() : instance;
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {        
        if (JOptionPane.showConfirmDialog(null, I18N.gm("want_to_delete_sync_data_src_config"), 
            I18N.gm("warning"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            SyncDataSourceConfigurationNode syncConfigNode = Utilities.actionsGlobalContext().lookup(SyncDataSourceConfigurationNode.class);
            LocalSyncDataSourceConfiguration localSyncDataSrcConfig = Utilities.actionsGlobalContext().lookup(LocalSyncDataSourceConfiguration.class);
            
            if (localSyncDataSrcConfig != null) {
                if (CommunicationsStub.getInstance().deleteSyncDataSourceConfiguration(localSyncDataSrcConfig.getId())) {
                    ((SyncGroupNodeChildren) syncConfigNode.getParentNode().getChildren()).addNotify();
                    
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                        NotificationUtil.INFO_MESSAGE, I18N.gm("sync_data_src_config_deleted_successfully"));
                } else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }    
}
