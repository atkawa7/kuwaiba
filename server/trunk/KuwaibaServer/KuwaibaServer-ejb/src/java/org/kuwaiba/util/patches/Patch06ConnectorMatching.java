/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.kuwaiba.util.patches;

import java.util.Calendar;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * Patches the database adds the classes and attributes in the data model to allow 
 * that the connector matching rules can be configured
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Patch06ConnectorMatching extends GenericPatch {    
    
    public Patch06ConnectorMatching() {
    }

    @Override
    public String getId() {
        return "6";
    }

    @Override
    public String getTitle() {
        return "Connector Matching";
    }

    @Override
    public String getDescription() {
        return "This action adds the classes and attributes in the data model to allow that the connector matching rules can be configured";
    }

    @Override
    public String getSourceVersion() {
        return "1.1.x";
    }

    @Override
    public String getTargetVersion() {
        return "1.5";
    }

    @Override
    public PatchResult executePatch() {
        PatchResult result = new PatchResult();
        result.setResultType(PatchResult.RESULT_SUCCESS);
        
        MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
        ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
        
        if (mem == null || aem == null) {
            result.setResultType(PatchResult.RESULT_ERROR);
            return result;
        }
        
        ClassMetadata cm = new ClassMetadata();                    
        cm.setDisplayName("");
        cm.setDescription("");                    
        cm.setAbstract(false);
        cm.setColor(0);
        cm.setCountable(false);
        cm.setCreationDate(Calendar.getInstance().getTimeInMillis());
        cm.setIcon(null);
        cm.setSmallIcon(null);
        cm.setCustom(false);
        cm.setViewable(true);
        cm.setInDesign(false);

        AttributeMetadata attributeModel = new AttributeMetadata();
        attributeModel.setDescription("");
        attributeModel.setReadOnly(false);                    
        attributeModel.setUnique(false);
        attributeModel.setVisible(true);
        attributeModel.setNoCopy(false);

        attributeModel.setName("connectorType"); //NOI18N
        attributeModel.setDisplayName("connectorType"); //NOI18N

        try {
            cm.setName("LinkConnectorType"); //NOI18N
            cm.setParentClassName("GenericType"); //NOI18N
            mem.createClass(cm);

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT,
                String.format("Created class %s", cm.getName()));

        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(ex.getMessage());
        }                        
        try {
            attributeModel.setType("LinkConnectorType"); //NOI18N
            mem.createAttribute("GenericPhysicalLink", attributeModel, true); //NOI18N

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added attributes to class %s", "GenericPhysicalLink")); //NOI18N

        } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }
        try {
            cm.setName("PortConnectorType"); //NOI18N
            cm.setParentClassName("GenericType"); //NOI18N

            mem.createClass(cm);

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        } 
        try {
            attributeModel.setType("PortConnectorType"); //NOI18N
            mem.createAttribute("GenericPort", attributeModel, true); //NOI18N

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added attributes to class %s", "GenericPort")); //NOI18N
        } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }
        return result;
    }
    
    @Override
    public String getMandatory() {
        return "[Mandatory] ";
    }
    
}
