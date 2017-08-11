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
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.kuwaiba.management.services.nodes.actions.endtoend.EndToEndViewSimpleScene;
import org.kuwaiba.management.services.nodes.actions.endtoend.EndToEndViewTopComponent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Opens an end-to-end view of the service, by trying to match the endpoints of the 
 * logical circuits directly associated to the selected instance
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class ShowEndToEndSimpleViewAction extends GenericObjectNodeAction {

    public ShowEndToEndSimpleViewAction() {
        putValue(NAME, "Show End-to-End View (Simple)");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedObjects.size() != 1)
            JOptionPane.showMessageDialog(null, "Select only one node service.", "Error", JOptionPane.ERROR_MESSAGE);
        else{
            //TopComponent endToEndTC = ((EndToEndViewTopComponent)WindowManager.getDefault().
              //  findTopComponent("ObjectViewTopComponent_" + selectedObjects.get(0).getOid()));
            
            TopComponent endToEndTC = new EndToEndViewTopComponent(selectedObjects.get(0), new EndToEndViewSimpleScene());
            endToEndTC.open();
            endToEndTC.requestActive();
        }
    }
    
    @Override
    public String getValidator() {
        return "service"; //NOI18N
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ);
    }

}
