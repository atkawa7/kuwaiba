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
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.kuwaiba.core.config.variables.nodes.ConfigurationVariablesRootNode;
import org.openide.util.Utilities;

/**
 * Adds a pool con configuration variables
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AddConfigurationVariablesPoolAction extends GenericInventoryAction {

    public AddConfigurationVariablesPoolAction() {
        putValue(NAME, "Add Configuration Variables Pool");
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONFIG_VARIABLES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigurationVariablesRootNode selectedNode = Utilities.actionsGlobalContext().lookup(ConfigurationVariablesRootNode.class);
        if (selectedNode != null) {
            
            JTextField txtName = new JTextField();
            txtName.setColumns(35);
            
            JTextField txtDescription = new JTextField();
            txtDescription.setColumns(35);
            
            JComplexDialogPanel pnlNewPool = new JComplexDialogPanel(new String[] { I18N.gm("name"), I18N.gm("description") } , new JComponent[] { txtName, txtDescription });
            
            if (JOptionPane.showConfirmDialog(null, pnlNewPool, "New Pool", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (txtName.getText().trim().isEmpty())
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, "The name of the pool can not be empty");
                else {
                    if (CommunicationsStub.getInstance().createConfigurationVariablesPool(txtName.getText().trim(), txtDescription.getText()) != null) 
                        ((ConfigurationVariablesRootNode.ConfigurationVariablesRootChildren)selectedNode.getChildren()).addNotify();
                    else
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
                
            }
        }
    }

}
