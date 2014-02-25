/*
 *  Copyright 2010 - 2014 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.communications.core;

import org.kuwaiba.wsclient.GroupInfoLight;

/**
 * Implementation for the local representation of the very basic information about an application users group
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalUserGroupObjectLight {
    protected  long oid;
    protected  String name;

    public LocalUserGroupObjectLight(GroupInfoLight user){
        this.oid = user.getId();
        this.name = user.getName();
    }

    public LocalUserGroupObjectLight(long oid, String name){
        this.oid = oid;
        this.name = name;
    }

    public long getOid() {
        return oid;
    }

    public String getName() {
        return name;
    }
}
