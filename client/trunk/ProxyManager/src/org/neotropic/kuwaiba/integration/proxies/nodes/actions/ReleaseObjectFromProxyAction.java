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
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
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
 * Action to release an object associated to a proxy
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELEASE_FROM)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseObjectFromProxyAction extends GenericObjectNodeAction implements ComposedAction {
    
    public ReleaseObjectFromProxyAction() {
        putValue(NAME, "Proxy...");        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0);
        
        List<LocalObjectLight> proxies = CommunicationsStub.getInstance()
            .getSpecialAttribute(selectedObject.getClassName(), selectedObject.getId(), "hasProxy");
        
        if (proxies != null) {
            if (!proxies.isEmpty()) {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight proxy : proxies) {
                    SubMenuItem subMenuItem = new SubMenuItem(proxy.toString());
                    subMenuItem.addProperty(Constants.PROPERTY_CLASSNAME, proxy.getClassName());
                    subMenuItem.addProperty(Constants.PROPERTY_ID, proxy.getId());
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            } else {
                JOptionPane.showMessageDialog(null, "There are no proxies related to the selected object", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROXIES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            SubMenuDialog eventSource = (SubMenuDialog) e.getSource();
            String objectClass = selectedObjects.get(0).getClassName();
            String objectId = selectedObjects.get(0).getId();
            String projectClass = (String) eventSource.getSelectedSubMenuItem().getProperty(Constants.PROPERTY_CLASSNAME);
            String projectId = (String) eventSource.getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID);

            if (CommunicationsStub.getInstance().releaseObjectFromProxy(objectClass, objectId, projectClass, projectId)) {
                NotificationUtil.getInstance().showSimplePopup(
                    "Success", 
                    NotificationUtil.INFO_MESSAGE, 
                    "The object was released successfully");
            } else {
                NotificationUtil.getInstance().showSimplePopup(
                    "Error", 
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
