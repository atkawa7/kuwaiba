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
 * Patches the database adds the abstract classes GenericProject, GenericActivity 
 * and some sample subclasses to allow that the Projects Module to work correctly
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Patch04ProjectsModuleSupport extends GenericPatch {
    
    public Patch04ProjectsModuleSupport() {
    }

    @Override
    public String getId() {
        return "4";
    }

    @Override
    public String getTitle() {
        return "Projects Module Support";
    }

    @Override
    public String getDescription() {
        return "This action adds the abstract classes GenericProject, GenericActivity and some sample subclasses to allow that the Projects Module to work correctly";
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
        cm.setColor(0);
        cm.setCountable(true);
        cm.setCreationDate(Calendar.getInstance().getTimeInMillis());
        cm.setIcon(null);
        cm.setSmallIcon(null);
        cm.setCustom(true);
        cm.setViewable(true);
        cm.setInDesign(false);

        AttributeMetadata attributeModel = new AttributeMetadata();
        attributeModel.setDescription("");
        attributeModel.setReadOnly(false);
        attributeModel.setUnique(false);
        attributeModel.setVisible(true);
        attributeModel.setNoCopy(false);

        long genericProjectId = -1;                    
        try {
            cm.setName("GenericProject"); //NOI18N
            cm.setParentClassName("AdministrativeItem"); //NOI18N
            cm.setAbstract(true);

            genericProjectId = mem.createClass(cm);

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));

        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(ex.getMessage());
        }
        if (genericProjectId != -1) {
            try {
                attributeModel.setName("notes"); //NOI18N
                attributeModel.setDisplayName("notes"); 
                attributeModel.setType("String"); //NOI18N
                mem.createAttribute(genericProjectId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }                        
            try {
                attributeModel.setName("projectManager"); //NOI18N
                attributeModel.setDisplayName("projectManager");
                attributeModel.setType("Employee"); //NOI18N
                mem.createAttribute(genericProjectId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }                        
            try {
                attributeModel.setName("startDate"); //NOI18N
                attributeModel.setDisplayName("startDate");
                attributeModel.setType("Date"); //NOI18N
                mem.createAttribute(genericProjectId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }                        
            try {
                cm.setName("ProjectStatusType"); //NOI18N
                cm.setParentClassName("GenericType"); //NOI18N
                cm.setAbstract(false);

                cm.setId(mem.createClass(cm));

                aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                    String.format("Created class %s", cm.getName()));

            } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("status"); //NOI18N
                attributeModel.setDisplayName("status");
                attributeModel.setType("ProjectStatusType"); //NOI18N                        
                mem.createAttribute(genericProjectId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }                        
            try {
                aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                    String.format("Added attributes to class %s", "GenericProject"));

            } catch (ApplicationObjectNotFoundException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
        }

        long genericActivityId = -1;
        try {
            cm.setName("GenericActivity"); //NOI18N
            cm.setParentClassName("AdministrativeItem"); //NOI18N
            cm.setAbstract(true);

            genericActivityId = mem.createClass(cm);
            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));

        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }
        if (genericActivityId != -1) {
            try {
                cm.setName("ActivityType"); //NOI18N
                cm.setParentClassName("GenericType"); //NOI18N
                cm.setAbstract(false);

                cm.setId(mem.createClass(cm));

                aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                    String.format("Created class %s", cm.getName()));

            } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("activityType"); //NOI18N
                attributeModel.setDisplayName("activityType");
                attributeModel.setType("ActivityType"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("sequecing"); //NOI18N
                attributeModel.setDisplayName("sequecing");
                attributeModel.setType("ActivityType"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }                        
            try {
                cm.setName("ActivityStatusType"); //NOI18N
                cm.setParentClassName("GenericType"); //NOI18N
                cm.setAbstract(false);

                cm.setId(mem.createClass(cm));

                aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                    String.format("Created class %s", cm.getName()));

            } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("status"); //NOI18N
                attributeModel.setDisplayName("status");
                attributeModel.setType("ActivityStatusType"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }                        
            try {
                attributeModel.setName("notes"); //NOI18N
                attributeModel.setDisplayName("notes");
                attributeModel.setType("String"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("startDate"); //NOI18N
                attributeModel.setDisplayName("startDate");
                attributeModel.setType("Date"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("endDate"); //NOI18N
                attributeModel.setDisplayName("endDate");
                attributeModel.setType("Date"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("lastUpdate"); //NOI18N
                attributeModel.setDisplayName("lastUpdate");
                attributeModel.setType("Date"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("duration"); //NOI18N
                attributeModel.setDisplayName("duration");
                attributeModel.setType("Float"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("cost"); //NOI18N
                attributeModel.setDisplayName("cost");
                attributeModel.setType("Float"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                attributeModel.setName("owner"); //NOI18N
                attributeModel.setDisplayName("owner");
                attributeModel.setType("Employee"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }                    
            try {
                attributeModel.setName("risk"); //NOI18N
                attributeModel.setDisplayName("risk");
                attributeModel.setType("Integer"); //NOI18N
                mem.createAttribute(genericActivityId, attributeModel);

            } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
            try {
                aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                        ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT,
                        String.format("Added attributes to class %s", "Generic Activity"));
            } catch (ApplicationObjectNotFoundException ex) {
                result.setResultType(PatchResult.RESULT_ERROR);
                result.getMessages().add(", " + ex.getMessage());
            }
        }                    
        try {
            cm.setName("GeneralPurposeActivity"); //NOI18N
            cm.setParentClassName("GenericActivity"); //NOI18N
            cm.setAbstract(false);

            cm.setId(mem.createClass(cm));

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }                    
        try {                        
            cm.setName("PlanningActivity"); //NOI18N
            cm.setParentClassName("GenericActivity"); //NOI18N
            cm.setAbstract(false);

            cm.setId(mem.createClass(cm));

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }                    
        try {                        
            cm.setName("RollOutActivity"); //NOI18N
            cm.setParentClassName("GenericActivity"); //NOI18N
            cm.setAbstract(false);

            cm.setId(mem.createClass(cm));

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }                    
        try {                        
            cm.setName("DesignActivity"); //NOI18N
            cm.setParentClassName("GenericActivity"); //NOI18N
            cm.setAbstract(false);

            cm.setId(mem.createClass(cm));

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }                    
        try {
            cm.setName("AuditActivity"); //NOI18N
            cm.setParentClassName("GenericActivity"); //NOI18N
            cm.setAbstract(false);

            cm.setId(mem.createClass(cm));

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }                    
        try {
            cm.setName("GeneralPurposeProject"); //NOI18N
            cm.setParentClassName("GenericProject"); //NOI18N
            cm.setAbstract(false);

            cm.setId(mem.createClass(cm));

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class ", cm.getName()));
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
        }                    
        try {
            cm.setName("NetworkProject"); //NOI18N
            cm.setParentClassName("GenericProject"); //NOI18N
            cm.setAbstract(false);

            cm.setId(mem.createClass(cm));

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created class %s", cm.getName()));
        } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
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
