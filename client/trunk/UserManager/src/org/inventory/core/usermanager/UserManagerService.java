/*
 *   Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.GroupChildren;
import org.inventory.core.usermanager.nodes.UserChildren;
import org.openide.explorer.view.NodeTableModel;
import org.openide.explorer.view.TableView;
import org.openide.nodes.AbstractNode;

/**
 * Provides the logic to the associated TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UserManagerService {
    private UserManagerTopComponent umtc;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    /**
     * Table model used in the users table view
     */
    private NodeTableModel usersTableModel;
    /**
     * Table model used in the groups table view
     */
    private NodeTableModel groupsTableModel;

    /**
     * Reference to the user's list root node
     */
    private AbstractNode usersRoot;
    /**
     * Reference to the user's list root node
     */
    private AbstractNode groupsRoot;

    public UserManagerService(UserManagerTopComponent umtc){
        this.umtc = umtc;
    }

    /**
     * Populates the initial users list
     */
    public void populateUsersList() {
        usersTableModel = new NodeTableModel();
        List<LocalUserObject> users = com.getUsers();
        if (users == null){
            umtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            users = new ArrayList<>();
        }
        usersRoot = new AbstractNode(new UserChildren(users));
        usersTableModel.setNodes(usersRoot.getChildren().getNodes());
        if (usersRoot.getChildren().getNodesCount() != 0)
            usersTableModel.setProperties(usersRoot.getChildren().getNodes()[0].getPropertySets()[0].
                getProperties());
        umtc.getExplorerManager().setRootContext(usersRoot);
        umtc.setTblUsers(new TableView(usersTableModel));
    }

    /**
     * Populates the initial group list
     */
    public void populateGroupsList() {
        LocalUserGroupObject[] groups = com.getGroups();
        if (groups == null){
            umtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return;
        }
        //Do nothing if there are no groups
        if (groups.length == 0)
            return;
        groupsTableModel = new NodeTableModel();
        if (groups == null){
            umtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            groups = new LocalUserGroupObject[0];
        }
        groupsRoot = new AbstractNode(new GroupChildren(groups));
        groupsTableModel.setNodes(groupsRoot.getChildren().getNodes());
        if (groupsRoot.getChildren().getNodesCount() != 0)
            groupsTableModel.setProperties(groupsRoot.getChildren().getNodes()[0].getPropertySets()[0].
                getProperties());
        umtc.getExplorerManager().setRootContext(groupsRoot);
        umtc.setTblGroups(new TableView(groupsTableModel));
    }

    /**
     * Refreshes the list (but without fetching the user list from the server).
     * For some reason the call to method "add" in AddUser doesn't add the node
     */
    public void refreshUserList(){
        usersTableModel.setNodes(umtc.getExplorerManager().getRootContext().getChildren().getNodes());
        umtc.revalidate();

        //Focus the users tab
        umtc.getPnlTabbedMain().setSelectedIndex(0);
    }

    public void refreshGroupsList(){
        if (groupsTableModel == null)
            populateGroupsList();

        if (umtc.getPnlGroups().getComponentCount() == 0){
            umtc.getPnlGroups().add(umtc.getTblGroups());
            umtc.getPnlGroups().revalidate();
        }
        groupsTableModel.setNodes(umtc.getExplorerManager().getRootContext().getChildren().getNodes());
        umtc.revalidate();

        //Focus the groups tab
        umtc.getPnlTabbedMain().setSelectedIndex(1);
    }

    /**
     * Set the root context to the user's root node
     */
    public void setRootToUsers(){
        this.umtc.getExplorerManager().setRootContext(usersRoot);
    }

    /**
     * Set the root context to the group's root node
     */
    public void setRootToGroups(){
        if (groupsRoot != null) //null if there are no groups at all
            this.umtc.getExplorerManager().setRootContext(groupsRoot);
    }

    public AbstractNode getGroupsRoot() {
        return groupsRoot;
    }

    public AbstractNode getUsersRoot() {
        return usersRoot;
    }

    public UserManagerTopComponent getTC(){
        return this.umtc;
    }
}
