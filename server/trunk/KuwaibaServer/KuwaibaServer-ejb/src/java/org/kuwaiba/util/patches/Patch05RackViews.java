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

import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * Patches the database adds the attribute rackUnitsNumberingDescending to the Rack class
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Patch05RackViews extends GenericPatch {
    
    public Patch05RackViews() {
    }

    @Override
    public String getId() {
        return "5";
    }

    @Override
    public String getTitle() {
        return "Rack Views";
    }

    @Override
    public String getDescription() {
        return "This action adds the attribute rackUnitsNumberingDescending to the Rack class, adding support for ascending and descending rack unit numbering";
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
        try {
            AttributeMetadata attributeModel = new AttributeMetadata();
            attributeModel.setName("rackUnitsNumberingDescending"); //NOI18N
            attributeModel.setDisplayName("rackUnitsNumberingDescending"); //NOI18N
            attributeModel.setDescription("");
            attributeModel.setReadOnly(false);
            attributeModel.setType("Boolean"); //NOI18N
            attributeModel.setUnique(false);
            attributeModel.setVisible(true);
            attributeModel.setNoCopy(false); 

            mem.createAttribute("Rack", attributeModel, true); //NOI18N

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Added attributes to class %s", "Rack")); //NOI18N
        } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(ex.getMessage());
        }
        return result;
    }
    
    @Override
    public String getMandatory() {
        return "[Mandatory] ";
    }
    
}
