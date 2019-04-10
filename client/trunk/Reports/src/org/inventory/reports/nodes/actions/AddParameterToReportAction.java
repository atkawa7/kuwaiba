/**
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
package org.inventory.reports.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.reports.nodes.ReportNode;
import org.openide.util.Utilities;

/**
 * Adds a parameter to a report
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class AddParameterToReportAction extends GenericInventoryAction {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public AddParameterToReportAction() {
        putValue(NAME, "Add Parameter");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField txtParameterName = new JTextField(15);
        txtParameterName.setName("txtParameterName"); //NOI18N
        
        JComplexDialogPanel pnlForm = new JComplexDialogPanel(new String[] {"Parameter Name"}, 
                                                    new JComponent[] {txtParameterName});
        
        if (JOptionPane.showConfirmDialog(null, pnlForm, "New Parameter", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String parameterName = ((JTextField)pnlForm.getComponent("txtParameterName")).getText(); //NOI18N
            if (parameterName.isEmpty())
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "Parameter names can not be empty strings");
            else {
                ReportNode selectedNode = Utilities.actionsGlobalContext().lookup(ReportNode.class);

                if (com.updateReportParameters(selectedNode.getLookup().lookup(LocalReportLight.class).getId(), 
                        new String[] {parameterName}, null)) {
                    selectedNode.resetPropertySheet();
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Parameter added successfully");
                }
                else
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            }
        }
        
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_REPORTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
