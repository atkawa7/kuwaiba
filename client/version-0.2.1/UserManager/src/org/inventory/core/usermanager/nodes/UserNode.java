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

package org.inventory.core.usermanager.nodes;

import javax.swing.Action;
import org.inventory.core.services.interfaces.LocalUserObject;
import org.inventory.core.usermanager.UserManagerTopComponent;
import org.inventory.core.usermanager.actions.Delete;
import org.inventory.core.usermanager.nodes.properties.UserProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 * This node wraps an LocalUserObject instance
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class UserNode extends AbstractNode{
    private LocalUserObject object;
    public static final String PROP_USERNAME="username";
    public static final String PROP_LASTNAME="lastName";
    public static final String PROP_FIRSTNAME="firstName";
    public static final String PROP_GROUPS="groups";
    public static final String PROP_PASSWORD="password";

    public UserNode(LocalUserObject localUserObject) {
        super(Children.LEAF,Lookups.singleton(localUserObject));
        this.object = localUserObject;
    }

    @Override
    public String getDisplayName(){
        return object.getUserName();
    }

    @Override
    protected Sheet createSheet(){
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }
        ss.put(new UserProperty(PROP_USERNAME, "Username", "User name used in the login process", object.getUserName(),this.object));
        ss.put(new UserProperty(PROP_LASTNAME, "Last Name", "User's last name", object.getLastName()==null?"":object.getLastName(),this.object));
        ss.put(new UserProperty(PROP_FIRSTNAME, "First Name", "User's first name", object.getFirstName()==null?"":object.getFirstName(),this.object));
        ss.put(new UserProperty(PROP_GROUPS, "Groups", "Groups this user belongs to", "",this.object));
        ss.put(new UserProperty(PROP_PASSWORD, "Password", "User's password", "****",this.object));
        return s;      
    }

    public LocalUserObject getObject(){
        return this.object;
    }

    @Override
    public Action[] getActions(boolean context){
        UserManagerTopComponent tc =(UserManagerTopComponent)WindowManager.getDefault().findTopComponent("UserManagerTopComponent");
        return new Action[]{new Delete(this,tc.getUserManagerServiceInstance())};
    }
}
