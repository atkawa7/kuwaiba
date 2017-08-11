/*
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
 * 
 */
package com.neotropic.inventory.modules.projects.actions;

import com.neotropic.inventory.modules.projects.ProjectsModuleService;
import com.neotropic.inventory.modules.projects.nodes.ActivityNode;
import com.neotropic.inventory.modules.projects.nodes.ProjectChildren;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;

/**
 * Action to delete an Activity
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteActivityAction extends GenericInventoryAction {
    private final ResourceBundle bundle;
    private static DeleteActivityAction instance;
    
    private DeleteActivityAction() {
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_DELETE_ACTIVITY"));
    }
    
    public static DeleteActivityAction getInstance() {
        return instance == null ? instance = new DeleteActivityAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (JOptionPane.showConfirmDialog(null, 
            bundle.getString("MESSAGE_DELETE_ACTIVITY"), bundle.getString("LBL_WARNING"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            Iterator<? extends ActivityNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ActivityNode.class).allInstances().iterator();
            while (selectedNodes.hasNext()) {
                ActivityNode selectedNode = selectedNodes.next();
                
                if (CommunicationsStub.getInstance().deleteActivity(
                    selectedNode.getObject().getClassName(), selectedNode.getObject().getOid())) {
                    
                    ((ProjectChildren) selectedNode.getParentNode().getChildren()).addNotify();
                    
                    NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_INFORMATION"), 
                        NotificationUtil.INFO_MESSAGE, bundle.getString("LBL_ACTIVITY_DELETE_SUCCESSFULLY"));
                } else {
                    NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_ERROR"), 
                        NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            }
        }
    }
    
}
