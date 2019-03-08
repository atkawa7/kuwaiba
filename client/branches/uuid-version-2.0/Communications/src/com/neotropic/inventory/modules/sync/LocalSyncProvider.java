/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.inventory.modules.sync;

/**
 * Local representation of a Synchronization Provider
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalSyncProvider {
    /**
     * The id of the provider. Typically, this the FQN of the class at server side that implements the provider
     */
    private String id;
    /**
     * How the provider should be display in combo boxes and lists
     */
    private String displayName;
    /**
     * True if the provider is automated, false if supervised
     */
    private boolean automated;

    public LocalSyncProvider(String id, String displayName, boolean automated) {
        this.id = id;
        this.displayName = displayName;
        this.automated = automated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isAutomated() {
        return automated;
    }

    public void setAutomated(boolean automated) {
        this.automated = automated;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocalSyncProvider ? ((LocalSyncProvider)obj).getDisplayName().equals(displayName) : false;
    }
}
