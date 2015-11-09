/*
 *   Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.GroupNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Represents a single group's property
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>User Update
 */
public class UserGroupProperty extends PropertySupport.ReadWrite {

    /**
     * Current value
     */
    private Object value;

    private LocalUserGroupObject object;
    /**
     * Reference to the communication component
     */
    private CommunicationsStub com;

    public UserGroupProperty(String name,String displayName,String toolTextTip,Object value, LocalUserGroupObject group){
        super(name,value.getClass(),displayName,toolTextTip);
        this.object = group;
        this.value = value;
        this.com = CommunicationsStub.getInstance();
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
        Boolean success = false;
        if(this.getName().equals(GroupNode.PROP_NAME))
            success = com.setGroupProperties(object.getOid(), (String)t, null);
        else if(this.getName().equals(GroupNode.PROP_DESCRIPTION))
            success = com.setGroupProperties(object.getOid(), null,(String)t);
        
        if(success)
            this.value = t;
        else{
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }

    /**
     * Can this property to be written?
     * @return A boolean meaning this property can be written or not
     */
    @Override
    public boolean canWrite(){
        return true;
    }
}
