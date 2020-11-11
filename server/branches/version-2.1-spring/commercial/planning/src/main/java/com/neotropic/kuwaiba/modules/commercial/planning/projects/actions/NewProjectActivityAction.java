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
package com.neotropic.kuwaiba.modules.commercial.planning.projects.actions;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new Activity.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProjectActivityAction extends AbstractAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts; 
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    @PostConstruct
    protected void init() {
        this.id = "projects.new-activity";
        this.displayName = ts.getTranslatedString("module.projects.actions.activity.new-activity.name");
        this.description = ts.getTranslatedString("module.projects.actions.activity.new-activity.description");
        this.order = 1000;
        
        setCallback((parameters) -> {
            BusinessObjectLight project = (BusinessObjectLight) parameters.get("project");
            String name = (String) parameters.get("name");
            String instanceOfClass = (String) parameters.get("class");
            HashMap<String, String> attributes = new HashMap();
            attributes.put(Constants.PROPERTY_NAME, name);
           
                try {
                    bem.createSpecialObject(instanceOfClass, project.getClassName(), project.getId(), attributes, null);
                } catch (BusinessObjectNotFoundException | OperationNotPermittedException | MetadataObjectNotFoundException 
                        | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                    Logger.getLogger(NewProjectActivityAction.class.getName()).log(Level.SEVERE, null, ex);
                }
            return new ActionResponse();     
        });
    } 
    
    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }
    
}
