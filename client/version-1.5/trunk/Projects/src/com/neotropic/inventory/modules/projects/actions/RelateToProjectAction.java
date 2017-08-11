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
package com.neotropic.inventory.modules.projects.actions;

import com.neotropic.inventory.modules.projects.ProjectsModuleService;
import com.neotropic.inventory.modules.projects.windows.ProjectsFrame;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action to relate an object to a Project
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToProjectAction extends GenericObjectNodeAction {
    private final ResourceBundle bundle;
    
    public RelateToProjectAction() {
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_RELATE_TO_PROJECT"));        
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> projects = ProjectsModuleService.getAllProjects();
        if (projects == null) {
            JOptionPane.showMessageDialog(null, "This database seems outdated. Contact your administrator to apply the necessary patches to run the Projects module", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            if (projects.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no projects created. Create at least one using the Projects Module", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                ProjectsFrame projectsFrame = new ProjectsFrame(selectedObjects, projects);
                projectsFrame.setVisible(true);
            }
        }
    }

    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
