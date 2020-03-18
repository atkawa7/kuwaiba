/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.core.apis.integration;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Defines the behavior of all modules be it commercial, open source or third-party free contributions.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <W> The web component to be embedded into the web page. In Vaadin, this 
 * could be a Panel, or a VerticalLayout, for example.
 */
public abstract class AbstractModule<W> {
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;
    
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
     * Builds the web component that will be embedded in the web client.
     * @return The web component. 
     */
    public abstract W getWebComponent();
    
    /**
     * Says if the module can be used or not (for example, if the license has expired or not).
     * @throws OperationNotPermittedException if the module can not be loaded or used. 
     */
    public abstract void validate() throws OperationNotPermittedException;
    
    public enum ModuleType {
        TYPE_OPEN_SOURCE,
        TYPE_FREEWARE,
        TYPE_TRIAL,
        TYPE_PERPETUAL_LICENSE,
        TYPE_TEMPORARY_LICENSE,
        TYPE_OTHER
    }
}
