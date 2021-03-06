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
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.session.LocalUserGroupObjectLight;
import org.inventory.core.services.api.session.LocalUserObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.Utils;
import org.inventory.core.usermanager.nodes.UserNode;
import org.inventory.core.usermanager.nodes.customeditor.GroupsEditorSupport;
import org.inventory.core.usermanager.nodes.customeditor.PasswordEditorSupport;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.util.Lookup;

/**
 * Represents a single user's property
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class UserProperty extends ReadWrite{

    /**
     * Current value
     */
    private Object value;

    private LocalUserObject object;
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
        this.object = user;
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

        LocalUserGroupObjectLight[] groups = object.getGroups();
        List<Long> oids = new ArrayList<Long>();
        for (LocalUserGroupObjectLight group : groups) {
            oids.add(group.getOid());
        }

        boolean success = false;
        if (this.getName().equals(UserNode.PROP_USERNAME))
            success = com.setUserProperties(object.getOid(), (String)t, null, null, null, oids);
        else if(this.getName().equals(UserNode.PROP_PASSWORD))
            success = com.setUserProperties(object.getOid(), null, (String)t, null, null, oids);
        else if(this.getName().equals(UserNode.PROP_FIRSTNAME))
            success = com.setUserProperties(object.getOid(), null, null, (String)t, null, oids);
        else if(this.getName().equals(UserNode.PROP_LASTNAME))
            success = com.setUserProperties(object.getOid(), null, null, null, (String)t, oids);
        
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
                return new GroupsEditorSupport(com.getGroups(),this.object);

        return null;
    }

    /**
     * I don't like this workaround, but as described in the setValue method, this is while 
     * @param passwd a String with the password to be set for this user
     */
    public void setPassword(String passwd) {
        LocalObject update = Lookup.getDefault().lookup(LocalObject.class);
        //The password is hashed before setting it
        update.setLocalObject("User", //NOI18N
                new String[]{UserNode.PROP_PASSWORD}, new Object[]{Utils.getMD5Hash(passwd)}); //NOI18N
        update.setOid(this.object.getOid());
        if(!com.saveObject(update)){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("User Update", NotificationUtil.ERROR, com.getError());
        }
    }
}
