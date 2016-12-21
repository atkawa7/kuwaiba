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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalReport;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.reports.nodes.ReportNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Adds a custom parameter to the task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class RemoveParameterFromReportAction extends AbstractAction implements Presenter.Popup {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    RemoveParameterFromReportAction() {
        putValue(NAME, "Remove Parameter");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        String parameterName = ((JMenuItem)e.getSource()).getText();
        
        LocalReportLight report = Utilities.actionsGlobalContext().lookup (LocalReportLight.class);
        
        if (com.updateReportParameters(report.getId(), null, new String[] {parameterName})) {
            Utilities.actionsGlobalContext().lookup (ReportNode.class).resetPropertySheet();
            NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Parameter deleted successfully");
        } else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());        
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuParameters = new JMenu("Remove Parameter");
        
        LocalReportLight reportLight = Utilities.actionsGlobalContext().lookup (LocalReportLight.class);
        LocalReport theReport = com.getReport(reportLight.getId());
        
        if (theReport != null) {
            if (theReport.getParameters() == null || theReport.getParameters().isEmpty())
                mnuParameters.setEnabled(false);
            else {
                for (String parameter : theReport.getParameters()) {
                    JMenuItem mnuParameter  = new JMenuItem(this);
                    mnuParameter.setText(parameter);
                    mnuParameters.add(mnuParameter);
                }
            }
        } else mnuParameters.setEnabled(false);
        return mnuParameters;
    }    
}
