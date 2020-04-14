/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.modules.core.listtypeman.actions;

import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new List Type.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NewListTypeItemAction extends AbstractModuleAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @PostConstruct
    protected void init() {
        this.id = "listtypeman.new-list-type";
        this.displayName = ts.getTranslatedString("module.listtypeman.actions.new-list-type-item.name");
        this.description = ts.getTranslatedString("module.listtypeman.actions.new-list-type-item.description");
        this.order = 1000;
    
        setCallback((parameters) -> {
            HashMap<String, Object> parametersAsHashMap = ModuleActionParameter.asHashMap(parameters);
            String className = (String)parametersAsHashMap.get("className");
            String name = (String)parametersAsHashMap.get("name");
            String dispName = (String)parametersAsHashMap.get("displayName");
            
            HashMap<String, String> attributes = (HashMap<String, String>)parametersAsHashMap.get("attributes");
            try {
                aem.createListTypeItem(className, name, dispName);
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