/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.springframework.stereotype.Service;

/**
 * All third-party commercial modules should inherit from this class.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractCommercialModule extends AbstractModule {
    /**
     * Gets the module's category. A category helps the user interface t
     * @return The module's category
     */
    public abstract String getCategory();
    
    /**
     * Says if the module can be used or not (for example, if the license has expired or not) or if there are unmet dependencies.
     * @throws OperationNotPermittedException The reason why the module could not be started.
     */
    public abstract void validate() throws OperationNotPermittedException;
}
