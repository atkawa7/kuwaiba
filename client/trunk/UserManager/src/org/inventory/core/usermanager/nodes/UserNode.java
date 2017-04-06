/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import javax.swing.Action;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.usermanager.nodes.actions.UserManagerActionFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * A node representing an application user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UserNode extends AbstractNode {

    public static final String ICON_PATH="org/inventory/core/usermanager/res/user.png";
    
    public UserNode(LocalUserObject user) {
        super(Children.LEAF, Lookups.singleton(user));
        setIconBaseWithExtension(ICON_PATH);
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
    protected Sheet createSheet() {
        return super.createSheet(); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { UserManagerActionFactory.getRelateToGroupAction(), 
                              UserManagerActionFactory.getRemoveFromGroupAction(),
                              null, //Separator
                              UserManagerActionFactory.getDeleteUserAction()
                            };
    }
}
