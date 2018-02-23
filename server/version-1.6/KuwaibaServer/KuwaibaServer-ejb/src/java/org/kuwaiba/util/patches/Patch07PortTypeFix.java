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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.beans.ToolsBean;
import org.kuwaiba.util.ChangeDescriptor;

/**
 * Patches the database updates the classes ElectricalPort and OpticalPort
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Patch07PortTypeFix extends GenericPatch {
    
    public Patch07PortTypeFix() {
    }

    @Override
    public String getId() {
        return "7";
    }

    @Override
    public String getTitle() {
        return "Port Type Fix";
    }

    @Override
    public String getDescription() {
        return "This patch will update the classes ElectricalPort and OpticalPort, changing the type of the <i>type</i> attribute to ElectricalPortType and OpticalPortType respectively, to fix a bug detected in a default datamodel. <b>Warning: This action will set to null the value of the attribute in the existing instances</b>";
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
            ClassMetadata electricalPortType = mem.getClass("ElectricalPortType"); //NOI18N
            if (!"CommunicationsPortType".equals(electricalPortType.getParentClassName())) //NOI18N
                return result;
        } catch (MetadataObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(ex.getMessage());
            return result;
        }

        try {
            ClassMetadata electricalLinkPort = mem.getClass("ElectricalPort"); //NOI18N

            if (electricalLinkPort.hasAttribute("type")) { //NOI18N
                AttributeMetadata oldAttr = electricalLinkPort.getAttribute("type"); //NOI18N

                try {
                    AttributeMetadata newAttr = new AttributeMetadata();
                    newAttr.setId(oldAttr.getId());
                    newAttr.setName(null);
                    newAttr.setDisplayName(null);
                    newAttr.setDescription(null);
                    newAttr.setType("ElectricalPortType"); //NOI18N
                    newAttr.setAdministrative(null);
                    newAttr.setUnique(null);
                    newAttr.setMandatory(null);
                    newAttr.setVisible(null);
                    newAttr.setReadOnly(null);
                    newAttr.setNoCopy(null);

                    ChangeDescriptor changeDescriptor = mem.setAttributeProperties(electricalLinkPort.getId(), newAttr);

                    aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN,
                        ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT,
                        changeDescriptor);
                } catch (InvalidArgumentException | ObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                    Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (MetadataObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(", " + ex.getMessage());
            return result;
        }

        try {
            ClassMetadata opticalPortType = mem.getClass("OpticalPortType"); //NOI18N

            if (!"CommunicationsPortType".equals(opticalPortType.getParentClassName())) //NOI18N
                return result;
        } catch (MetadataObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(ex.getMessage());
            return result;
        }

        try {
            ClassMetadata opticalLinkPort = mem.getClass("OpticalPort"); //NOI18N
            if (opticalLinkPort.hasAttribute("type")) { //NOI18N
                AttributeMetadata oldAttr = opticalLinkPort.getAttribute("type"); //NOI18N

                try {
                    AttributeMetadata newAttr = new AttributeMetadata();
                    newAttr.setId(oldAttr.getId());
                    newAttr.setName(null);
                    newAttr.setDisplayName(null);
                    newAttr.setDescription(null);
                    newAttr.setType("OpticalPortType"); //NOI18N
                    newAttr.setAdministrative(null);
                    newAttr.setUnique(null);
                    newAttr.setMandatory(null);
                    newAttr.setVisible(null);
                    newAttr.setReadOnly(null);
                    newAttr.setNoCopy(null);
                    ChangeDescriptor changeDescriptor = mem.setAttributeProperties(opticalLinkPort.getId(), newAttr);

                    aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN,
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT,
                    changeDescriptor);
                } catch (InvalidArgumentException | ObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                    result.setResultType(PatchResult.RESULT_ERROR);
                    result.getMessages().add(", " + ex.getMessage());
                }
            }
        } catch (MetadataObjectNotFoundException ex) {
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
