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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action to release an object associated with a project
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELEASE_FROM)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseFromProject extends GenericObjectNodeAction implements ComposedAction {
    private final ResourceBundle bundle;
    
    public ReleaseFromProject() {
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_RELEASE_FROM_PROJECT"));        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0);
        
        List<LocalObjectLight> projects = CommunicationsStub.getInstance()
            .getProjectsAssociateToObject(selectedObject.getClassName(), selectedObject.getOid());
        
        if (projects != null) {
            if (!projects.isEmpty()) {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight project : projects) {
                    SubMenuItem subMenuItem = new SubMenuItem(project.toString());
                    subMenuItem.addProperty(Constants.PROPERTY_CLASSNAME, project.getClassName());
                    subMenuItem.addProperty(Constants.PROPERTY_ID, project.getOid());
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            } else {
                JOptionPane.showMessageDialog(null, "There are no projects related to the selected object", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
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
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            SubMenuDialog eventSource = (SubMenuDialog) e.getSource();
            String objectClass = selectedObjects.get(0).getClassName();
            long objectId = selectedObjects.get(0).getOid();
            String projectClass = (String) eventSource.getSelectedSubMenuItem().getProperty(Constants.PROPERTY_CLASSNAME);
            long projectId = (long) eventSource.getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID);

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
