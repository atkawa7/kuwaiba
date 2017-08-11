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

package org.inventory.views.objectview.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.views.objectview.ObjectViewTopComponent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * This action opens an Object View for the selected node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
//@ActionID(category = "Tools", id = "org.inventory.views.objectview.ObjectViewTopComponent")
//@ActionReferences(value = { @ActionReference(path = "Menu/Tools/Views"),
//    @ActionReference(path = "Toolbars/02_Views", position = 1 )})
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ShowObjectViewAction extends GenericObjectNodeAction {

    public ShowObjectViewAction() {
        putValue(NAME, "Show Object View");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedObjects.size() != 1)
            JOptionPane.showMessageDialog(null, "Select only one object.", "Error", JOptionPane.ERROR_MESSAGE);
        else {
            ObjectViewTopComponent objectViewTC = ((ObjectViewTopComponent)WindowManager.getDefault().
                findTopComponent("ObjectViewTopComponent_" + selectedObjects.get(0).getOid()));
            
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
    public String getValidator() {
        return null; //Available for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ);
    }

}
