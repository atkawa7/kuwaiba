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
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.reports.nodes.AbstractReportChildren;
import org.inventory.reports.nodes.InventoryLevelReportsRootNode;
import org.openide.util.Utilities;

/**
 * Creates an inventory level report
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class CreateInventoryLevelReportAction extends GenericInventoryAction {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    CreateInventoryLevelReportAction() {
        putValue(NAME, "New Report");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        JTextField txtReportName = new JTextField(20);
        txtReportName.setName("txtReportName"); //NOI18N
        
        JTextField txtReportDescription = new JTextField(20);
        txtReportDescription.setName("txtReportDescription"); //NOI18N
        
        JComplexDialogPanel pnlGeneralInfo = new JComplexDialogPanel(
                                    new String[] { "Name", "Description" }, 
                                    new JComponent[] { txtReportName, txtReportDescription });
        
        if (JOptionPane.showConfirmDialog(null, pnlGeneralInfo, "New Report", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            InventoryLevelReportsRootNode selectedNode = Utilities.actionsGlobalContext().lookup(InventoryLevelReportsRootNode.class);
            
            LocalReportLight newTemplate = com.createInventoryLevelReport(((JTextField)pnlGeneralInfo.getComponent("txtReportName")).getText(),
                                                ((JTextField)pnlGeneralInfo.getComponent("txtReportDescription")).getText(), 
                                                "", LocalReportLight.TYPE_HTML, true, new ArrayList<String>());
            
            if (newTemplate == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((AbstractReportChildren)selectedNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Report created successfully");
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_REPORTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
