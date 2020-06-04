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
package org.neotropic.kuwaiba.modules.optional.physcon.persistence;

import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService.EXECUTION_STATE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class PhysicalConnectionService {
    /**
     * The Translation Service instance.
     */
    private TranslationService ts;
    /**
     * The Persistence Service instance.
     */
    @Autowired
    private PersistenceService persistenceService;
    /**
     * The Application Entity Manager instance.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * The Business Entity Manager instance.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The Metadata Entity Manager instance.
     */
    @Autowired
    private MetadataEntityManager mem;
    
    public void deletePhysicalConnection(String objectClassName, String objectId, String userName) throws IllegalStateException, InventoryException {
        if (persistenceService.getState() == EXECUTION_STATE.STOPPED)
            throw new IllegalStateException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, objectClassName))
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.physcon.messages.is-not-a-phys-conn"), objectClassName));
        bem.deleteObject(objectClassName, objectId, true);
        aem.createGeneralActivityLogEntry(userName, 
            ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
            String.format("Deleted %s instance with id %s", objectClassName, objectId));
    }
}
