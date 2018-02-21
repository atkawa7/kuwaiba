/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalReport;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.util.Utilities;

/**
 * Executes a class level report
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class ExecuteInventoryLevelReportAction extends GenericInventoryAction {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public ExecuteInventoryLevelReportAction() {
        putValue(NAME, "Execute Report");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        LocalReportLight selectedReport = Utilities.actionsGlobalContext().lookup(LocalReportLight.class);
        
        LocalReport report = com.getReport(selectedReport.getId());
        
        if (report == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            if (report.getParameters().isEmpty())
                saveReport(report.getId(), com.executeInventoryLevelReport(report.getId(), new HashMap<String, String>()));
            else {
                List<JTextField> components = new ArrayList<>();
                
                for (String parameter : report.getParameters()) {
                    JTextField aTextField = new JTextField(10);
                    aTextField.setName(parameter);
                    components.add(aTextField);
                }
                
                JComplexDialogPanel pnlParameters = new JComplexDialogPanel(report.getParameters().
                        toArray(new String[0]), components.toArray(new JTextField[0]));
                
                if (JOptionPane.showConfirmDialog(null, pnlParameters, "Execute Report", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    HashMap<String, String> parameters = new HashMap<>();
                    
                    for (String parameter : report.getParameters()) 
                        parameters.put(parameter, ((JTextField)pnlParameters.getComponent(parameter)).getText());
                    
                    saveReport(report.getId(), com.executeInventoryLevelReport(report.getId(), parameters));
                }
            }
        }
    }
    
    private void saveReport(long reportId, byte[] reportAsByteArray) {
        if (reportAsByteArray == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {           
            try {
                File tempFile = File.createTempFile("inventory_report_" + reportId + "_" + Calendar.getInstance().getTimeInMillis(), ".html"); //NOI18N

                try (FileOutputStream faos = new FileOutputStream(tempFile)) {
                    faos.write(reportAsByteArray);
                    faos.flush();
                }
                if (Desktop.isDesktopSupported()) 
                try {
                    Desktop.getDesktop().browse(Utilities.toURI(tempFile));
                } catch (IOException ex) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                }

            } catch (IOException ex) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }
        }
           
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_REPORTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
