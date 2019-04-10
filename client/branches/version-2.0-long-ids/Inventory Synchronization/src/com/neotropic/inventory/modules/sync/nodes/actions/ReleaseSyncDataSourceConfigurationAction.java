/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import com.neotropic.inventory.modules.sync.nodes.SyncGroupNode;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Abstract action executed by relesea a sync data source configuration.
 * Is not a GenericInventoryAction
 * @author Adrian MArtinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class ReleaseSyncDataSourceConfigurationAction extends GenericInventoryAction {
    private static ReleaseSyncDataSourceConfigurationAction instance;
    
    private ReleaseSyncDataSourceConfigurationAction() {
        putValue(NAME, "Release from SyncGroup");
    }
    
    public static ReleaseSyncDataSourceConfigurationAction getInstance() {
        return instance == null ? instance = new ReleaseSyncDataSourceConfigurationAction() : instance;
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {        
//            Iterator<? extends SyncDataSourceConfigurationNode> selectedSyncDsConfigNodes = Utilities.actionsGlobalContext().lookupResult(SyncDataSourceConfigurationNode.class).allInstances().iterator();
//            List<SyncDataSourceConfigurationNode> dsConfigs = new ArrayList<>();
            List<Long> existingWireContainersList = new ArrayList<>();
            
//            while(selectedSyncDsConfigNodes.hasNext())
//                dsConfigs.add(selectedSyncDsConfigNodes.next());
//
//            for (SyncDataSourceConfigurationNode dsConfig : dsConfigs) {
//                existingWireContainersList.add(dsConfig.getLookup().lookup(LocalSyncDataSourceConfiguration.class).getId());
//            }
            SyncDataSourceConfigurationNode syncConfigNode = Utilities.actionsGlobalContext().lookup(SyncDataSourceConfigurationNode.class);
            LocalSyncDataSourceConfiguration localSyncDataSrcConfig = Utilities.actionsGlobalContext().lookup(LocalSyncDataSourceConfiguration.class);
            existingWireContainersList.add(localSyncDataSrcConfig.getId());
            
            SyncGroupNode syncGroupNode = (SyncGroupNode) syncConfigNode.getParentNode();
            LocalSyncGroup syncGroup = syncGroupNode.getLookup().lookup(LocalSyncGroup.class);
            

            if (CommunicationsStub.getInstance().releaseSyncDataSourceConfigurationFromSyncGroup(syncGroup.getId(), existingWireContainersList)) {
                ((SyncGroupNodeChildren) syncConfigNode.getParentNode().getChildren()).addNotify();

                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, "The DataSourceConfiguration was relesed");
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            
        }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
