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

import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
import org.neotropic.kuwaiba.core.persistence.PersistenceService.EXECUTION_STATE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage the physical connections
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
    /**
     * A side in a physical connection
     */
    public static String RELATIONSHIP_ENDPOINTA = "endpointA"; //NOI18N
    /**
     * B side in a physical connection
     */
    public static String RELATIONSHIP_ENDPOINTB = "endpointB"; //NOI18N
    
    public String createPhysicalConnection(String aObjectClass, String aObjectId, 
        String bObjectClass, String bObjectId, String name, String connectionClass,
        String templateId, String userName) throws IllegalStateException, OperationNotPermittedException {
        if (persistenceService.getState() == EXECUTION_STATE.STOPPED)
            throw new IllegalStateException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        String newConnectionId = null;
        try {
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new OperationNotPermittedException(String.format(ts.getTranslatedString("module.general.messages.is-not-subclass"), connectionClass, Constants.CLASS_GENERICPHYSICALCONNECTION)); //NOI18N
            
            //The connection (either link or container, will be created in the closest common parent between the endpoints)
            BusinessObjectLight commonParent = bem.getCommonParent(aObjectClass, aObjectId, bObjectClass, bObjectId);
            
            if (commonParent == null || commonParent.getName().equals(Constants.DUMMY_ROOT))
                throw new OperationNotPermittedException(ts.getTranslatedString("module.physcon.messages.no-common-parent"));
            
            boolean isLink = false;
            
            //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
            if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, connectionClass)) { //NOI18N
                
                if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, aObjectClass) || !mem.isSubclassOf(Constants.CLASS_GENERICPORT, bObjectClass)) //NOI18N
                    throw new OperationNotPermittedException(ts.getTranslatedString("module.physcon.messages.is-not-a-port"));
                
                if (!bem.getSpecialAttribute(aObjectClass, aObjectId, RELATIONSHIP_ENDPOINTA).isEmpty()) //NOI18N
                    
                    throw new OperationNotPermittedException(String.format(ts.getTranslatedString("module.physcon.messages.endpoint-connected"), bem.getObjectLight(aObjectClass, aObjectId)));

                if (!bem.getSpecialAttribute(bObjectClass, bObjectId, RELATIONSHIP_ENDPOINTB).isEmpty()) //NOI18N
                    throw new OperationNotPermittedException(String.format(ts.getTranslatedString("module.physcon.messages.endpoint-connected"), bem.getObjectLight(bObjectClass, bObjectId)));
                
                isLink = true;
            }

            
            HashMap<String, String> attributes = new HashMap<>();
            if (name == null || name.isEmpty())
                throw new OperationNotPermittedException(ts.getTranslatedString("module.physcon.messages.name-empty"));
            
            attributes.put(Constants.PROPERTY_NAME, name);
            
            newConnectionId = bem.createSpecialObject(connectionClass, commonParent.getClassName(), commonParent.getId(), attributes, templateId);
            
            if (isLink) { //Check connector mappings only if it's a link
                aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, aObjectClass, aObjectId);
                aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, bObjectClass, bObjectId);
            }
            
            bem.createSpecialRelationship(connectionClass, newConnectionId, aObjectClass, aObjectId, RELATIONSHIP_ENDPOINTA, true);
            bem.createSpecialRelationship(connectionClass, newConnectionId, bObjectClass, bObjectId, RELATIONSHIP_ENDPOINTB, true);
            
            aem.createGeneralActivityLogEntry(userName, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [%s] (%s)", name, connectionClass, newConnectionId));
            
            return newConnectionId;
        } catch (InventoryException e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null) {
                try {
                    bem.deleteObject(connectionClass, newConnectionId, true);
                } catch (InventoryException ex) {
                }
            }
            throw new OperationNotPermittedException(e.getMessage());
        }
    }
    
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
