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

package org.inventory.communications.core;

import java.util.Objects;

/**
 * A local, simplified representation of a user/group privilege
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalPrivilege {
    public static final String PRIVILEGE_NAVIGATION_TREE = "navigation-tree";
    public static final String PRIVILEGE_OBJECT_PROPERTIES = "object-properties";
    public static final String PRIVILEGE_SPECIAL_EXPLORERS = "special-explorers";
    public static final String PRIVILEGE_ATTACHMENTS = "attachments";
    public static final String PRIVILEGE_PHYSICAL_VIEW = "physical-view";
    public static final String PRIVILEGE_TOPOLOGY_DESIGNER = "topology-designer";
    public static final String PRIVILEGE_DATA_MODEL_MANAGER = "data-model-manager";
    public static final String PRIVILEGE_LIST_TYPE_MANAGER = "list-type-manager";
    public static final String PRIVILEGE_CONTAINMENT_MANAGER = "containment-manager";
    public static final String PRIVILEGE_FAVORITES = "favorites";
    public static final String PRIVILEGE_SDH_MODULE = "sdh-module";
    public static final String PRIVILEGE_MPLS_MODULE = "mpls-module";
    public static final String PRIVILEGE_PROJECTS = "projects";
    public static final String PRIVILEGE_CONTRACT_MANAGER = "contract-manager";
    public static final String PRIVILEGE_IP_ADDRESS_MANAGER = "ip-address-manager";
    public static final String PRIVILEGE_SERVICE_MANAGER = "service-manager";
    public static final String PRIVILEGE_USER_MANAGER = "user-manager";
    public static final String PRIVILEGE_SOFTWARE_ASSETS_MANAGER = "software-assets-manager";
    public static final String PRIVILEGE_REPORTS = "reports";
    public static final String PRIVILEGE_TEMPLATES = "templates";
    public static final String PRIVILEGE_POOLS = "pools";
    public static final String PRIVILEGE_BULK_IMPORT = "bulk-import";
    public static final String PRIVILEGE_AUDIT_TRAIL = "audit-trail";
    public static final String PRIVILEGE_QUERY_MANAGER = "query-manager";
    public static final String PRIVILEGE_TASK_MANAGER = "task-manager";
    public static final String PRIVILEGE_SYNC = "sync";
    public static final String PRIVILEGE_CONTACTS = "contacts";
    public static final String PRIVILEGE_CONFIG_VARIABLES = "config-variables";
    public static final String PRIVILEGE_VALIDATORS = "validators";
    public static final String PRIVILEGE_WAREHOUSES = "warehouses";
    
    /**
     * For now, these privileges will be hard-coded, however in the near future, every module will provide its own set of tokens
     */
    public static String[] DEFAULT_PRIVILEGES = new String[] {
                                                PRIVILEGE_NAVIGATION_TREE, "Navigation Tree",
                                                PRIVILEGE_OBJECT_PROPERTIES, "Object Properties",
                                                PRIVILEGE_SPECIAL_EXPLORERS, "Special Explorers",
                                                PRIVILEGE_PHYSICAL_VIEW, "Physical View",
                                                PRIVILEGE_TOPOLOGY_DESIGNER, "Topology Designer",
                                                PRIVILEGE_DATA_MODEL_MANAGER, "Data Model Manager",
                                                PRIVILEGE_LIST_TYPE_MANAGER, "List Type Manager",
                                                PRIVILEGE_CONTAINMENT_MANAGER, "Containment Manager",
                                                PRIVILEGE_FAVORITES, "Favorites",
                                                PRIVILEGE_PROJECTS, "Projects",
                                                PRIVILEGE_SDH_MODULE, "SDH Module",
                                                PRIVILEGE_MPLS_MODULE, "MPLS Module",
                                                PRIVILEGE_CONTRACT_MANAGER, "Contract Manager",
                                                PRIVILEGE_IP_ADDRESS_MANAGER, "IP Address Manager",
                                                PRIVILEGE_SERVICE_MANAGER, "Service Manager",
                                                PRIVILEGE_USER_MANAGER, "User Manager",
                                                PRIVILEGE_SOFTWARE_ASSETS_MANAGER, "Software Assets Manager",
                                                PRIVILEGE_REPORTS, "Reports",
                                                PRIVILEGE_TEMPLATES, "Templates",
                                                PRIVILEGE_POOLS, "Pools",
                                                PRIVILEGE_BULK_IMPORT, "Bulk Import",
                                                PRIVILEGE_AUDIT_TRAIL, "Audit Trail",
                                                PRIVILEGE_QUERY_MANAGER, "Query Manager",
                                                PRIVILEGE_TASK_MANAGER, "Task Manager",
                                                PRIVILEGE_FAVORITES, "Favorites",
                                                PRIVILEGE_SYNC, "Inventory Synchronization"
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
