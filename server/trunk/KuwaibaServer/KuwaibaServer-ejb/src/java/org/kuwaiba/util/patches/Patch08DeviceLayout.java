/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 */

package org.kuwaiba.util.patches;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * Patches the database so that the Device Layout feature can be used
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Patch08DeviceLayout extends GenericPatch {
    
    public Patch08DeviceLayout() {
    }
    
    @Override
    public String getId() {
        return "8";
    }

    @Override
    public String getTitle() {
        return "Device Layout Support";
    }

    @Override
    public String getDescription() {
        return "Creates the class <i>GenericApplicationListType</i> and its subclass <i>CustomShape</i> to allow that the Devices Layout to work correctly";
    }

    @Override
    public String getSourceVersion() {
        return "1.5";
    }

    @Override
    public String getTargetVersion() {
        return "1.6";
    }

    @Override
    public PatchResult executePatch() {
        PatchResult result = new PatchResult();
        
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
        MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
        ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
        
        if (bem == null || mem == null || aem == null) {
            result.setResultType(PatchResult.RESULT_ERROR);
            return result;
        }
        try {
            ClassMetadata customShapeClassMetadata = mem.getClass("PredefinedShape"); //NOI18N

            ClassMetadata customShape = new ClassMetadata();
            customShape.setId(customShapeClassMetadata.getId());
            customShape.setName("CustomShape"); //NOI18N
            mem.setClassProperties(customShape);

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT,
                "Changed the PredefinedShape class name to CustomShape");
            
            result.setResultType(PatchResult.RESULT_SUCCESS);
            return result;
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException 
            | InvalidArgumentException | ObjectNotFoundException ex) {}
        
        ClassMetadata classMetadata = new ClassMetadata();
        classMetadata.setDisplayName("");
        classMetadata.setDescription("");
        classMetadata.setColor(0);
        classMetadata.setCountable(true);
        classMetadata.setCreationDate(Calendar.getInstance().getTimeInMillis());
        classMetadata.setIcon(null);
        classMetadata.setSmallIcon(null);
        classMetadata.setCustom(true);
        classMetadata.setViewable(true);
        classMetadata.setInDesign(false);

        try {
            mem.getClass("GenericApplicationListType");
            result.setResultType(PatchResult.RESULT_SUCCESS);
            return result;
        } catch (MetadataObjectNotFoundException ex) {}

        long genericApplicationListTypeId = -1;

        try {
            classMetadata.setName("GenericApplicationListType"); //NOI18N
            classMetadata.setParentClassName("GenericObjectList"); //NOI18N
            classMetadata.setAbstract(true);                        

            genericApplicationListTypeId = mem.createClass(classMetadata);

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", classMetadata.getName()));

        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.getMessages().add(" * " + ex.getMessage());
        }

        try {
            mem.getClass("CustomShape"); //NOI18N
            result.setResultType(PatchResult.RESULT_SUCCESS);
            return result;
        } catch (MetadataObjectNotFoundException ex) {}

        if (genericApplicationListTypeId != -1) {
            long customShapeId = -1;
            try {
                classMetadata.setName("CustomShape"); //NOI18N
                classMetadata.setParentClassName("GenericApplicationListType"); //NOI18N
                classMetadata.setAbstract(false);

                customShapeId = mem.createClass(classMetadata);

                aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                    String.format("Created class %s", classMetadata.getName()));

            } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                result.getMessages().add(" * " + ex.getMessage());
            }
            if (customShapeId != -1) {
                try {
                    AttributeMetadata attributeMetadata = new AttributeMetadata();
                    attributeMetadata.setDescription("");
                    attributeMetadata.setReadOnly(false);
                    attributeMetadata.setUnique(false);
                    attributeMetadata.setVisible(true);
                    attributeMetadata.setNoCopy(false);

                    attributeMetadata.setName("icon"); //NOI18N
                    attributeMetadata.setDisplayName("icon"); 
                    attributeMetadata.setType("Binary"); //NOI18N
                    mem.createAttribute(customShapeId, attributeMetadata);

                    aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                        ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT,
                        String.format("Added attributes to class %s", "CustomShape"));
                } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                    result.getMessages().add(" * " + ex.getMessage());
                }
            }
        }
        return result;
    }
    
    @Override
    public String getMandatory() {
        return "[Mandatory] ";
    }
    
}
