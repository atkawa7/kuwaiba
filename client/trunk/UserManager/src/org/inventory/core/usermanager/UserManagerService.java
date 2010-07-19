/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.core.usermanager;

import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalUserObject;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.core.usermanager.nodes.UserChildren;
import org.openide.explorer.view.NodeTableModel;
import org.openide.nodes.AbstractNode;

/**
 * Provides the logic to the associated TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class UserManagerService {
    private UserManagerTopComponent umtc;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public UserManagerService(UserManagerTopComponent _umtc){
        this.umtc = _umtc;
    }

    /**
     * Populates the initial users list
     * @return A NodeTableModel containing the users available at the moment
     */
    public NodeTableModel populateUsersList() {
        NodeTableModel tableModel = new NodeTableModel();
        LocalUserObject[] users = com.getUsers();
        if (users == null){
            umtc.getNotifier().showSimplePopup(
                    java.util.ResourceBundle.getBundle("org/inventory/core/usermanager/Bundle").
                    getString("LBL_USERMANAGEMENT"), NotificationUtil.ERROR, com.getError());
            users = new LocalUserObject[0];
        }
        AbstractNode root = new AbstractNode(new UserChildren(users));
        tableModel.setNodes(root.getChildren().getNodes());
        tableModel.setProperties(root.getChildren().getNodes()[0].getPropertySets()[0].
                getProperties());
        umtc.getExplorerManager().setRootContext(root);
        return tableModel;
    }
}
