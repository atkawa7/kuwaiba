/**
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
 */
package com.neotropic.inventory.modules.projects.actions;

import com.neotropic.inventory.modules.projects.ProjectsModuleService;
import com.neotropic.inventory.modules.projects.nodes.ProjectRootChildren;
import com.neotropic.inventory.modules.projects.nodes.ProjectRootNode;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.util.Utilities;

/**
 * Action to create a Project Pool
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CreateProjectPoolAction extends GenericInventoryAction {
    private final ResourceBundle bundle;
    private static CreateProjectPoolAction instance;
    
    private CreateProjectPoolAction() {
        bundle = ProjectsModuleService.bundle;
        putValue(NAME, bundle.getString("ACTION_NAME_ADD_PROJECT_POOL"));
    }
    
    public static CreateProjectPoolAction getInstance() {
        return instance == null ? instance = new CreateProjectPoolAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectRootNode selectedNode = Utilities.actionsGlobalContext().lookup(ProjectRootNode.class);
        if (selectedNode == null)
            return;
        
        List<LocalClassMetadataLight> possibleProjectClasses = CommunicationsStub
            .getInstance().getLightSubclasses(Constants.CLASS_GENERICPROJECT, true, true);
        if (possibleProjectClasses == null) 
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            JTextField txtProjPoolName = new JTextField();
            txtProjPoolName.setName("txtProjPoolName");
            txtProjPoolName.setColumns(10);
                    
            JComboBox cmbProjPoolType = new JComboBox(possibleProjectClasses.toArray(new LocalClassMetadataLight[0]));
            cmbProjPoolType.setName("cmbProjPoolType");
            
            JTextField txtProjPoolDescription = new JTextField();
            txtProjPoolDescription.setName("txtProjPoolDescription");
            txtProjPoolDescription.setColumns(10);
            
            String lblName = bundle.getString("LBL_PROJECT_POOL_NAME");
            String lblType = bundle.getString("LBL_PROJECT_POOL_TYPE");
            String lblDescription = bundle.getString("LBL_PROJECT_POOL_DESCRIPTION");
            
            JComplexDialogPanel pnlProjPoolProperties = new JComplexDialogPanel(
                new String[] {lblName, lblType, lblDescription}, 
                new JComponent[] {txtProjPoolName, cmbProjPoolType, txtProjPoolDescription});
            
            String title = bundle.getString("TITLE_NEW_PROJECT_POOL");
            
            if (JOptionPane.showConfirmDialog(null, pnlProjPoolProperties, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                LocalClassMetadataLight projPoolType = (LocalClassMetadataLight) ((JComboBox) pnlProjPoolProperties.getComponent("cmbProjPoolType")).getSelectedItem();
                                
                LocalPool newProjPool = CommunicationsStub.getInstance().createProjectPool(
                    ((JTextField) pnlProjPoolProperties.getComponent("txtProjPoolName")).getText(), 
                    ((JTextField) pnlProjPoolProperties.getComponent("txtProjPoolDescription")).getText(), 
                    projPoolType == null ? Constants.CLASS_GENERICPROJECT : projPoolType.getClassName());
                                
                if (newProjPool == null) {
                    NotificationUtil.getInstance().showSimplePopup(bundle.getString("LBL_ERROR"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                } else {
                    ((ProjectRootChildren) selectedNode.getChildren()).addNotify();
                    NotificationUtil.getInstance().showSimplePopup(
                        bundle.getString("LBL_INFORMATION"), 
                        NotificationUtil.INFO_MESSAGE, 
                        bundle.getString("LBL_PROJ_POOL_CREATE_SUCCESSFULLY"));
                }
            }
        }
    }
}
