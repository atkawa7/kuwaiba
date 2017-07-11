/**
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
import org.openide.util.lookup.ServiceProvider;

/**
 * Opens an end-to-end view of the service, by trying to match the endpoints of the 
 * logical circuits directly associated to the selected instance
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class ShowEndToEndViewAction extends GenericObjectNodeAction {

    public ShowEndToEndViewAction() {
        putValue(NAME, "Show End-to-End View");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (selectedObjects.isEmpty()) 
            JOptionPane.showMessageDialog(null, "This action can not be executed here", "Information", JOptionPane.INFORMATION_MESSAGE);
        else {
            
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