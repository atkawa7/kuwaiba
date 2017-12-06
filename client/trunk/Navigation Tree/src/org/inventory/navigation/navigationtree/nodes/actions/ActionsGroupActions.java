/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ActionsGroupActions extends GenericObjectNodeAction implements Presenter.Popup {
    public final Class<?> actionsGroupClass;
    public final String iconPath;
    
    public ActionsGroupActions(String lblAction, String iconPath, Class<?> actionsGroupClass) {
        putValue(NAME, lblAction != null ? lblAction : "");        
        this.actionsGroupClass = actionsGroupClass;
        this.iconPath = iconPath;
    }
    
    public Class<?> getActionsGroupClass() {
        return actionsGroupClass;
    }
    
    @Override
    public String[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GenericObjectNodeAction action = (GenericObjectNodeAction) getValue(((JMenuItem) e.getSource()).getName());
        
        if (action != null) {
            ObjectNode node = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
            List<LocalObjectLight> objects = new ArrayList();
            objects.add(node.getObject());
            action.setSelectedObjects(objects);
            action.actionPerformed(e);
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuActionsGroup = new JMenu((String) getValue(NAME));
        mnuActionsGroup.setIcon(ImageUtilities.loadImageIcon(iconPath, false));
        
        ObjectNode objectNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        if (objectNode == null) {
            mnuActionsGroup.setEnabled(false);
            return mnuActionsGroup;
        }
        List<GenericObjectNodeAction> actions = new ArrayList();
        
        for (Object object : Lookup.getDefault().lookupAll(actionsGroupClass)) {
            if (object instanceof GenericObjectNodeAction) {
                GenericObjectNodeAction action = (GenericObjectNodeAction) object;
                
                if (action.appliesTo() != null) {
                    for (String className : action.appliesTo()) {
                        if (CommunicationsStub.getInstance().isSubclassOf(objectNode.getObject().getClassName(), className)) {
                            actions.add(action);
                            break;
                        }
                    }
                } else {
                    if (action.getValidators() != null) {
                        for (String validator : action.getValidators()) {
                            if (CommunicationsStub.getInstance().getMetaForClass(objectNode.getObject().getClassName(), false).getValidator(validator) == 1) {
                                actions.add(action);
                                break;
                            }
                        }                                                
                    } else {
                        actions.add(action);
                    }                
                }
            }
        }
        if (actions.isEmpty()) {
            mnuActionsGroup.setEnabled(false);            
        } else {
            for (GenericObjectNodeAction action : actions) {
                JMenuItem mnuiAction = new JMenuItem((String) action.getValue(NAME));
                mnuiAction.setName((String) action.getValue(NAME));
                putValue(mnuiAction.getName(), action);
                mnuiAction.addActionListener(this);                
            }
            MenuScroller.setScrollerFor(mnuActionsGroup, 20, 100);
        }
        return mnuActionsGroup;
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
}
