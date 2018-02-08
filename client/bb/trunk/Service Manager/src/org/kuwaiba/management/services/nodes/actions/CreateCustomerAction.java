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
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.nodes.CustomerPoolNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * This action allows to create a customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CreateCustomerAction extends GenericInventoryAction implements Presenter.Popup {
    
    public CreateCustomerAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_CUSTOMER"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        Iterator<? extends CustomerPoolNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(CustomerPoolNode.class).allInstances().iterator();
            
        if (!selectedNodes.hasNext())
            return;
        
        Node selectedNode = selectedNodes.next();
        
        LocalObjectLight newCustomer = CommunicationsStub.getInstance().
                createPoolItem(((CustomerPoolNode)selectedNode).getPool().getOid(), 
                ((JMenuItem)e.getSource()).getName());
        if (newCustomer == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            ((CustomerPoolNode.CustomerPoolChildren)selectedNode.getChildren()).addNotify();
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATED"));
        }
    }
  
    @Override
    public JMenuItem getPopupPresenter() {
        List<LocalClassMetadataLight> customerClasses = CommunicationsStub.getInstance().
                getLightSubclasses(Constants.CLASS_GENERICCUSTOMER, false, false);
        JMenuItem menu = new JMenu(java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_CUSTOMER"));
        for (LocalClassMetadataLight customerClass : customerClasses){
            JMenuItem customerEntry = new JMenuItem(customerClass.getClassName());
            customerEntry.setName(customerClass.getClassName());
            customerEntry.addActionListener(this);
            menu.add(customerEntry);
        }
        return menu;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
