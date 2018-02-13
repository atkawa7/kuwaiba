/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.management.services.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
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
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.kuwaiba.management.services.nodes.ServiceChildren;
import org.kuwaiba.management.services.nodes.ServiceNode;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action releases de relationship between the object and the service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELEASE_FROM)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseFromServiceAction extends GenericObjectNodeAction implements ComposedAction {
    
    public ReleaseFromServiceAction() {
        putValue(NAME, I18N.gm("release_from_service"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObject = selectedObjects.get(0); //Uses the last selected only
        
        List<LocalObjectLight> services = CommunicationsStub.getInstance().
            getSpecialAttribute(selectedObject.getClassName(), selectedObject.getOid(), "uses"); //NOI18N
        
        if (services != null) {
            if (!services.isEmpty()) {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight service : services) {
                    SubMenuItem subMenuItem = new SubMenuItem(service.toString());                    
                    subMenuItem.addProperty(Constants.PROPERTY_ID, service.getOid());
                    subMenuItem.addProperty(Constants.PROPERTY_CLASSNAME, service.getClassName());
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            } else {
                JOptionPane.showMessageDialog(null, "There are not services related to the selected object", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to release this service?", I18N.gm("warning"), 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();

                boolean success = true;
                while (selectedNodes.hasNext()) {
                    ObjectNode selectedNode = selectedNodes.next();
                    if (CommunicationsStub.getInstance().releaseObjectFromService(
                        (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_CLASSNAME), 
                        (long) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID), 
                        selectedNode.getObject().getOid())) {
                        
                        if (selectedNode.getParentNode() instanceof ServiceNode)
                            ((ServiceChildren)selectedNode.getParentNode().getChildren()).addNotify();
                    } else {
                        success = false;
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    }
                }

                if (success)
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, "The selected resources were released from the service");
            }
        }
    }

    @Override
    public String[] getValidators() {
        return null; //Enable this action for any object
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return null;  //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}