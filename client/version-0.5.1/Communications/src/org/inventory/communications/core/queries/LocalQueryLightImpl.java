/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.communications.core.queries;

import org.inventory.core.services.api.queries.LocalQueryLight;
//import org.kuwaiba.wsclient.RemoteQueryLight;
import org.kuwaiba.wsclient.RemoteQueryLight;
import org.openide.util.lookup.ServiceProvider;

/**
 * This is the simple version of LocalQuery {@link #LocalQueryImpl}
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalQueryLight.class)
public class LocalQueryLightImpl implements LocalQueryLight{
    private Long id;
    private String name;
    private boolean isPublic;
    private String description;

    public LocalQueryLightImpl() {    }

    public LocalQueryLightImpl(RemoteQueryLight remoteQuery) {
        this.id = remoteQuery.getOid();
        this.name = remoteQuery.getName();
        this.description = remoteQuery.getDescription();
        this.isPublic = remoteQuery.isIsPublic();
    }

    public LocalQueryLightImpl(Long id, String name, String description, boolean isPublic){
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString(){
        return name;
    }
}
