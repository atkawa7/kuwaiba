/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.nodes.CustomerNode;
import org.kuwaiba.management.services.nodes.ServiceManagerRootNode;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * This action allows to create a customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CreateCustomerAction extends AbstractAction implements Presenter.Popup {
    private ServiceManagerRootNode rootNode;
    
    
    public CreateCustomerAction(ServiceManagerRootNode rootNode) {
        this.rootNode = rootNode;
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_CUSTOMER"));
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        String objectClass = ((JMenuItem)e.getSource()).getName();
        LocalObjectLight newService = CommunicationsStub.getInstance().createCustomer(objectClass, null, null);
        if (newService != null)
            rootNode.getChildren().add(new CustomerNode[] {new CustomerNode(newService)});
        else{
            Lookup.getDefault().lookup(NotificationUtil.class).showSimplePopup("Error", 
                    NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        LocalClassMetadataLight[] customerClasses = CommunicationsStub.getInstance().
                getLightSubclasses("GenericCustomer", false, false);
        JMenuItem menu = new JMenu(java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_CUSTOMER"));
        for (LocalClassMetadataLight customerClass : customerClasses){
            JMenuItem customerEntry = new JMenuItem(customerClass.getClassName());
            customerEntry.setName(customerClass.getClassName());
            customerEntry.addActionListener(this);
            menu.add(customerEntry);
        }
        return menu;
    }
    
}
