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

package org.inventory.core.usermanager.nodes;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.actions.UserManagerActionFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * The root node of the User Manager module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UserManagerRootNode extends AbstractNode {
    
    public static final String ICON_PATH="org/inventory/core/usermanager/res/root.png";
    
    public UserManagerRootNode() {
        super(new GroupChildren());
        setDisplayName("User Manager");
        setIconBaseWithExtension(ICON_PATH);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { UserManagerActionFactory.getCreateGroupAction() };
    }
    
    public static class GroupChildren extends Children.Keys<LocalUserGroupObject> {

        @Override
        public void addNotify() {
            List<LocalUserGroupObject> groups = CommunicationsStub.getInstance().
                                                    getGroups();

            if (groups == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            else {
                Collections.sort(groups);
                setKeys(groups);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalUserGroupObject key) {
            return new Node[] { new GroupNode(key) };
        }
    }
}
