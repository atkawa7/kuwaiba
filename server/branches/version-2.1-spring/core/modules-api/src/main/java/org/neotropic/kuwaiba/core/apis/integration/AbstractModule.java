/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.apis.integration;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;

/**
 * Defines the behavior of all modules be it commercial, open source or third-party free contributions.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractModule {
    /**
     * Reference to the metadata entity manager.
     */
    protected MetadataEntityManager mem;
    /**
     * Reference to the metadata entity manager.
     */
    protected ApplicationEntityManager aem;
    /**
     * Reference to the metadata entity manager.
     */
    protected BusinessEntityManager bem;
    
    /**
     * A simple unique string that identifies the module so it is easier to refer to it in automated processes such as defining 
     * if a user can user certain functionality based on his/her privileges.
     * @return 
     */
    public abstract String getId();
    /**
     * Gets the module's name. Must be unique, otherwise, the system will only take last one loaded at application's startup
     * @return The module's name
     */
    public abstract String getName();
    
    /**
     * Gets the module description
     * @return he module's description
     */
    public abstract String getDescription();
    
    /**
     * Gets the module's version
     * @return The module's version
     */
    public abstract String getVersion();
    
    /**
     * Gets the module's vendor
     * @return The module's vendor
     */
    public abstract String getVendor();
    
    /**
     * Gets the module's type. For valid values #ModuleTypes
     * @return The module's types
     */
    public abstract ModuleType getModuleType();
    
    /**
     * This method initializes the module. Must be called before anything else, otherwise the other modules won't be able to use the persistence service.
     * @param aem The ApplicationEntityManager instance. Might be null if not needed by the module
     * @param mem The MetadataEntityManager instance. Might be null if not needed by the module
     * @param bem The BusinessEntityManager instance. Might be null if not needed by the module
     */
    public void configureModule (MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
    }
    
    public enum ModuleType {
        TYPE_OPEN_SOURCE,
        TYPE_FREEWARE,
        TYPE_TRIAL,
        TYPE_PERPETUAL_LICENSE,
        TYPE_TEMPORARY_LICENSE,
        TYPE_OTHER
    }
}
