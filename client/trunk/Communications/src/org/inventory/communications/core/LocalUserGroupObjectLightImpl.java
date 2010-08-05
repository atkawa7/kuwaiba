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

package org.inventory.communications.core;

import org.inventory.core.services.interfaces.LocalUserGroupObjectLight;
import org.inventory.webservice.UserGroupInfoLight;

/**
 * Implementation for the local representation of the very basic information about an application users group
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalUserGroupObjectLightImpl implements LocalUserGroupObjectLight{
    protected  Long oid;
    protected  String name;

    public LocalUserGroupObjectLightImpl(){
    }
    
    public LocalUserGroupObjectLightImpl(UserGroupInfoLight user){
        this.oid = user.getOid();
        this.name = user.getName();
    }

    public LocalUserGroupObjectLightImpl(Long _oid, String _name){
        this.oid = _oid;
        this.name = _name;
    }

    public Long getOid() {
        return oid;
    }

    public String getName() {
        return name;
    }
}
