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

package org.inventory.core.usermanager.nodes.customeditor;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import org.inventory.core.services.interfaces.LocalUserGroupObjectLight;

/**
 * This is the editor for changing the groups for a given users
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class GroupsEditorSupport extends PropertyEditorSupport{

    private LocalUserGroupObjectLight[] allGroups;
    private LocalUserGroupObjectLight[] myGroups;

    public GroupsEditorSupport(LocalUserGroupObjectLight[] _allGroups, LocalUserGroupObjectLight[] _myGroups){
        this.allGroups = _allGroups;
        this.myGroups = _myGroups;
    }

    @Override
    public Component getCustomEditor(){
        return new SetGroupsPanel(allGroups,myGroups);
    }

    @Override
    public boolean supportsCustomEditor(){
        return true;
    }

}
