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

package org.neotropic.kuwaiba.modules.optional.serviceman.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Deletes a customer.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class DeleteCustomerAction extends AbstractAction {
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
        this.id = "serviceman.delete-customer";
        this.displayName = ts.getTranslatedString("module.serviceman.actions.delete-customer.name");
        this.description = ts.getTranslatedString("module.serviceman.actions.delete-customer.description");
        this.order = 10;
    
        setCallback((parameters) -> {
            try {
                BusinessObjectLight customer = (BusinessObjectLight)parameters.get(Constants.PROPERTY_RELATED_OBJECT);
                bem.deleteObject(customer.getClassName(), customer.getId(), false);
                return new ActionResponse();
            } catch (InventoryException ex) {
                throw new ModuleActionException(ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(DeleteCustomerAction.class.getName()).log(Level.SEVERE, ex.getMessage());
                throw new ModuleActionException(ts.getTranslatedString("module.general.messages.unexpected-error"));
            } 
        });
    }

    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }

    @Override
    public boolean requiresConfirmation() {
        return true;
    }
}
