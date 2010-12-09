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

package org.inventory.core.usermanager.nodes.properties;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalUserObject;
import org.inventory.core.services.interfaces.NotificationUtil;
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

    public UserProperty(String _name,String _displayName,String _toolTextTip,Object _value, LocalUserObject _user){
        super(_name,_value.getClass(),_displayName,_toolTextTip);
        this.object = _user;
        this.value = _value;
        this.com = CommunicationsStub.getInstance();
        if (_name.equals(UserNode.PROP_PASSWORD) || _name.equals(UserNode.PROP_GROUPS))
            this.setValue("canEditAsText", Boolean.FALSE);
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        LocalObject update = Lookup.getDefault().lookup(LocalObject.class);

        update.setLocalObject("User", //NOI18N
                new String[]{this.getName()}, new Object[]{t});
        update.setOid(this.object.getOid());
        
        if(!com.saveObject(update)){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("User Update", NotificationUtil.ERROR, com.getError());
        }
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
