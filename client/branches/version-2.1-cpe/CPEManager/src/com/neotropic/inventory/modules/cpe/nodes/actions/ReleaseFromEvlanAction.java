/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.inventory.modules.cpe.nodes.actions;

import com.neotropic.inventory.modules.cpe.nodes.EvlanPoolNode;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.RELEASE_FROM)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseFromEvlanAction extends GenericObjectNodeAction implements ComposedAction{
    
    public ReleaseFromEvlanAction() {
        putValue(NAME, I18N.gm("modules.cpe.nodes.actions.ReleaseFromEvlanAction.name"));
    }

    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
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
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CPE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalObjectLight selectedObj = selectedObjects.get(0); //Uses the last selected only
        List<LocalObjectLight> evlans = CommunicationsStub.getInstance()
            .getSpecialAttribute(selectedObj.getClassName(), selectedObj.getId(), "FTTH_EVLANHas"); //NOI18N
        
        if (evlans == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            if (evlans.isEmpty())
                JOptionPane.showMessageDialog(null, I18N.gm("modules.cpe.nodes.actions.ReleaseFromEvlanAction.related"), 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            else {
                List<SubMenuItem> subMenuItems = new ArrayList<>();
                for (LocalObjectLight evlan : evlans) {
                    SubMenuItem subMenuItem = new SubMenuItem(evlan.toString());
                    subMenuItem.addProperty(Constants.PROPERTY_CLASSNAME, evlan.getClassName());
                    subMenuItem.addProperty(Constants.PROPERTY_ID, evlan.getId());
                    subMenuItems.add(subMenuItem);
                }
                SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
            }
        }
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            if (JOptionPane.showConfirmDialog(null, 
                    I18N.gm("modules.cpe.nodes.actions.ReleaseFromEvlanAction.release"), I18N.gm("warning"), 
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();

                boolean success = true;
                while (selectedNodes.hasNext()) {
                    ObjectNode selectedNode = selectedNodes.next();
                    if (CommunicationsStub.getInstance().releaseObjectFromEvlan(selectedNode.getObject().getClassName(), 
                        selectedNode.getObject().getId(), 
                        (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_CLASSNAME), 
                        (String) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID))) {
                        if (selectedNode.getParentNode() instanceof EvlanPoolNode)
                            ((EvlanPoolNode.EvlanPoolChildren)selectedNode.getParentNode().getChildren()).addNotify();
                    } else {
                        success = false;
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    }
                }

                if (success)
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, I18N.gm("modules.cpe.nodes.actions.ReleaseFromEvlanAction.released"));
            }
        }
    }
    
}
