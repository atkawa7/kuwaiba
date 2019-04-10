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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalConfigurationVariable;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.kuwaiba.core.config.variables.nodes.ConfigurationVariablesPoolNode;
import org.openide.util.Utilities;

/**
 * Adds a configuration variable
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AddConfigurationVariableAction extends GenericInventoryAction {

    public AddConfigurationVariableAction() {
        putValue(NAME, "Add Configuration Variable");
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONFIG_VARIABLES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ConfigurationVariablesPoolNode selectedNode = Utilities.actionsGlobalContext().lookup(ConfigurationVariablesPoolNode.class);
        if (selectedNode != null) {
            
            JTextField txtName = new JTextField();
            txtName.setColumns(35);
            
            JTextField txtDescription = new JTextField();
            txtDescription.setColumns(35);
            
            JTextField txtValue = new JTextField();
            txtValue.setColumns(35);
            
            JCheckBox chkMasked = new JCheckBox();
            
            JComboBox<ConfigVariableType> cmbTypes = new JComboBox<>(new ConfigVariableType[] { new ConfigVariableType("String", LocalConfigurationVariable.TYPE_STRING),
                                                                     new ConfigVariableType("Integer", LocalConfigurationVariable.TYPE_INTEGER),
                                                                     new ConfigVariableType("Float", LocalConfigurationVariable.TYPE_FLOAT),
                                                                     new ConfigVariableType("Boolean", LocalConfigurationVariable.TYPE_BOOLEAN),    
                                                                     new ConfigVariableType("Array", LocalConfigurationVariable.TYPE_ARRAY),    
                                                                     new ConfigVariableType("Table", LocalConfigurationVariable.TYPE_MATRIX)
                                                                     });
            cmbTypes.setSelectedIndex(0);
            
            JComplexDialogPanel pnlConfigVariable = new JComplexDialogPanel(new String[] { I18N.gm("name"), I18N.gm("description"), "Value", "Masked", I18N.gm("type") } , 
                    new JComponent[] { txtName, txtDescription, txtValue, chkMasked, cmbTypes });
            
            if (JOptionPane.showConfirmDialog(null, pnlConfigVariable, "New Configuration Variable", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (txtName.getText().trim().isEmpty())
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, "The name of the configuration variable can not be empty");
                else {
                    if (CommunicationsStub.getInstance().createConfigurationVariable(
                            selectedNode.getLookup().lookup(LocalPool.class).getId(), txtName.getText().trim(), txtDescription.getText(), 
                            ((ConfigVariableType)cmbTypes.getSelectedItem()).getType(), chkMasked.isSelected(), txtValue.getText().trim()) != null) 
                        ((ConfigurationVariablesPoolNode.ConfigurationVariablesPoolNodeChildren)selectedNode.getChildren()).addNotify();
                    else
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
                
            }
        }
    }
    
    /**
     * Dummy class to be used in the config variable type combo box
     */
    private class ConfigVariableType {
        private String displayName;
        private int type;

        public ConfigVariableType(String displayName, int type) {
            this.displayName = displayName;
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getType() {
            return type;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
        
    }

}
