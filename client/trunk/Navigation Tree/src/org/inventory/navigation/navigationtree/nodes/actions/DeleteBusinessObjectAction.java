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
package org.inventory.navigation.navigationtree.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;

/**
 * Action to delete a business object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class DeleteBusinessObjectAction extends CallbackSystemAction {
   
    public static String ACTION_MAP_KEY = "DeleteBusinessObject"; //NOI18N
    private final JMenuItem popupPresenter;

    public DeleteBusinessObjectAction() {
        popupPresenter = new JMenuItem(getName(), ImageIconResource.WARNING_ICON);
        popupPresenter.addActionListener(this);
    }
    
    @Override
    public JMenuItem getPopupPresenter() { 
        super.getPopupPresenter();
        return popupPresenter;
    }
       
    
    @Override
    public void actionPerformed(ActionEvent ev) {

        if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this object? (all children will be removed as well)", 
                I18N.gm("warning"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
            
            if (!selectedNodes.hasNext())
                return;
            
            ArrayList<String> classNames = new ArrayList<>();
            ArrayList<Long> oids = new ArrayList<>();
            HashSet<Node> parents = new HashSet<>();
            
            while (selectedNodes.hasNext()) {
                ObjectNode selectedNode = (ObjectNode)selectedNodes.next();
                classNames.add(selectedNode.getObject().getClassName());
                oids.add(selectedNode.getObject().getOid());
                if (selectedNode.getParentNode() != null)
                    parents.add(selectedNode.getParentNode());
            }
                        
            if (CommunicationsStub.getInstance().deleteObjects(classNames, oids)){
                
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
    public String getName() {
        return I18N.gm("delete"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("Delete Business Object"); //NOI18N
    }
    
    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    public Object getActionMapKey() {
        return ACTION_MAP_KEY;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
