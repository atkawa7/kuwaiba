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

package org.kuwaiba.core.config.variables.nodes.actions;

import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * A factory that provides single instances of the available actions in this module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ConfigurationVariablesActionFactory {
    /**
     * The add configuration variable action
     */
    private static AddConfigurationVariableAction addConfigurationVariableAction;
    /**
     * The delete configuration variable action
     */
    private static DeleteConfigurationVariableAction deleteConfigurationVariableAction;
    /**
     * The add configuration variables pool action
     */
    private static AddConfigurationVariablesPoolAction addConfigurationVariablesPoolAction;
    /**
     * The deletes configuration variables pool action
     */
    private static DeleteConfigurationVariablesPoolAction deleteConfigurationVariablesPoolAction;
    
    /**
     * Returns a singleton instance of AddConfigurationVariableAction
     * @return The singleton instance
     */
    public static GenericInventoryAction getAddConfigurationVariableAction() {
        return addConfigurationVariableAction == null ? addConfigurationVariableAction = new AddConfigurationVariableAction() : addConfigurationVariableAction;
    }
    
    /**
     * Returns a singleton instance of DeleteConfigurationVariableAction
     * @return The singleton instance
     */
    public static GenericInventoryAction getDeleteConfigurationVariableAction() {
        return deleteConfigurationVariableAction == null ? deleteConfigurationVariableAction = new DeleteConfigurationVariableAction() : deleteConfigurationVariableAction;
    }
    
    /**
     * Returns a singleton instance of AddConfigurationVariablesPoolAction
     * @return The singleton instance
     */
    public static GenericInventoryAction getAddConfigurationVariablesPoolAction() {
        return addConfigurationVariablesPoolAction == null ? addConfigurationVariablesPoolAction = new AddConfigurationVariablesPoolAction() : addConfigurationVariablesPoolAction;
    }
    
    /**
     * Returns a singleton instance of DeleteConfigurationVariablesPoolAction
     * @return The singleton instance
     */
    public static GenericInventoryAction getDeleteConfigurationVariablesPoolAction() {
        return deleteConfigurationVariablesPoolAction == null ? deleteConfigurationVariablesPoolAction = new DeleteConfigurationVariablesPoolAction() : deleteConfigurationVariablesPoolAction;
    }
}
