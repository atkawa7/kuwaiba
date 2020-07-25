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
package org.neotropic.kuwaiba.integration.proxies.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.Action.NAME;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalInventoryProxy;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.openide.util.Utilities;

/**
 * Action to release a proxy associated to an existing project.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ReleaseProxyFromProjectAction extends GenericInventoryAction implements ComposedAction {
    private LocalInventoryProxy selectedObject;
    public ReleaseProxyFromProjectAction() {
        putValue(NAME, "Release from Project...");        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        selectedObject = Utilities.actionsGlobalContext().lookup(LocalInventoryProxy.class);
        
        List<LocalObjectLight> projects = CommunicationsStub.getInstance()
            .getProjectsAssociateToObject(selectedObject.getClassName(), selectedObject.getId());
        
        if (projects != null) {
            if (!projects.isEmpty()) {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight project : projects) {
                    SubMenuItem subMenuItem = new SubMenuItem(project.toString());
                    subMenuItem.addProperty(Constants.PROPERTY_CLASSNAME, project.getClassName());
                    subMenuItem.addProperty(Constants.PROPERTY_ID, project.getId());
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
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROJECTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            SubMenuDialog eventSource = (SubMenuDialog) e.getSource();
            String objectClass = selectedObject.getClassName();
            String objectId = selectedObject.getId();
            String projectClass = (String) eventSource.getSelectedSubMenuItem().getProperty(Constants.PROPERTY_CLASSNAME);
            String projectId = (String) eventSource.getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID);

            if (CommunicationsStub.getInstance().releaseObjectFromProject(objectClass, objectId, projectClass, projectId)) {
                NotificationUtil.getInstance().showSimplePopup(
                    "Success", 
                    NotificationUtil.INFO_MESSAGE, 
                    "Proxy released successfully");
            } else {
                NotificationUtil.getInstance().showSimplePopup(
                    "Error", 
                    NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            }
        }
    }
}
