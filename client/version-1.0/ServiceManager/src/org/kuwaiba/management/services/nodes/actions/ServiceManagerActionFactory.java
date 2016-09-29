/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.management.services.nodes.actions;

/**
 * Action factory for the Service Manager module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServiceManagerActionFactory {
    /**
     * Singleton for the create service action
     */
    private static CreateServiceAction createServiceAction;
    /**
     * Singleton for the create service pool action
     */
    private static CreateServicePoolAction createServicePoolAction;
    /**
     * Singleton for the create service action
     */
    private static CreateCustomerAction createCustomerAction;
    /**
     * Singleton for the create service pool action
     */
    private static CreateCustomerPoolAction createCustomerPoolAction;
    /**
     * Singleton for the delete service action
     */
    private static DeleteServiceAction deleteServiceAction;
    /**
     * Singleton for the delete customer action
     */
    private static DeleteCustomerAction deleteCustomerAction;
    /**
     * Singleton for the delete service pool action
     */
    private static DeleteServicePoolAction deleteServicePoolAction;
    /**
     * Singleton for the delete customer action
     */
    private static DeleteCustomerPoolAction deleteCustomerPoolAction;
    
    public static CreateServiceAction getCreateServiceAction() {
        if (createServiceAction == null)
            createServiceAction = new CreateServiceAction();
        
        return createServiceAction;
    }
    
    public static CreateServicePoolAction getCreateServicePoolAction() {
        if (createServicePoolAction == null)
            createServicePoolAction = new CreateServicePoolAction();
        
        return createServicePoolAction;
    }
    
    public static CreateCustomerAction getCreateCustomerAction() {
        if (createCustomerAction == null)
            createCustomerAction = new CreateCustomerAction();
        
        return createCustomerAction;
    }
    
    public static CreateCustomerPoolAction getCreateCustomerPoolAction() {
        if (createCustomerPoolAction == null)
            createCustomerPoolAction = new CreateCustomerPoolAction();
        
        return createCustomerPoolAction;
    }
    
    public static DeleteServiceAction getDeleteServiceAction() {
        if (deleteServiceAction == null)
            deleteServiceAction = new DeleteServiceAction();
        
        return deleteServiceAction;
    }
    
    public static DeleteCustomerAction getDeleteCustomerAction() {
        if (deleteCustomerAction == null)
            deleteCustomerAction = new DeleteCustomerAction();
        
        return deleteCustomerAction;
    }
    
    public static DeleteServicePoolAction getDeleteServicePoolAction() {
        if (deleteServicePoolAction == null)
            deleteServicePoolAction = new DeleteServicePoolAction();
        
        return deleteServicePoolAction;
    }
    
    public static DeleteCustomerPoolAction getDeleteCustomerPoolAction() {
        if (deleteCustomerPoolAction == null)
            deleteCustomerPoolAction = new DeleteCustomerPoolAction();
        
        return deleteCustomerPoolAction;
    }
}
