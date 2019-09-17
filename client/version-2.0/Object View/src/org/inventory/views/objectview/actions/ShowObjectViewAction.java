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

package org.inventory.views.objectview.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.views.objectview.ObjectViewTopComponent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * This action opens an Object View for the selected node
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.OPEN_VIEW)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ShowObjectViewAction extends GenericObjectNodeAction {

    public ShowObjectViewAction() {
        putValue(NAME, "Object View");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedObjects.size() != 1)
            JOptionPane.showMessageDialog(null, "Select only one object.", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
        else {
            ObjectViewTopComponent objectViewTC = ((ObjectViewTopComponent)WindowManager.getDefault().
                findTopComponent("ObjectViewTopComponent_" + selectedObjects.get(0).getId()));
            
            if (objectViewTC == null) {
                objectViewTC = new ObjectViewTopComponent(selectedObjects.get(0));
                objectViewTC.open();
            } else {
                if (objectViewTC.isOpened()) 
                    objectViewTC.requestAttention(true);
                else  //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                      //so we will reuse the instance, taking into account that the TC.componentOpen will re-render the view.
                    objectViewTC.open();
            }
            objectViewTC.requestActive();
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null; //Available for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}
