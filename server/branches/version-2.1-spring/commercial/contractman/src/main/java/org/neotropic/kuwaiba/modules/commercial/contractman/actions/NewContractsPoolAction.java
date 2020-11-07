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
package org.neotropic.kuwaiba.modules.commercial.contractman.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new Contracts Pool.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewContractsPoolAction extends AbstractAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;    
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Type of pool module root. These pools are used in models and are the root of such model
     */
    public static final int POOL_TYPE_MODULE_ROOT = 2;    
    
    @PostConstruct
    protected void init() {
        this.id = "contractman.new-contracts-pool";
        this.displayName = ts.getTranslatedString("module.contractman.actions.pool.new-pool.name");
        this.description = ts.getTranslatedString("module.contractman.actions.pool.new-pool.description");
        this.order = 1000;

        setCallback((parameters) -> {
            String name = (String) parameters.get("name");
            String dispName = (String)  parameters.get("description");
            String instanceOfClass = (String) parameters.get("class");
            
                try {
                    aem.createRootPool(name, dispName, instanceOfClass, POOL_TYPE_MODULE_ROOT);
                } catch (MetadataObjectNotFoundException ex) {
                    Logger.getLogger(NewContractsPoolAction.class.getName()).log(Level.SEVERE, null, ex);
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
