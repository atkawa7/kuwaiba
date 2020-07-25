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
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalInventoryProxy;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Action to relate an inventory proxy to a project.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class RelateProxyToProjectAction extends GenericInventoryAction implements ComposedAction {
    private Collection<? extends LocalInventoryProxy> selectedObjects;
    public RelateProxyToProjectAction() {
        putValue(NAME, "Relate to Project...");        
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {
        selectedObjects = Utilities.actionsGlobalContext().lookupAll(LocalInventoryProxy.class);
        List<LocalObjectLight> projects = CommunicationsStub.getInstance().getAllProjects();
        if (projects == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (projects.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no projects created. Create at least one using the Projects Module", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                SelectValueFrame projectsFrame = new SelectValueFrame("Available Projects", "Select a project from the list", "Create Relationship", projects);
                projectsFrame.addListener(this);
                projectsFrame.setVisible(true);
            }
        }
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
                    String objId = selectedObject.getId();
                    String objClassName = selectedObject.getClassName();
                    
                    String projectId = ((LocalObjectLight) selectedValue).getId();
                    String projectClass = ((LocalObjectLight) selectedValue).getClassName();
                    
                    if (CommunicationsStub.getInstance().associateObjectToProject(projectClass, projectId, objClassName, objId)) {
                        JOptionPane.showMessageDialog(null, String.format("%s related to project %s", selectedObject, selectedValue));
                        frame.dispose();
                    } else
                        JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
