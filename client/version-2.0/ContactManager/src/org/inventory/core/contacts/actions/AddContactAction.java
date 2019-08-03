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

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;


/**
 * Adds a contact to a customer
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class AddContactAction extends GenericObjectNodeAction {

    public AddContactAction() {
        this.putValue(NAME, "Add Contact"); 
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
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_CONTACTS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
               
        List<LocalClassMetadataLight> contactTypes = CommunicationsStub.getInstance().getLightSubclasses("GenericContact", false, false); //NOI18N
        
        if (contactTypes == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        
        JTextField txtContactName = new JTextField();
        txtContactName.setName("txtContactName"); //NOI18N
        JComboBox cmbContactTypes = new JComboBox(contactTypes.toArray());
        cmbContactTypes.setName("cmbContactTypes"); //NOI18N
        
        JComplexDialogPanel pnlNewContact = new JComplexDialogPanel(new String[] { "Name", "Type" }, 
                new JComponent[] { txtContactName, cmbContactTypes });

        if (JOptionPane.showConfirmDialog(null, pnlNewContact, "Add Contact", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String contactName = txtContactName.getText();  //NOI18N
            LocalClassMetadataLight contactType = (LocalClassMetadataLight)cmbContactTypes.getSelectedItem();
            
            if (contactType == null) {
                JOptionPane.showMessageDialog(null, "Please select a contact type", I18N.gm("error"),  JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            if(!CommunicationsStub.getInstance().createContact(contactType.getClassName(), contactName, selectedNode.getObject().getClassName(), selectedNode.getObject().getId()))
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else 
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The contact was created successfully");
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }
}
