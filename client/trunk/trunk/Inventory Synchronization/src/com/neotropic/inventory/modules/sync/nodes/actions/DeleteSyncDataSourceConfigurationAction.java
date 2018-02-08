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
package com.neotropic.inventory.modules.sync.nodes.actions;

import com.neotropic.inventory.modules.sync.nodes.SyncConfigurationNode;
import com.neotropic.inventory.modules.sync.nodes.SyncGroupNode.SyncGroupNodeChildren;
import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteSyncDataSourceConfigurationAction extends GenericInventoryAction implements Presenter.Popup {
    private final JMenuItem popupPresenter;
    
    public DeleteSyncDataSourceConfigurationAction() {
        putValue(NAME, I18N.gm("delete"));
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {        
        if (JOptionPane.showConfirmDialog(null, I18N.gm("want_to_delete_sync_data_src_config"), 
            I18N.gm("warning"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            SyncConfigurationNode syncConfigNode = Utilities.actionsGlobalContext().lookup(SyncConfigurationNode.class);
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

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
    
}
