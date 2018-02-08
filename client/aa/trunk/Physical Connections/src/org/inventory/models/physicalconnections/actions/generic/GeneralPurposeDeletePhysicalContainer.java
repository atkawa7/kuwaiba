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
 *  under the License.
 */
package org.inventory.models.physicalconnections.actions.generic;

import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * General Purpose version of the Deletes a physical link
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class GeneralPurposeDeletePhysicalContainer extends GenericObjectNodeAction implements Presenter.Popup {
    private final JMenuItem popupPresenter;

    public GeneralPurposeDeletePhysicalContainer() {
        putValue(NAME, "Delete Physical Container");
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
       
    @Override
    public String[] getValidators() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        
        ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        if (selectedNode == null)
            JOptionPane.showMessageDialog(null, "You must select a node first");
        else {
            
            if (JOptionPane.showConfirmDialog(null, "This will delete the connection and all its existing children. Are you sure you want to do it?", 
                    "Delete Container", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
                if (CommunicationsStub.getInstance().deletePhysicalConnection(selectedNode.getObject().getClassName(), 
                        selectedNode.getObject().getOid())) {
                    
                    //If the node is in a tree, update the list
                    if (selectedNode.getParentNode() != null && AbstractChildren.class.isInstance(selectedNode.getParentNode().getChildren()))
                        ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                    
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "Container deleted successfully");
                }
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }    
    
    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }

    @Override
    public String[] appliesTo() {
        return new String[] {Constants.CLASS_GENERICPHYSICALCONTAINER};
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}