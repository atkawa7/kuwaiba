/*
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
 * Patches the database creating the EquipmentModel class and the model attribute to
 * GenericCommunicationsElement
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Patch09EquipmentModel extends GenericPatch {

    @Override
    public String getId() {
        return "9";        
    }

    @Override
    public String getTitle() {
        return "Attribute model";
    }

    @Override
    public String getDescription() {
        return "This patch will create the attribute <i>model</i> of type <i>EquipmentModel</i> in the classes <i>GenericCommunicationsElement</i> and <i>GenericDistributionFrame</i> and its subclasses. "
                + "If not want create the attribute <i>model</i> for all <i>GenericCommunicationsElement</i> and <i>GenericDistributionFrame</i> instances, create the attribute model manually with type as subclass of <i>GenericObjectList<i>"
                + "<br /><b>Warning: this patch will fail if any subclass of <i>GenericCommunicationsElement</i> or <i>GenericDistributionFrame</i> already has an attribute called <i>model</i></b> and won't migrate the old values to the new model."
                + "<br /><i>What to do?</i>:"
                + "<br />1. Delete the attribute <i>model</i> in the said subclass."
                + "<br />2. Run the patch."
                + "<br />3. Create an <i>EquipmentModel</i> instance for each <i>model</i> value you already had prior to the execution of the patch."
                + "<br />4. Manually set the <i>model</i> attribute to the respective value using the the enumeration you just populated in the instances that might have been affected";
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
        String equipmentModelClassName = "EquipmentModel"; //NOI18N
        ClassMetadata equipmentTypeClass;

        try {
            equipmentTypeClass = mem.getClass(equipmentModelClassName); 
        } catch (MetadataObjectNotFoundException ex) {
            equipmentTypeClass = null;
        }
        if (equipmentTypeClass == null) {                                            
            try {
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
                cm.setName(equipmentModelClassName);
                cm.setParentClassName("GenericType"); //NOI18N

                mem.createClass(cm);

                aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT,
                    String.format("Created class %s", cm.getName()));

            } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                result.getMessages().add(" * " + ex.getMessage());
            }
        }                    
        List<RemoteBusinessObjectLight> equipments;

        try {
            equipments = bem.getObjectsOfClassLight("GenericCommunicationsElement", 0); //NOI18N
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            result.getMessages().add(" * " + ex.getMessage());
            result.setResultType(PatchResult.RESULT_ERROR);
            return result;
        }
        List<String> classesToRemoveModelAttr = new ArrayList();
        // Search the classes that cannot be applied the patch and need remove the model attribute
        for (RemoteBusinessObjectLight equipment : equipments) {
            ClassMetadata equipmentClass;
            try {
                equipmentClass = mem.getClass(equipment.getClassName());
            } catch (MetadataObjectNotFoundException ex) {
                result.getMessages().add(" * " + ex.getMessage());
                result.setResultType(PatchResult.RESULT_ERROR);
                return result;
            }
            AttributeMetadata attrMetadataModel = equipmentClass.getAttribute("model"); //NOI18N
            try {
                if (attrMetadataModel != null && mem.isSubClass("GenericType", attrMetadataModel.getType())/*equipmentModelClassName.equals(attrMetadataModel.getType())*/)
                    continue;
            } catch (MetadataObjectNotFoundException ex) {
                result.getMessages().add(" * " + ex.getMessage());
                result.setResultType(PatchResult.RESULT_ERROR);
                return result;
            }

            RemoteBusinessObject object;
            try {
                object = bem.getObject(equipment.getClassName(), equipment.getId());
            } catch (MetadataObjectNotFoundException | ObjectNotFoundException | InvalidArgumentException ex) {
                result.getMessages().add(" * " + ex.getMessage());
                result.setResultType(PatchResult.RESULT_ERROR);
                return result;
            }
            if (object.getAttributes().containsKey("model")) { //NOI18N
                String currentModel = object.getAttributes().get("model").get(0); //NOI18N

                result.getMessages().add(" * Fail reason: 2 for object with id = " + object.getName() + " class = " + object.getClassName() + " model = " + "\"" + currentModel + "\"" + " see patch description");
                classesToRemoveModelAttr.add(object.getClassName());
            }
        }
        if (classesToRemoveModelAttr.isEmpty()) {
            ClassMetadata genericComElementClass;
            ClassMetadata genericDistributionFrame;
            try {
                genericComElementClass = mem.getClass("GenericCommunicationsElement"); //NOI18N
                genericDistributionFrame = mem.getClass("GenericDistributionFrame"); //NOI18N
            } catch (MetadataObjectNotFoundException ex) {
                result.getMessages().add(" * " + ex.getMessage());
                result.setResultType(PatchResult.RESULT_ERROR);
                return result;
            }
            AttributeMetadata attributeModel = new AttributeMetadata();
            attributeModel.setDescription("");
            attributeModel.setReadOnly(false);                    
            attributeModel.setUnique(false);
            attributeModel.setVisible(true);
            attributeModel.setNoCopy(false);
            attributeModel.setName("model"); //NOI18N
            attributeModel.setDisplayName("model"); //NOI18N
            attributeModel.setType(equipmentModelClassName);

            if (!genericComElementClass.hasAttribute("model")) { //NOI18N

                try {
                    mem.createAttribute("GenericCommunicationsElement", attributeModel, false); //NOI18N
                    aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                    String.format("Added attributes to class %s", "GenericCommunicationsElement")); //NOI18N
                } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                    result.getMessages().add(" * Fail reason: 1 " + ex.getMessage() + " see the patch description");
                    result.setResultType(PatchResult.RESULT_ERROR);
                    return result;
                }
            }
            if (!genericDistributionFrame.hasAttribute("model")) { //NOI18N
                try {
                    mem.createAttribute("GenericDistributionFrame", attributeModel, false); //NOI18N
                    aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                    String.format("Added attributes to class %s", "GenericDistributionFrame")); //NOI18N
                } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                    result.getMessages().add(" * Fail reason: 1 " + ex.getMessage() + " see the patch description");
                    result.setResultType(PatchResult.RESULT_ERROR);
                    return result;
                }                                                
            }
        }
        return result;
    }

    @Override
    public String getMandatory() {
        return "";
    }
    
}
