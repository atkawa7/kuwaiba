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
package com.neotropic.inventory.modules.projects.actions;

import com.neotropic.inventory.modules.projects.ProjectsModuleService;
import com.neotropic.inventory.modules.projects.nodes.ProjectPoolChildren;
import com.neotropic.inventory.modules.projects.nodes.ProjectPoolNode;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action to creates a Project
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class AddProjectAction extends GenericInventoryAction implements Presenter.Popup {
    private final ResourceBundle bundle;
    private static AddProjectAction instance;
        
    private AddProjectAction() {
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_ADD_PROJECT"));
    }
    
    public static AddProjectAction getInstance() {
        return instance == null ? instance = new AddProjectAction() : instance;
    }        

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectPoolNode selectedNode = Utilities.actionsGlobalContext().lookup(ProjectPoolNode.class);
        
        if (selectedNode == null)
            return;
        String id = selectedNode.getPool().getId();
        String className = selectedNode.getPool().getClassName();
        
        LocalObjectLight newProject = CommunicationsStub.getInstance().addProject(id, className, ((JMenuItem)e.getSource()).getText());
        
        if (newProject == null) {
            NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_ERROR"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        } else {
            ((ProjectPoolChildren) selectedNode.getChildren()).addNotify();
            NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_INFORMATION"), 
                NotificationUtil.INFO_MESSAGE, bundle.getString("LBL_PROJECT_CREATE_SUCCESSFULLY"));
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleProjects = new JMenu(this);
        
        ProjectPoolNode selectedNode = Utilities.actionsGlobalContext().lookup(ProjectPoolNode.class);
                    
        if (selectedNode != null) {
            List<LocalClassMetadataLight> possibleProjects = CommunicationsStub.getInstance()
                .getLightSubclasses(selectedNode.getPool().getClassName(), false, true);
            
            if (possibleProjects == null) {
                NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_ERROR"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                if (possibleProjects.isEmpty()) {
                } else {
                    for (LocalClassMetadataLight possibleProject : possibleProjects) {
                        JMenuItem mnuPossibleProject = new JMenuItem(possibleProject.getClassName());
                        mnuPossibleProject.addActionListener(this);
                                                
                        mnuPossibleProjects.add(mnuPossibleProject);
                    }
                }
            }
        }
        return mnuPossibleProjects;
    }
}
