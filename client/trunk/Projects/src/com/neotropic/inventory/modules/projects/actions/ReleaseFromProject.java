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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action to release an object associated with a project
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseFromProject extends GenericObjectNodeAction implements Presenter.Popup {
    private final ResourceBundle bundle;
    public final String CLNT_PROPERTY_PROJECT_CLASS = "projectClass";
    public final String CLNT_PROPERTY_PROJECT_ID = "projectId";
    
    public ReleaseFromProject() {
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_RELEASE_FROM_PROJECT"));        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        
        JMenuItem eventSource = (JMenuItem) e.getSource();
        String objectClass = selectedObjects.get(0).getClassName();
        long objectId = selectedObjects.get(0).getOid();
        String projectClass = (String) eventSource.getClientProperty(CLNT_PROPERTY_PROJECT_CLASS);
        long projectId = (long) eventSource.getClientProperty(CLNT_PROPERTY_PROJECT_ID);
        
        if (CommunicationsStub.getInstance().releaseObjectFromProject(objectClass, objectId, projectClass, projectId)) {
            NotificationUtil.getInstance().showSimplePopup(
                bundle.getString("LBL_SUCCESS"), 
                NotificationUtil.INFO_MESSAGE, 
                bundle.getString("LBL_RELEASE_OBJECT"));
        } else {
            NotificationUtil.getInstance().showSimplePopup(
                bundle.getString("LBL_ERROR"), 
                NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
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

    @Override
    public JMenuItem getPopupPresenter() {
        
        JMenu mnuProjects = new JMenu(this);
        
        ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        if (selectedNode != null) {
            
            List<LocalObjectLight> projects = CommunicationsStub.getInstance().getProjectsAssociateToObject(
                selectedNode.getObject().getClassName(), 
                selectedNode.getObject().getOid()
            );
            
            if (projects != null) {
                if (!projects.isEmpty()) {
                    for (LocalObjectLight project : projects) {
                        JMenuItem mnuProject = new JMenuItem(project.toString());
                        mnuProject.putClientProperty(CLNT_PROPERTY_PROJECT_CLASS, project.getClassName()); //NOI18N
                        mnuProject.putClientProperty(CLNT_PROPERTY_PROJECT_ID, project.getOid()); //NOI18N
                        mnuProject.addActionListener(this);
                        mnuProjects.add(mnuProject);
                    }
                } else {
                    mnuProjects.setEnabled(false);
                }
            } else {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            }
        }
        return mnuProjects;
    }
}
