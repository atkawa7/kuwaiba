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
import com.neotropic.inventory.modules.projects.nodes.ProjectPoolNode;
import com.neotropic.inventory.modules.projects.nodes.ProjectRootChildren;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;

/**
 * Action to delete a Project Pool
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteProjectPoolAction extends GenericInventoryAction {
    private final ResourceBundle bundle;
    private static DeleteProjectPoolAction instance;
    
    private DeleteProjectPoolAction() {           
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_DELETE_PROJECT_POOL"));
    }
    
    public static DeleteProjectPoolAction getIntance() {
        return instance == null ? instance = new DeleteProjectPoolAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(
            null, bundle.getString("LBL_WARNING_DELETE_PROJECT_POOL"), 
            bundle.getString("LBL_WARNING"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            ProjectPoolNode selectedNode = Utilities.actionsGlobalContext().lookup(ProjectPoolNode.class);
            if (selectedNode == null)
                return;
            if (CommunicationsStub.getInstance().deletePool(selectedNode.getPool().getOid())) {
                NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_INFORMATION"), 
                    NotificationUtil.INFO_MESSAGE, bundle.getString("LBL_CONFIRMATION_DELETE_PROJECT_POOL"));
                
                ((ProjectRootChildren) selectedNode.getParentNode().getChildren()).addNotify();
            } else {
                NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_ERROR"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }
    
}
