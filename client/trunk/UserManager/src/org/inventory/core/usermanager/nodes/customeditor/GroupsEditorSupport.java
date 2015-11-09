/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserGroupObjectLight;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * This is the editor for changing the groups for a given users
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GroupsEditorSupport extends PropertyEditorSupport
    implements ExPropertyEditor, VetoableChangeListener{

    /**
     * List of groups
     */
    private LocalUserGroupObjectLight[] allGroups;
    /**
     * PropertyEnv instance associated to this editor
     */
    private PropertyEnv env;
    /**
     * Reference to the panel used to display the list
     */
    private SetGroupsPanel myPanel = null;
    /**
     * Reference to de CommunicationsStub singleton instance
     */
    private CommunicationsStub com;
    /**
     * Reference to the user that is being edited
     */
    private LocalUserObject user;
    
    public GroupsEditorSupport(LocalUserGroupObjectLight[] allGroups, LocalUserObject user){
        this.allGroups = allGroups;
        this.com = CommunicationsStub.getInstance();
        this.user = user;
    }

    @Override
    public Component getCustomEditor(){
        if(this.myPanel==null)
            this.myPanel = new SetGroupsPanel(allGroups,user.getGroups(), env);
        return myPanel;

    }

    @Override
    public boolean supportsCustomEditor(){
        return true;
    }

    @Override
    public String getAsText(){
        if (user.getGroups() == null || user.getGroups().length == 0)
            return "";
        if (user.getGroups().length == 1)
            return user.getGroups()[0].getName();
        return "[Many]";
    }

    @Override
    public void setValue(Object o){
        //Do nothing, because we set the password and make the validations in the vetoable event
    }


    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        this.env.addVetoableChangeListener(this);
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if(evt.getNewValue().equals(PropertyEnv.STATE_VALID)){
            boolean success = com.setUserProperties(this.user.getOid(), null, null, null, null, myPanel.toBeAdded());
            if (!success)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else
                user.setGroups(myPanel.getSelectedGroups());
            
        }
    }
}
