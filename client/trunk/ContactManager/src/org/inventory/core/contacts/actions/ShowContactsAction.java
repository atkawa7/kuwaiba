/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.contacts.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Shows a list of contacts associated to a customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ShowContactsAction extends GenericObjectNodeAction {
    
    public ShowContactsAction() {
        this.putValue(NAME, "Show Contacts"); 
    }

    @Override
    public String[] appliesTo() {
        return new String[] { "GenericCustomer" }; //NOI18N        
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONTACTS, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
               
        JOptionPane.showMessageDialog(null, "Not implemented yet");
    }

    @Override
    public String[] getValidators() {
        return null;
    }
}
