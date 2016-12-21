/**
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

import com.neotropic.inventory.modules.ipam.nodes.SubnetNode;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolChildren;
import com.neotropic.inventory.modules.ipam.nodes.SubnetPoolNode;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static javax.swing.Action.NAME;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class DeleteSubnetAction extends GenericObjectNodeAction{

    /**
     * Reference to the communications stub singleton
     */
    private CommunicationsStub com;

    public DeleteSubnetAction(){
        putValue(NAME, java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DELETE"));
        com = CommunicationsStub.getInstance();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        Iterator<? extends SubnetNode> selectedNodes = Utilities.actionsGlobalContext().lookupResult(SubnetNode.class).allInstances().iterator();
        String className = "";
        SubnetNode selectedNode = null;
        SubnetPoolNode parentNode = null;
        
        if (!selectedNodes.hasNext())
            return;
        List<Long> ids = new ArrayList<>();
        while (selectedNodes.hasNext()) {
            selectedNode = (SubnetNode)selectedNodes.next();
            parentNode = (SubnetPoolNode)selectedNode.getParentNode();
            className = selectedNode.getObject().getClassName();
            ids.add(selectedNode.getObject().getOid());
        }
        
        if (com.deleteSubnet(className, ids)){
            ((SubnetPoolChildren)parentNode.getChildren()).addNotify();
            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, 
                    java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DELETION_TEXT_OK"));
        }
        else
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
    }

    @Override
    public String getValidator() {
        return Constants.VALIDATOR_SUBNET;
    }
    
}
