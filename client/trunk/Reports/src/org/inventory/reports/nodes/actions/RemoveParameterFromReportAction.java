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
import java.util.List;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalReport;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.reports.nodes.ReportNode;
import org.openide.util.Utilities;

/**
 * Adds a custom parameter to the task
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class RemoveParameterFromReportAction extends GenericInventoryAction implements ComposedAction {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    RemoveParameterFromReportAction() {
        putValue(NAME, "Remove Parameter...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalReportLight reportLight = Utilities.actionsGlobalContext().lookup(LocalReportLight.class);
        LocalReport theReport = com.getReport(reportLight.getId());
        
        if (theReport != null) {            
            if (theReport.getParameters() == null || theReport.getParameters().isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no parameters to the selected report", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (String parameter : theReport.getParameters())
                    subMenuItems.add(new SubMenuItem(parameter));
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            }
        } else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_REPORTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            String parameterName = ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getCaption();

            LocalReportLight report = Utilities.actionsGlobalContext().lookup (LocalReportLight.class);

            if (com.updateReportParameters(report.getId(), null, new String[] {parameterName})) {
                Utilities.actionsGlobalContext().lookup (ReportNode.class).resetPropertySheet();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Parameter deleted successfully");
            } else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }
}
