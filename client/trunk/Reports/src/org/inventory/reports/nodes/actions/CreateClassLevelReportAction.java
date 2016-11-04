/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.reports.nodes.ReportsModuleClassNode;
import org.openide.util.Utilities;

/**
 * Creates a class level report
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class CreateClassLevelReportAction extends AbstractAction {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    CreateClassLevelReportAction() {
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
            
            ReportsModuleClassNode selectedNode = Utilities.actionsGlobalContext().lookup(ReportsModuleClassNode.class);
            LocalClassMetadataLight selectedObject = selectedNode.getLookup().lookup(LocalClassMetadataLight.class);
            
            LocalReportLight newTemplate = com.createClassLevelReport(selectedObject.getClassName(), 
                                                ((JTextField)pnlGeneralInfo.getComponent("txtReportName")).getText(),
                                                ((JTextField)pnlGeneralInfo.getComponent("txtReportDescription")).getText(), 
                                                "", LocalReportLight.TYPE_HTML, true);
            
            if (newTemplate == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((ReportsModuleClassNode.ReportsModuleClassChildren)selectedNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Report created successfully");
            }
        }
    }
}
