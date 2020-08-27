/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.special.children.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * Action that deletes an spacial object business, for now it is restricted only 
 * to VLANs 
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class DeleteVlanObjectAction extends GenericObjectNodeAction  implements Presenter.Popup 
{
    private final JMenuItem popupPresenter;
    
    public DeleteVlanObjectAction() {
        popupPresenter = new JMenuItem(I18N.gm("delete"), ImageIconResource.WARNING_ICON);
        popupPresenter.addActionListener(this);
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {
        if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this object? (all children will be removed as well)", 
                I18N.gm("warning"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
            
            if (!selectedNodes.hasNext())
                return;
            
            ArrayList<String> classNames = new ArrayList<>();
            ArrayList<String> oids = new ArrayList<>();
            HashSet<Node> parents = new HashSet<>();
            
            while (selectedNodes.hasNext()) {
                ObjectNode selectedNode = (ObjectNode)selectedNodes.next();
                classNames.add(selectedNode.getObject().getClassName());
                oids.add(selectedNode.getObject().getId());
                if (selectedNode.getParentNode() != null)
                    parents.add(selectedNode.getParentNode());
            }
                        
            if (CommunicationsStub.getInstance().deleteVLANs(oids)){
                for (Node parent : parents) {
                    if (AbstractChildren.class.isInstance(parent.getChildren()))
                        ((AbstractChildren)parent.getChildren()).addNotify();
                }
                
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), 
                        NotificationUtil.INFO_MESSAGE, "The element was deleted successfully");
            }
            else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public String[] appliesTo() {
        return new String[] {Constants.CLASS_VLAN};
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SPECIAL_EXPLORERS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
    @Override
    public int numberOfNodes() {
        return -1;
    }
}
