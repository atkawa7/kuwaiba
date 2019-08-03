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

import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.inventory.communications.core.LocalPrivilege;
import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Delete sync find the appropriate action to execute the delete action. E.g. If the node
 * are a sync group execute to delete sync group action, or if the node are a data source 
 * configuration execute the action to delete it
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeleteSyncAction extends GenericInventoryAction implements Presenter.Popup{
    public static String ACTION_MAP_KEY = "DeleteSync"; //NOI18N
    private static DeleteSyncAction instance;
    private final JMenuItem popupPresenter;
    
    private DeleteSyncAction() {
        putValue(NAME, I18N.gm("delete"));
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
    
    public static DeleteSyncAction getInstance() {
        return instance == null ? instance = new DeleteSyncAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Utilities.actionsGlobalContext().lookup(LocalSyncGroup.class) != null)
            DeleteSyncGroupAction.getInstance().actionPerformed(e);
        else if (Utilities.actionsGlobalContext().lookup(LocalSyncDataSourceConfiguration.class) != null)
            DeleteSyncDataSourceConfigurationAction.getInstance().actionPerformed(e);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
}
