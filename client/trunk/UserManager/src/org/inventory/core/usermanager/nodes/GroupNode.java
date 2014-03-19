/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.core.usermanager.UserManagerTopComponent;
import org.inventory.core.usermanager.actions.DeleteAction;
import org.inventory.core.usermanager.nodes.properties.UserGroupProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 * Node representing a group of users
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GroupNode extends AbstractNode{

    public static final String PROP_GROUPSNAME="name";
    public static final String PROP_DESCRIPTION="description";

    public LocalUserGroupObject object;

    public GroupNode(LocalUserGroupObject group) {
        super (Children.LEAF, Lookups.singleton(group));
        object = group;
    }

    @Override
    public Sheet createSheet(){
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);

        if (set == null) {
            set = Sheet.createPropertiesSet();
            sheet.put(set);
        }

        set.put(new UserGroupProperty(PROP_GROUPSNAME, "Name", "Group's name", object.getName(), object));
        set.put(new UserGroupProperty(PROP_DESCRIPTION, "Description", "Group's description",
                object.getDescription()==null?"":object.getDescription(), object));
        return sheet;
    }

    @Override
    public Action[] getActions(boolean context){
        UserManagerTopComponent tc =(UserManagerTopComponent)WindowManager.getDefault().findTopComponent("UserManagerTopComponent");
        return new Action[]{new DeleteAction(this,tc.getUserManagerServiceInstance())};
    }

    public LocalUserGroupObject getObject(){
        return this.object;
    }
}