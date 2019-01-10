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
package com.neotropic.inventory.modules.contracts.nodes.actions;

/**
 * Action factory for the Contract Manager module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ContractManagerActionFactory {
    /**
     * Singleton for the create contract action
     */
    private static CreateContractAction createContractAction;
    /**
     * Singleton for the create contract pool action
     */
    private static CreateContractPoolAction createContractPoolAction;
    /**
     * Singleton for the delete a contract action
     */
    private static DeleteContractAction deleteContractAction;
    /**
     * Singleton for the delete a contract pool action
     */
    private static DeleteContractPoolAction deleteContractPoolAction;
    
    
    public static CreateContractAction getCreateContractAction() {
        if (createContractAction == null)
            createContractAction = new CreateContractAction();
        
        return createContractAction;
    }
    
    public static CreateContractPoolAction getCreateContractPoolAction() {
        if (createContractPoolAction == null)
            createContractPoolAction = new CreateContractPoolAction();
        
        return createContractPoolAction;
    }
    
    public static DeleteContractAction getDeleteContractAction() {
        if (deleteContractAction == null)
            deleteContractAction = new DeleteContractAction();
        
        return deleteContractAction;
    }
    
    public static DeleteContractPoolAction getDeleteContractPoolAction() {
        if (deleteContractPoolAction == null)
            deleteContractPoolAction = new DeleteContractPoolAction();
        
        return deleteContractPoolAction;
    }
}
