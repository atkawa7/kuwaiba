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
import org.inventory.core.services.i18n.I18N;
import org.kuwaiba.management.services.nodes.ServicePoolNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * This action allows to create a service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
class CreateServiceAction extends GenericInventoryAction implements Presenter.Popup {

    public CreateServiceAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_SERVICE"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
       
        Iterator<? extends ServicePoolNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ServicePoolNode.class).allInstances().iterator();
            
        if (!selectedNodes.hasNext())
            return;
        
        ServicePoolNode selectedNode = selectedNodes.next();
        
        LocalObjectLight newService = CommunicationsStub.getInstance().
                createPoolItem(selectedNode.getPool().getId(), ((JMenuItem)e.getSource()).getName());     
                
        if (newService == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            ((ServicePoolNode.ServicePoolChildren)selectedNode.getChildren()).addNotify();
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATED"));
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        List<LocalClassMetadataLight> serviceClasses = CommunicationsStub.getInstance().
                getLightSubclasses(Constants.CLASS_GENERICSERVICE, false, false);
        JMenuItem menu = new JMenu(java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CREATE_SERVICE"));
        
        if (serviceClasses == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            menu.setEnabled(false);
        } else {
            for (LocalClassMetadataLight serviceClass : serviceClasses){
                JMenuItem customerEntry = new JMenuItem(serviceClass.getClassName());
                customerEntry.setName(serviceClass.getClassName());
                customerEntry.addActionListener(this);
                menu.add(customerEntry);
            }
        }
        return menu;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ);
    }
}
