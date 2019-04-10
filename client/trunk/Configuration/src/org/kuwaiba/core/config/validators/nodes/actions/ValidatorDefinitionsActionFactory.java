/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.core.config.validators.nodes.actions;

import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * A factory that provides single instances of the available actions in this module section.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ValidatorDefinitionsActionFactory {
    /**
     * The add validator configuration action
     */
    private static AddValidatorDefinitionAction addValidatorDefinitionAction;
    /**
     * The delete validator configuration action
     */
    private static DeleteValidatorDefinitionAction deleteValidatorDefinitionAction;
    
    /**
     * Returns a singleton instance of AddValidatorDefinitionAction
     * @return The singleton instance
     */
    public static GenericInventoryAction getAddValidatorDefinitionAction() {
        return addValidatorDefinitionAction == null ? addValidatorDefinitionAction = new AddValidatorDefinitionAction() : addValidatorDefinitionAction;
    }
    
    /**
     * Returns a singleton instance of DeleteValidatorDefinitionAction
     * @return The singleton instance
     */
    public static GenericInventoryAction getDeleteValidatorDefinitionAction() {
        return deleteValidatorDefinitionAction == null ? deleteValidatorDefinitionAction = new DeleteValidatorDefinitionAction(): deleteValidatorDefinitionAction;
    }
}
