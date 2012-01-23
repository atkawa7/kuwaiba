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

import java.util.Date;
import org.inventory.core.services.api.session.LocalUserGroupObject;
import org.kuwaiba.wsclient.UserGroupInfo;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation for the local representation of an application users group
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalUserGroupObject.class)
public class LocalUserGroupObjectImpl extends LocalUserGroupObjectLightImpl implements LocalUserGroupObject{
    /**
     * Group's creation date (actually a timestamp)
     */
    private Date creationDate;
    /**
     * Group's description
     */
    private String description;
    
    public LocalUserGroupObjectImpl(UserGroupInfo group) {
        super(group.getOid(),group.getName());
        this.description = group.getDescription();
        if (group.getCreationDate() == null)
            this.creationDate = null;
        else
            this.creationDate = new Date(group.getCreationDate());
    }

    public LocalUserGroupObjectImpl() {
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
