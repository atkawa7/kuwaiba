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
 *
 */
package org.inventory.navigation.navigationtree.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
/**
 * This class represent an action that has a sub menu of actions grouped
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ActionsGroup extends GenericObjectNodeAction implements Presenter.Popup {
    private final ActionsGroupType.Group actionGroupType;
    private final String iconPath;
    
    public ActionsGroup(String lblAction, String iconPath, ActionsGroupType.Group actionGroupType) {
        putValue(NAME, lblAction != null ? lblAction : "");        
        this.actionGroupType = actionGroupType;
        this.iconPath = iconPath;
    }
    
    public ActionsGroupType.Group getActionGroupType() {
        return actionGroupType;
    }
    
    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GenericObjectNodeAction action = (GenericObjectNodeAction) getValue(((JMenuItem) e.getSource()).getName());
        if (action != null)
            action.actionPerformed(e);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuActionsGroup = new JMenu((String) getValue(NAME));
        if (iconPath != null)
            mnuActionsGroup.setIcon(ImageUtilities.loadImageIcon(iconPath, false));
        
        ObjectNode objectNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        if (objectNode == null) {
            mnuActionsGroup.setEnabled(false);
            return mnuActionsGroup;
        }
        List<GenericObjectNodeAction> actions = new ArrayList<>();
        
        for (GenericObjectNodeAction action : Lookup.getDefault().lookupAll(GenericObjectNodeAction.class)) {
            ActionsGroupType actionsGroupType = action.getClass().getAnnotation(ActionsGroupType.class);
            
            if (actionsGroupType != null && actionsGroupType.group() != null && actionsGroupType.group() == actionGroupType) {
                
                if (action.appliesTo() != null) {
                    for (String className : action.appliesTo()) {
                        if (CommunicationsStub.getInstance().isSubclassOf(objectNode.getObject().getClassName(), className)) {
                            actions.add(action);
                            break;
                        }
                    }
                } else 
                    actions.add(action);                
            }
        }
        if (actions.isEmpty())
            mnuActionsGroup.setVisible(false);            
        else {
            for (GenericObjectNodeAction action : actions) {
                JMenuItem mnuiAction = new JMenuItem((String) action.getValue(NAME));
                mnuiAction.setName((String) action.getValue(NAME));
                putValue(mnuiAction.getName(), action);
                mnuiAction.addActionListener(this);
                mnuiAction.setEnabled(action.isEnabled());
                mnuActionsGroup.add(mnuiAction);
            }
            MenuScroller.setScrollerFor(mnuActionsGroup, 20, 100);
        }
        return mnuActionsGroup;
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
