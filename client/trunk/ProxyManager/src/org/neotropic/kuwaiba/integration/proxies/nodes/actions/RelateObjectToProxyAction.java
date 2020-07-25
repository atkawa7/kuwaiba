/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.integration.proxies.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalInventoryProxy;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Relates an inventory object to an inventory proxy.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELATE_TO)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class RelateObjectToProxyAction extends GenericObjectNodeAction implements ComposedAction  {

    public RelateObjectToProxyAction() {
        putValue(NAME, "Inventory Proxy...");
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }

    @Override
    public int numberOfNodes() {
        return -1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROXIES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalInventoryProxy> proxies = CommunicationsStub.getInstance().getAllProxies();
        if (proxies == null)
            NotificationUtil.getInstance().
                    showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (proxies.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no proxies created. Create at least one using the Inventory Proxy Manager Module", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                SelectValueFrame projectsFrame = new SelectValueFrame("Available Proxies", 
                        "Select an inventory proxy", "Create Relationship", proxies);
                projectsFrame.addListener(this);
                projectsFrame.setVisible(true);
            }
        }
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        SelectValueFrame frame = (SelectValueFrame) e.getSource();
        LocalInventoryProxy selectedValue = (LocalInventoryProxy)frame.getSelectedValue();
        
        if (selectedValue == null)
            JOptionPane.showMessageDialog(null, "Select a proxy from the list");
        else {
            boolean allGood = true;
            for (LocalObjectLight selectedObject : selectedObjects) {
                String objectId = selectedObject.getId();
                String objectClassName = selectedObject.getClassName();

                String proxyId = ((LocalObjectLight) selectedValue).getId();
                String proxyClass = ((LocalObjectLight) selectedValue).getClassName();

                if (CommunicationsStub.getInstance().associateObjectToProxy(objectClassName, objectId, proxyClass, proxyId))
                    frame.dispose();
                else {
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                    allGood = false;
                }
            }
            
            if (allGood)
                JOptionPane.showMessageDialog(null, String.format("%s related to proxy %s sucessfully", 
                        selectedObjects.size() == 1 ? "Object" : "Objects" , selectedValue));

        }
    }

}
