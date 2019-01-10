/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package com.neotropic.inventory.modules.sync.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalBackgroundJob;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;

/**
 * Action to kill a job
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class KillJobAction extends AbstractAction implements ComposedAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalBackgroundJob> jobs = CommunicationsStub.getInstance().getCurrentJobs();
        
        if (jobs != null) {
            if (!jobs.isEmpty()) {
                List<SubMenuItem> subMenuItems = new ArrayList();
                for (LocalBackgroundJob job : jobs) {
                    SubMenuItem subMenuItem = new SubMenuItem(jobs.toString());                    
                    subMenuItem.addProperty(Constants.PROPERTY_ID, job.getId());
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            } else {
                JOptionPane.showMessageDialog(null, "There are no jobs running", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to kill this job?", I18N.gm("warning"), 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                
                if (CommunicationsStub.getInstance().killJob((long) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID)))
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "The selected job was killed");
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
    
    
}
