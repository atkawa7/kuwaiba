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
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action to relate an object to a Project
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateToProjectAction extends GenericObjectNodeAction implements ComposedAction {
    private final ResourceBundle bundle;
    
    public RelateToProjectAction() {
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_RELATE_TO_PROJECT"));        
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> projects = ProjectsModuleService.getAllProjects();
        if (projects == null) {
            JOptionPane.showMessageDialog(null, "This database seems outdated. Contact your administrator to apply the necessary patches to use the Projects module", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
        } else {
            if (projects.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no projects created. Create at least one using the Projects Module", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                SelectValueFrame projectsFrame = new SelectValueFrame(ProjectsModuleService.bundle.getString("LBL_TITLE_AVAILABLE_PROJECTS"), ProjectsModuleService.bundle.getString("LBL_INSTRUCTIONS_SELECT_PROJECTS"), "Create Relationship", projects);
                projectsFrame.addListener(this);
                projectsFrame.setVisible(true);
            }
        }
    }

    @Override
    public String[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof SelectValueFrame) {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedValue = frame.getSelectedValue();
            
            if (selectedValue == null)
                JOptionPane.showMessageDialog(null, "Select a project from the list");
            else {
                for (LocalObjectLight selectedObject : selectedObjects) {
                    long objId = selectedObject.getOid();
                    String objClassName = selectedObject.getClassName();
                    
                    long projectId = ((LocalObjectLight) selectedValue).getOid();
                    String projectClass = ((LocalObjectLight) selectedValue).getClassName();
                    
                    if (CommunicationsStub.getInstance().associateObjectToProject(projectClass, projectId, objClassName, objId)) {
                        
                        JOptionPane.showMessageDialog(null, String.format("%s added to project %s", selectedObject, selectedValue));
                        frame.dispose();
                    } else
                        JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
