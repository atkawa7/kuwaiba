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
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.reports.nodes.AbstractReportChildren;
import org.inventory.reports.nodes.ReportNode;
import org.openide.util.Utilities;

/**
 * Deletes a class level report
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class DeleteReportAction extends GenericInventoryAction {
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public DeleteReportAction() {
        putValue(NAME, "Delete Report");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this report?", 
                "Delete Report", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                
                ReportNode selectedNode = Utilities.actionsGlobalContext().lookup(ReportNode.class);
                
                if (!com.deleteReport(selectedNode.getLookup().lookup(LocalReportLight.class).getId()))
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                ((AbstractReportChildren)selectedNode.getParentNode().getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Report deleted successfully");
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_REPORTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
