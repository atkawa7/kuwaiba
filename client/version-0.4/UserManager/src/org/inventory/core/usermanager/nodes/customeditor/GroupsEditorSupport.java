/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.core.services.api.session.LocalUserGroupObjectLight;
import org.inventory.core.services.api.session.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Lookup;

/**
 * This is the editor for changing the groups for a given users
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
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
    /**
     * Reference to the NotificationUtil instance
     */
    private NotificationUtil nu;
    public GroupsEditorSupport(LocalUserGroupObjectLight[] _allGroups, LocalUserObject _user){
        this.allGroups = _allGroups;
        this.com = CommunicationsStub.getInstance();
        this.nu = Lookup.getDefault().lookup(NotificationUtil.class);
        this.user = _user;
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
        if (user.getGroups() == null)
            return "";
        if (user.getGroups().length == 0)
            return "";
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
            Boolean success = com.setUserProperties(this.user.getOid(), null, null, null, null, myPanel.toBeAdded());
            if (!success)
                nu.showSimplePopup("User Update", NotificationUtil.ERROR, com.getError());
            else
                user.setGroups(myPanel.getSelectedGroups());
            
        }
    }
}
