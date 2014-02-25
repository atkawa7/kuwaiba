/*
 *   Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.usermanager.nodes.properties;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserGroupObjectLight;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.UserNode;
import org.inventory.core.usermanager.nodes.customeditor.GroupsEditorSupport;
import org.inventory.core.usermanager.nodes.customeditor.PasswordEditorSupport;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.util.Lookup;

/**
 * Represents a single user's property
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UserProperty extends ReadWrite{

    /**
     * Current value
     */
    private Object value;

    private LocalUserObject user;
    /**
     * Reference to the communication component
     */
    private CommunicationsStub com;

    /**
     * Custom editor for password property
     */
    private PasswordEditorSupport pes = null;

    public UserProperty(String name,String displayName,String toolTextTip,
            Object value, LocalUserObject user){
        super(name, value.getClass(),displayName, toolTextTip);
        this.user = user;
        this.value = value;
        this.com = CommunicationsStub.getInstance();
        if (name.equals(UserNode.PROP_PASSWORD) || name.equals(UserNode.PROP_GROUPS))
            this.setValue("canEditAsText", Boolean.FALSE);
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        LocalUserGroupObjectLight[] groups = user.getGroups();
        long[] oids = new long[groups.length];
        for (int i = 0; i < groups.length; i++)
            oids[i] = groups[i].getOid();

        boolean success = false;
        if (this.getName().equals(UserNode.PROP_USERNAME))
            success = com.setUserProperties(user.getOid(), (String)t, null, null, null, oids);
        else if(this.getName().equals(UserNode.PROP_PASSWORD))
            success = com.setUserProperties(user.getOid(), null, (String)t, null, null, oids);
        else if(this.getName().equals(UserNode.PROP_FIRSTNAME))
            success = com.setUserProperties(user.getOid(), null, null, (String)t, null, oids);
        else if(this.getName().equals(UserNode.PROP_LASTNAME))
            success = com.setUserProperties(user.getOid(), null, null, null, (String)t, oids);
        
        if(!success){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("User Update", NotificationUtil.ERROR, com.getError());
        }else
            this.value = t;
    }

    /**
     * Can this property to be written?
     * @return A boolean meaning this property can be written or not
     */
    @Override
    public boolean canWrite(){
        return true;
    }

    @Override
    public PropertyEditorSupport getPropertyEditor(){
        if (this.getName().equals(UserNode.PROP_PASSWORD)){ //NOI18N
            if(this.pes == null)
                pes = new PasswordEditorSupport(this);
            return pes;
        }
        if (this.getName().equals(UserNode.PROP_GROUPS)) //NOI18N
                return new GroupsEditorSupport(com.getGroups(),this.user);

        return null;
    }

    /**
     * I don't like this workaround, but as described in the setValue method, this is while 
     * @param passwd a String with the password to be set for this user
     */
    public void setPassword(String passwd) {
        if(!com.setUserProperties(user.getOid(), null, passwd, null, null, null)){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("User Update", NotificationUtil.ERROR, com.getError());
        }
    }
}
