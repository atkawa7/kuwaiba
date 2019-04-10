/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package org.inventory.core.templates.nodes.actions;

import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * Factory for all actions to be used by nodes in this module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TemplateActionsFactory {
    private static CreateTemplateAction createTemplateAction;
    private static CreateTemplateElementAction createTemplateElementAction;
    private static CreateMultipleTemplateElementAction createMultipleTemplateElementAction;
    private static DeleteTemplateElementAction deleteTemplateElementAction;
    private static CreateTemplateElementSpecialAction createTemplateElementSpecialAction;
    private static CreateMultipleSpecialTemplateElementAction createMultipleSpecialTemplateElementAction;
    private static EditLayoutAction editLayoutAction;
    
    public static GenericInventoryAction getCreateTemplateAction() {
        if (createTemplateAction == null)
            createTemplateAction = new CreateTemplateAction();
        return createTemplateAction;
    }
    
    public static GenericInventoryAction getCreateTemplateElementAction() {
        if (createTemplateElementAction == null)
            createTemplateElementAction = new CreateTemplateElementAction();
        return createTemplateElementAction;
    }
    
    public static GenericInventoryAction getCreateMultipleTemplateElementAction() {
        if (createMultipleTemplateElementAction == null)
            createMultipleTemplateElementAction = new CreateMultipleTemplateElementAction();
        return createMultipleTemplateElementAction;            
    }
    
    public static GenericInventoryAction getDeleteTemplateElementAction() {
        if (deleteTemplateElementAction == null)
            deleteTemplateElementAction = DeleteTemplateElementAction.getInstance();
        return deleteTemplateElementAction;
    }
    
    public static GenericInventoryAction getCreateTemplateElementSpecialAction() {
        if (createTemplateElementSpecialAction == null) 
            createTemplateElementSpecialAction = new CreateTemplateElementSpecialAction();
        return createTemplateElementSpecialAction;
    }
    
    public static GenericInventoryAction getCreateMultipleSpecialTemplateElementAction() {
        if (createMultipleSpecialTemplateElementAction == null)
            createMultipleSpecialTemplateElementAction = new CreateMultipleSpecialTemplateElementAction();
        return createMultipleSpecialTemplateElementAction;
    }
            
    public static EditLayoutAction getEditLayoutAction() {
        return editLayoutAction == null ? editLayoutAction = new EditLayoutAction() : editLayoutAction;
    }
}
