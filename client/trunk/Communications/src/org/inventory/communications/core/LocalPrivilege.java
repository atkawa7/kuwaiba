/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import java.util.Objects;

/**
 * A local, simplified representation of a user/group privilege
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalPrivilege {
    /**
     * For now, these privileges will be hardcoded, however in the near future, every module will provide its own set of tokens
     */
    public static String[] DEFAULT_PRIVILEGES = new String[] {
                                                "navigation-tree", "Navigation Tree",
                                                "relationship-explorer", "Relationship Explorer",
                                                "special-children-explorer", "Special Children Explorer",
                                                "physical-view", "Physical View",
                                                "topology-designer", "Topology Designer",
                                                "data-model-manager", "Data Model Manager",
                                                "list-type-manager", "List Type Manager",
                                                "containment-manager", "Containment Manager",
                                                "sdh-module", "SDH Module",
                                                "mpls-module", "MPLS Module",
                                                "contract-manager", "Contract Manager",
                                                "ip-address-manager", "IP Address Manager",
                                                "service-manager", "Service Manager",
                                                "user-manager", "User Manager",
                                                "software-asset-manager", "Software Asset Manager",
                                                "reports", "Reports",
                                                "templates", "Templates",
                                                "pools", "Pools",
                                                "bulk-import", "Bulk Import",
                                                "audit-trail", "Audit Trail",
                                                "query-manager", "Query Manager",
                                                "task-manager", "Task Manager"
                                               };
    /**
     * Not an actual privilege. Use this to indicate that an existing privilege should be removed
     */
    public static final int ACCESS_LEVEL_UNSET = 0;
    /**
     * Read-only privilege
     */
    public static final int ACCESS_LEVEL_READ = 1;
    /**
     * Read-write privilege
     */
    public static final int ACCESS_LEVEL_READ_WRITE = 2;
    /**
     * Unique identifier of a particular feature
     */
   private String featureToken;
   /**
    * Access level. See ACCESS_LEVEL* for possible values
    */
   private int accessLevel;

    public LocalPrivilege(String featureToken, int accessLevel) {
        this.featureToken = featureToken;
        this.accessLevel = accessLevel;
    }

    public String getFeatureToken() {
        return featureToken;
    }

    public void setFeatureToken(String featureToken) {
        this.featureToken = featureToken;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalPrivilege)
            return featureToken.equals(((LocalPrivilege)obj).featureToken);
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.featureToken);
        return hash;
    }
   
}
