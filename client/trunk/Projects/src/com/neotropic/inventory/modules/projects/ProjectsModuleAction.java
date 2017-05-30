/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 */
package com.neotropic.inventory.modules.projects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action to open the top component <code>ProjectsModuleTopComponent</code>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ActionID(
        category = "Tools",
        id = "com.neotropic.inventory.modules.projects.ProjectsModuleAction"
)
@ActionRegistration(
        iconBase = "com/neotropic/inventory/modules/projects/res/icon.png",
        displayName = "#CTL_ProjectsModuleAction"
)
@ActionReference(path = "Menu/Tools/Advanced", position = 3333)
@Messages("CTL_ProjectsModuleAction=Projects")
public final class ProjectsModuleAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalPool projectsRootPool = CommunicationsStub.getInstance().getProjectsRootPool(Constants.CLASS_GENERICPROJECT);
        
        if (projectsRootPool == null) {
            JOptionPane.showMessageDialog(null, "Projects Module not available \nContact your administrator to Apply Patch for the Projects Module", "Apply Patch", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ProjectsModuleTopComponent tc = ProjectsModuleTopComponent.getInstance();
        tc.open();
        tc.requestActive();
    }
}
