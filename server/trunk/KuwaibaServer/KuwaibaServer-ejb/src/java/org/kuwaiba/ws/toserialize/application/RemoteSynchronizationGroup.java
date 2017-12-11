/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.ws.toserialize.application;

import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper of SynchronizationGroup
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

@XmlAccessorType(XmlAccessType.FIELD)
public final class RemoteSynchronizationGroup implements Serializable {
     /**
     * Group id
     */
    private long id;
    /**
     * Group name
     */
    private String name;
    /**
     * Group provider
     */
    private RemoteSynchronizationProvider provider;
    
    public RemoteSynchronizationGroup() { }
    
    public RemoteSynchronizationGroup(SynchronizationGroup syncGroup) {
        this.id = syncGroup.getId();
        this.name = syncGroup.getName();
        this.provider = new RemoteSynchronizationProvider(syncGroup.getProvider().getId(), syncGroup.getProvider().getName());
    }
    
    public RemoteSynchronizationGroup(long id, String name, RemoteSynchronizationProvider provider) {
        this.id = id;
        this.name = name;
        this.provider = provider;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RemoteSynchronizationProvider getProvider() {
        return provider;
    }

    public void setProvider(RemoteSynchronizationProvider provider) {
        this.provider = provider;
    }
    
    public static List<RemoteSynchronizationGroup> toArray(List<SynchronizationGroup> syncGroups) {
        List<RemoteSynchronizationGroup> res = new ArrayList<>();
        for (SynchronizationGroup syncGroup : syncGroups)
            res.add(new RemoteSynchronizationGroup(syncGroup.getId(), syncGroup.getName(), 
                    new RemoteSynchronizationProvider(syncGroup.getProvider().getId(), syncGroup.getProvider().getName())));
        
        return res;
    }
    
}
