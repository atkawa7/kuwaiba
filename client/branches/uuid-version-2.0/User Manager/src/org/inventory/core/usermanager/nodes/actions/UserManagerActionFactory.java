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

package org.inventory.core.usermanager.nodes.actions;

import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * Action factory for the User Manager module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class UserManagerActionFactory {
    private static CreateUserAction createUserAction;
    private static CreateGroupAction createGroupAction;
    private static DeleteUserAction deleteUserAction;
    private static DeleteGroupAction deleteGroupAction;
    private static RelateToGroupAction relateToGroupAction;
    private static RemoveFromGroupAction removeFromGroupAction;
    private static RefreshUserListAction refreshUserListAction;
    
    public static GenericInventoryAction getCreateUserAction() {
        if (createUserAction == null)
            createUserAction = new CreateUserAction();
        return createUserAction;
    }
    
    public static GenericInventoryAction getCreateGroupAction() {
        if (createGroupAction == null)
            createGroupAction = new CreateGroupAction();
        return createGroupAction;
    }
    
    public static GenericInventoryAction getDeleteUserAction() {
        if (deleteUserAction == null)
            deleteUserAction = new DeleteUserAction();
        return deleteUserAction;
    }
    
    public static GenericInventoryAction getDeleteGroupAction() {
        if (deleteGroupAction == null)
            deleteGroupAction = new DeleteGroupAction();
        return deleteGroupAction;
    }
    
    public static GenericInventoryAction getRelateToGroupAction() {
        if (relateToGroupAction == null)
            relateToGroupAction = new RelateToGroupAction();
        return relateToGroupAction;
    }
    
    public static GenericInventoryAction getRemoveFromGroupAction() {
        if (removeFromGroupAction == null)
            removeFromGroupAction = new RemoveFromGroupAction();
        return removeFromGroupAction;
    }
    
    public static GenericInventoryAction getRefreshUserListAction() {
        if (refreshUserListAction == null)
            refreshUserListAction = new RefreshUserListAction();
        return refreshUserListAction;
    }
}