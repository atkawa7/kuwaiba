/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.core.config.variables.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.kuwaiba.core.config.variables.nodes.ConfigurationVariablesPoolNode;
import org.kuwaiba.core.config.variables.nodes.ConfigurationVariablesRootNode;
import org.openide.util.Utilities;

/**
 * Deletes a pool of configuration variables and the variables within
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DeleteConfigurationVariablesPoolAction extends GenericInventoryAction {

    public DeleteConfigurationVariablesPoolAction() {
        putValue(NAME, "Delete Configuration Variables Pool");
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONFIG_VARIABLES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this pool? All the variables contained within will be deleted as well.", 
                "Delete Pool", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) 
            return;
        
        ConfigurationVariablesPoolNode selectedNode = Utilities.actionsGlobalContext().lookup(ConfigurationVariablesPoolNode.class);
        if (selectedNode != null) {
            
            if (CommunicationsStub.getInstance().deleteConfigurationVariablesPool(selectedNode.getLookup().lookup(LocalPool.class).getId()))
                ((ConfigurationVariablesRootNode.ConfigurationVariablesRootChildren)selectedNode.getParentNode().getChildren()).addNotify();
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        
    }
}
