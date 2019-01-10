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
import com.neotropic.inventory.modules.projects.nodes.ProjectChildren;
import com.neotropic.inventory.modules.projects.nodes.ProjectNode;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action to add an Activity
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class AddActivityAction extends GenericInventoryAction implements Presenter.Popup {
    private final ResourceBundle bundle;
    private static AddActivityAction instance;
    
    private AddActivityAction() {
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_ADD_ACTIVITY"));
    }
    
    public static AddActivityAction getInstance() {
        return instance == null ? instance = new AddActivityAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ProjectNode.class);
        
        if (selectedNode != null) {
            LocalObjectLight activity = CommunicationsStub.getInstance().addActivity(
                selectedNode.getObject().getId(), 
                selectedNode.getObject().getClassName(), 
                ((JMenuItem)e.getSource()).getText());
            
            if (activity == null) {
                NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_ERROR"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                ((ProjectChildren) selectedNode.getChildren()).addNotify();
                NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_INFORMATION"), 
                    NotificationUtil.INFO_MESSAGE, bundle.getString("LBL_ACTIVITY_CREATE_SUCCESSFULLY"));
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleActivities = new JMenu(this);
        
        ProjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ProjectNode.class);
        
        if (selectedNode != null) {
            List<LocalClassMetadataLight> possibleActivities = CommunicationsStub.getInstance()
                .getLightSubclasses(Constants.CLASS_GENERICACTIVITY, false, true);
            
            if (possibleActivities == null) {
                NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_ERROR"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                if (possibleActivities.isEmpty())
                    mnuPossibleActivities.setEnabled(false);
                else {
                    for (LocalClassMetadataLight possibleActivity : possibleActivities) {
                        JMenuItem mnuPossibleActivity = new JMenuItem(possibleActivity.getClassName());
                        mnuPossibleActivity.addActionListener(this);
                        
                        mnuPossibleActivities.add(mnuPossibleActivity);
                    }
                }
            }
        }
        return mnuPossibleActivities;
    }
}
