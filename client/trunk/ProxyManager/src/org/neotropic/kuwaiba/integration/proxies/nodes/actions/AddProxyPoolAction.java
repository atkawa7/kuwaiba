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

package org.neotropic.kuwaiba.integration.proxies.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.neotropic.kuwaiba.integration.proxies.nodes.ProxyPoolNode;
import org.neotropic.kuwaiba.integration.proxies.nodes.ProxyRootNode;
import org.openide.util.Utilities;

/**
 * Creates an inventory proxy pool.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AddProxyPoolAction extends GenericInventoryAction {

    public AddProxyPoolAction() {
        putValue(NAME, "Add Inventory Proxy Pool");
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROXIES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProxyRootNode selectedNode = Utilities.actionsGlobalContext().lookup(ProxyRootNode.class);
        
        if (selectedNode != null) {
            JTextField txtName = new JTextField();
            txtName.setColumns(35);

            JTextField txtDescription = new JTextField();
            txtDescription.setColumns(35);

            JComplexDialogPanel pnlProxy = new JComplexDialogPanel(new String[] { "Pool Name", "Pool Description" } , 
                    new JComponent[] { txtName, txtDescription });

            if (JOptionPane.showConfirmDialog(null, pnlProxy, "New Proxy Pool", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (txtName.getText().trim().isEmpty())
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "The name of the proxy pool can not be empty");
                else {
                    if (CommunicationsStub.getInstance().createProxyPool(txtName.getText(), txtDescription.getText()) != null) 
                        ((ProxyRootNode.ProxiesRootChildren)selectedNode.getChildren()).addNotify();
                    else
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }

            }
        }
    }

}
