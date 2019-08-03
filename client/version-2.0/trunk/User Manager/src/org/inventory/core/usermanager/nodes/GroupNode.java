/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.actions.UserManagerActionFactory;
import org.inventory.core.usermanager.nodes.properties.PropertyGroupCreationDate;
import org.inventory.core.usermanager.nodes.properties.PropertyGroupDescription;
import org.inventory.core.usermanager.nodes.properties.PropertyGroupName;
import org.inventory.core.usermanager.nodes.properties.PropertyGroupPrivileges;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Represents a group
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GroupNode extends AbstractNode implements PropertyChangeListener {
    public static final String ICON_PATH="org/inventory/core/usermanager/res/group.png";
    
    public GroupNode(LocalUserGroupObject group) {
        super(new UserChildren(group), Lookups.singleton(group));
        setIconBaseWithExtension(ICON_PATH);
        group.addNonVetoablePropertyChangeListener(this);
        group.addVetoablePropertyChangeListener(GroupNodePropertyChangeListener.getInstance());
    }
    
    @Override
    public String getName() {
        return getLookup().lookup(LocalUserGroupObject.class).getName();
    }
    
    @Override
    public void setName(String name) {
        String oldDisplayName = getDisplayName();
        getLookup().lookup(LocalUserGroupObject.class).setName(name);
        fireDisplayNameChange(oldDisplayName, getDisplayName());
    }
    
    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalUserGroupObject.class).toString();
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public boolean canRename() {
        return true;
    }
    
    @Override
    public void destroy() throws IOException {
        getLookup().lookup(LocalUserGroupObject.class).removeAllPropertyChangeListeners();
        super.destroy();
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set defaultSet = Sheet.createPropertiesSet();
        LocalUserGroupObject group = getLookup().lookup(LocalUserGroupObject.class);
        
        PropertyGroupName prpName = new PropertyGroupName(group);
        PropertyGroupDescription prpDescription = new PropertyGroupDescription(group);
        PropertyGroupCreationDate prpCreationDateProperty = new PropertyGroupCreationDate(group);
        PropertyGroupPrivileges prpPrivileges = new PropertyGroupPrivileges(group);

        defaultSet.put(prpName);
        defaultSet.put(prpDescription);
        defaultSet.put(prpCreationDateProperty);
        defaultSet.put(prpPrivileges);

        sheet.put(defaultSet);
        return sheet;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { 
            UserManagerActionFactory.getCreateUserAction(),
            UserManagerActionFactory.getRefreshUserListAction(),
            null,
            UserManagerActionFactory.getDeleteGroupAction() };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { //NOI18N
            fireDisplayNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
            firePropertyChange("name", evt.getOldValue(), evt.getNewValue());
        }
    }
    
    public static class UserChildren extends Children.Keys<LocalUserObject> {
        private LocalUserGroupObject group;

        public UserChildren(LocalUserGroupObject group) {
            this.group = group;
        }
        
        @Override
        public void addNotify() {
            List<LocalUserObject> users = CommunicationsStub.getInstance().getUsersInGroup(group.getId());

            if (users == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            else {
                Collections.sort(users);
                setKeys(users);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalUserObject key) {
            return new Node[] { new UserNode(key) };
        }
    }
}
