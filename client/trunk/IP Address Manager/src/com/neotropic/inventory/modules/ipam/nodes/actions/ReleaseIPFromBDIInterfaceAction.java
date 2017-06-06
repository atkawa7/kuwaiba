/*
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.inventory.modules.ipam.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * Release a relation between service instance and an interface
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ReleaseIPFromBDIInterfaceAction extends GenericObjectNodeAction implements Presenter.Popup {
       
    @Override
    public void actionPerformed(ActionEvent e) {
        if (CommunicationsStub.getInstance().releasePortFromInterface((String)((JMenuItem)e.getSource()).getClientProperty("portClassName"),  //NOI18N
                (long)((JMenuItem)e.getSource()).getClientProperty("portId"), (long)((JMenuItem)e.getSource()).getClientProperty("bdiId")))
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUCCESS"));
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }

    @Override
    public JMenuItem getPopupPresenter() {
        
        JMenu mnuAction = new JMenu(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_RELEASE_INTERFACE"));
        Iterator<? extends ObjectNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        ObjectNode selectedNode = (ObjectNode)selectedNodes.next();
        
        List<LocalObjectLight> bdis = CommunicationsStub.getInstance().getSpecialAttribute(selectedNode.getObject().getClassName(), 
                selectedNode.getObject().getOid(), Constants.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE);

        if (bdis != null) {
        
            if (bdis.isEmpty())
                mnuAction.setEnabled(false);
            else {
                for (LocalObjectLight bdi : bdis){
                    JMenuItem mnuBdis = new JMenuItem(bdi.toString());
                    mnuBdis.putClientProperty("bdiId", bdi.getOid()); //NOI18N
                    mnuBdis.putClientProperty("portClassName", selectedNode.getObject().getClassName());  //NOI18N
                    mnuBdis.putClientProperty("portId", selectedNode.getObject().getOid()); //NOI18N
                    mnuBdis.addActionListener(this);
                    mnuAction.add(mnuBdis);
                }
            }
            return mnuAction;
        } else {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        } 
    }

    @Override
    public String getValidator() {
        return Constants.VALIDATOR_LOGICAL_SET;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_IP_ADDRESS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
    
}
