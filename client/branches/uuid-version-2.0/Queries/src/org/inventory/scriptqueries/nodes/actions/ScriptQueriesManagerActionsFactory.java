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
package org.inventory.scriptqueries.nodes.actions;

/**
 * Creates instances of actions to nodes in the script query manager module
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ScriptQueriesManagerActionsFactory {
    private static CreateScriptQueryAction createScriptQueryAction;
    private static DeleteScriptQueryAction deleteScriptQueryAction;
    private static AddParameterToScriptQueryAction addParameterToScriptQueryAction;
    private static RemoveParameterFromScriptQueryAction removeParameterFromScriptQueryAction;
    private static ExecuteScriptQueryAction executeScriptQueryAction;
    private static ExecuteScriptQueryCollectionAction executeScriptQueryCollectionAction;
    
    public static CreateScriptQueryAction getCreateScriptQueryAction() {
        return createScriptQueryAction == null ? createScriptQueryAction = new CreateScriptQueryAction() : createScriptQueryAction;
    }
    
    public static DeleteScriptQueryAction getDeleteScriptQueryAction() {
        return deleteScriptQueryAction == null ? deleteScriptQueryAction = new DeleteScriptQueryAction() : deleteScriptQueryAction;
    }
    
    public static AddParameterToScriptQueryAction getAddParameterToScriptQueryAction() {
        return addParameterToScriptQueryAction == null ? addParameterToScriptQueryAction = new AddParameterToScriptQueryAction() : addParameterToScriptQueryAction;
    }
    
    public static RemoveParameterFromScriptQueryAction getRemoveParameterFromScriptQueryAction() {
        return removeParameterFromScriptQueryAction == null ? removeParameterFromScriptQueryAction = new RemoveParameterFromScriptQueryAction() : removeParameterFromScriptQueryAction;
    }
    
    public static ExecuteScriptQueryAction getExecuteScriptQueryAction() {
        return executeScriptQueryAction == null ? executeScriptQueryAction = new ExecuteScriptQueryAction() : executeScriptQueryAction;
    }
    
    public static ExecuteScriptQueryCollectionAction getExecuteScriptQueryCollectionAction() {
        return executeScriptQueryCollectionAction == null ? executeScriptQueryCollectionAction = new ExecuteScriptQueryCollectionAction() : executeScriptQueryCollectionAction;
    }
}
