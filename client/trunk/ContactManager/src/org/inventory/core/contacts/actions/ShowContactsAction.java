/**
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
package org.inventory.core.contacts.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.ConnectException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalContact;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.contacts.ContactsTable;
import org.inventory.core.contacts.ContactsTableModel;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Shows a list of contacts associated to a customer
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
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
        LocalObjectLight selectedObject = selectedNode.getLookup().lookup(LocalObjectLight.class);
                
        List<LocalContact> contactsForCustomer = CommunicationsStub.getInstance().getContactsForCustomer(selectedObject.getClassName(), selectedObject.getId());
        
        if (contactsForCustomer == null) 
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError()); //NOI18N
        else {
            if (contactsForCustomer.isEmpty()){
                JOptionPane.showMessageDialog(null, "No contacts were found for this customer", I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                ContactsTable tblContacts = new ContactsTable(new ContactsTableModel(contactsForCustomer));
                JFrame wdwContacts = new JFrame(String.format("Contacts for %s", selectedObject));
                wdwContacts.setLocationRelativeTo(null);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                wdwContacts.setSize(screenSize.width, screenSize.height / 3);
                wdwContacts.setLayout(new BorderLayout());
                wdwContacts.add(new JScrollPane(tblContacts));
                wdwContacts.setVisible(true);
            } catch(ConnectException ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage()); //NOI18N
            } 
        }        
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }
}
