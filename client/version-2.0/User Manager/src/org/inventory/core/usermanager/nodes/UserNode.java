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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expregss or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.inventory.core.usermanager.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.usermanager.nodes.actions.UserManagerActionFactory;
import org.inventory.core.usermanager.nodes.properties.PropertyUserEnabled;
import org.inventory.core.usermanager.nodes.properties.PropertyUserFirstName;
import org.inventory.core.usermanager.nodes.properties.PropertyUserLastName;
import org.inventory.core.usermanager.nodes.properties.PropertyUserName;
import org.inventory.core.usermanager.nodes.properties.PropertyUserPassword;
import org.inventory.core.usermanager.nodes.properties.PropertyUserPrivileges;
import org.inventory.core.usermanager.nodes.properties.PropertyUserType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * A node representing an application user
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class UserNode extends AbstractNode implements PropertyChangeListener {

    public static final String ICON_PATH="org/inventory/core/usermanager/res/user.png"; //NOI18N
    
    public UserNode(LocalUserObject user) {
        super(Children.LEAF, Lookups.singleton(user));
        setIconBaseWithExtension(ICON_PATH);
        user.addNonVetoablePropertyChangeListener(this);
        user.addVetoablePropertyChangeListener(UserNodePropertyChangeListener.getInstance());
    }
    
    @Override
    public void setName(String name) {
        String oldDisplayName = getDisplayName();
        getLookup().lookup(LocalUserObject.class).setUserName(name);
        fireDisplayNameChange(oldDisplayName, getDisplayName());
    }
    
    @Override
    public String getName() {
        return getLookup().lookup(LocalUserObject.class).getUserName();
    }
    
    @Override
    public String getDisplayName() {
        return getLookup().lookup(LocalUserObject.class).toString();
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
        getLookup().lookup(LocalUserObject.class).removeAllChangeListeners();
        super.destroy();
    }
    
    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set defaultSet = Sheet.createPropertiesSet();
        LocalUserObject user = getLookup().lookup(LocalUserObject.class);
        
        PropertyUserName prpName = new PropertyUserName(user);
        PropertyUserPassword prpPassword = new PropertyUserPassword(user);
        PropertyUserFirstName prpFirstName = new PropertyUserFirstName(user);
        PropertyUserLastName prpLastName = new PropertyUserLastName(user);
        PropertyUserEnabled prpEnabled = new PropertyUserEnabled(user);
        PropertyUserType prpType = new PropertyUserType(user);
        PropertyUserPrivileges prpPrivileges = new PropertyUserPrivileges(user);
        
        defaultSet.put(prpName);
        defaultSet.put(prpPassword);
        defaultSet.put(prpFirstName);
        defaultSet.put(prpLastName);
        defaultSet.put(prpEnabled);
        defaultSet.put(prpType);
        defaultSet.put(prpPrivileges);

        sheet.put(defaultSet);
        return sheet;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { UserManagerActionFactory.getRelateToGroupAction(), 
                              UserManagerActionFactory.getRemoveFromGroupAction(),
                              null, //Separator
                              UserManagerActionFactory.getDeleteUserAction()
                            };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "username": //NOI18N
                firePropertyChange("username", evt.getOldValue(), evt.getNewValue());  //NOI18N
            case "firstName": //NOI18N
            case "lastName": //NOI18N
                fireDisplayNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
                break;
        }
    }
}
