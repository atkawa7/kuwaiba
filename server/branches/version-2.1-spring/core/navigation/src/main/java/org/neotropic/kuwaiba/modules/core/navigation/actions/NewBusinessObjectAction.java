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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Create a business object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class NewBusinessObjectAction extends AbstractAction {
    /**
     * New business object action parameter class name
     */
    public static String PARAM_CLASS_NAME = "className"; //NOI18N
    /**
     * New business object action parameter parent class name
     */
    public static String PARAM_PARENT_CLASS_NAME = "parentClassName"; //NOI18N
    /**
     * New business object action parameter id
     */
    public static String PARAM_PARENT_OID = "id"; //NOI18N
    /**
     * New business object action parameter id
     */
    public static String PARAM_OBJECTS_OIDS = "ids"; //NOI18N
    /**
     * New business object action parameter attributes
     */
    public static String PARAM_ATTRIBUTES = "attributes"; //NOI18N
    /**
     * New business object action parameter template id
     */
    public static String PARAM_TEMPLATE_ID = "templateId"; //NOI18N
    /**
     * Pattern to create multiple objects
     */
    public static String PARAM_PATTERN = "pattern"; //NOI18N
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    
    @PostConstruct
    protected void init() {
        this.id = "navigation.new-business-object";
        this.displayName = ts.getTranslatedString("module.navigation.actions.new-business-object.name");
        this.description = ts.getTranslatedString("module.navigation.actions.new-business-object.description");
        this.icon = new Icon(VaadinIcon.PLUS);
        this.order = 1;
        
        setCallback(parameters -> {
            try {
                String className = (String) parameters.get(PARAM_CLASS_NAME);
                String parentClassName = (String) parameters.get(PARAM_PARENT_CLASS_NAME);
                String parentOid = (String) parameters.get(PARAM_PARENT_OID);
                HashMap<String, String> attributes = (HashMap) parameters.get(PARAM_ATTRIBUTES);
                String templateId = (String) parameters.get(PARAM_TEMPLATE_ID);
                String pattern = (String) parameters.get(PARAM_PATTERN);
                if(pattern != null)
                    bem.createBulkObjects(className, parentClassName, parentOid, pattern);
                else
                    bem.createObject(className, parentClassName, parentOid, attributes, templateId);
                return new ActionResponse();
            } catch (InventoryException ex) {
                throw new ModuleActionException(ex.getMessage());
            }
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
