/**
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
package org.inventory.reports.nodes.actions;

import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * Actions factory for this module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ReportActionsFactory {
    private static CreateClassLevelReportAction createClassLevelReportAction;
    private static CreateInventoryLevelReportAction createInventoryLevelReportAction;
    private static DeleteReportAction deleteClassLevelReportAction;
    private static AddParameterToReportAction addParameterToReportAction;
    private static RemoveParameterFromReportAction removeParameterFromReportAction;
    private static ExecuteInventoryLevelReportAction executeInventoryLevelReportAction;
    
    public static GenericInventoryAction getDeleteClassLevelReportAction() {
        if (deleteClassLevelReportAction == null)
            deleteClassLevelReportAction = new DeleteReportAction();
        return deleteClassLevelReportAction;
    }
    
    public static GenericInventoryAction getCreateClassLevelReportAction() {
        if (createClassLevelReportAction == null)
            createClassLevelReportAction = new CreateClassLevelReportAction();
        return createClassLevelReportAction;
    }
    
    public static GenericInventoryAction getCreateInventoryLevelReportAction() {
        if (createInventoryLevelReportAction == null)
            createInventoryLevelReportAction = new CreateInventoryLevelReportAction();
        return createInventoryLevelReportAction;
    }
    
    public static GenericInventoryAction getAddParameterToReportAction() {
        if (addParameterToReportAction == null)
            addParameterToReportAction = new AddParameterToReportAction();
        return addParameterToReportAction;
    }
    
    public static GenericInventoryAction getRemoveParameterFromReportAction() {
        if (removeParameterFromReportAction == null)
            removeParameterFromReportAction = new RemoveParameterFromReportAction();
        return removeParameterFromReportAction;
    }
    
    public static GenericInventoryAction getExecuteInventoryLevelReportAction() {
        if (executeInventoryLevelReportAction == null)
            executeInventoryLevelReportAction = new ExecuteInventoryLevelReportAction();
        return executeInventoryLevelReportAction;
    }
}
