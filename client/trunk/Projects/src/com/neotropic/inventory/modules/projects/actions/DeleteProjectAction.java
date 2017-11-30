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
import com.neotropic.inventory.modules.projects.nodes.ProjectNode;
import com.neotropic.inventory.modules.projects.nodes.ProjectPoolChildren;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.ResourceBundle;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Action to delete a Project
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteProjectAction extends GenericInventoryAction implements Presenter.Popup {
    private final ResourceBundle bundle;
    private static DeleteProjectAction instance;
    private final JMenuItem popupPresenter;
    
    private DeleteProjectAction() {           
        bundle = ProjectsModuleService.bundle;
        
        putValue(NAME, bundle.getString("ACTION_NAME_DELETE_PROJECT"));
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
    
    public static DeleteProjectAction getIntance() {
        return instance == null ? instance = new DeleteProjectAction() : instance;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, 
            bundle.getString("MESSAGE_DELETE_PROJECT"), I18N.gm("warning"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            Iterator<? extends ProjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ProjectNode.class).allInstances().iterator();
            while (selectedNodes.hasNext()) {
                ProjectNode selectedNode = selectedNodes.next();
                
                if (CommunicationsStub.getInstance().deleteProject(selectedNode.getObject().getClassName(), selectedNode.getObject().getOid())) {
                    
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                        NotificationUtil.INFO_MESSAGE, bundle.getString("LBL_PROJECT_DELETE_SUCCESSFULLY"));
                                        
                    ((ProjectPoolChildren) selectedNode.getParentNode().getChildren()).addNotify();
                } else {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
}
