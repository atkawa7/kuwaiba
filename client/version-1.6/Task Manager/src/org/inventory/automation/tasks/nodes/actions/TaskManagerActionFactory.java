/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.automation.tasks.nodes.actions;

import javax.swing.AbstractAction;

/**
 * Share singletons of all actions applicable to the nodes in this module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TaskManagerActionFactory {
    private static ExecuteTaskAction executeTaskAction;
    private static CreateTaskAction createTaskAction;
    private static DeleteTaskAction deleteTaskAction;
    private static SubscribeUserAction subscribeUserAction;
    private static UnsubscribeUserAction unsubscribeUserAction;
    private static AddParameterToTaskAction addParameterToTaskAction;
    private static RemoveParameterFromTaskAction removeParameterFromTaskAction;
    
    public static AbstractAction createExecuteTaskAction() {
        if (executeTaskAction == null)
            executeTaskAction = new ExecuteTaskAction();
        return executeTaskAction;
    }
    
    public static AbstractAction createCreateTaskAction() {
        if (createTaskAction == null)
            createTaskAction = new CreateTaskAction();
        return createTaskAction;
    }
    
    public static AbstractAction createDeleteTaskAction() {
        if (deleteTaskAction == null)
            deleteTaskAction = new DeleteTaskAction();
        return deleteTaskAction;
    }
    
    public static AbstractAction createSubscribeUserAction() {
        if (subscribeUserAction == null)
            subscribeUserAction = new SubscribeUserAction();
        return subscribeUserAction;
    }
    
    public static AbstractAction createUnsubscribeUserAction() {
        if (unsubscribeUserAction == null)
            unsubscribeUserAction = new UnsubscribeUserAction();
        return unsubscribeUserAction;
    }
    
    public static AbstractAction createAddParameterToTaskActionAction() {
        if (addParameterToTaskAction == null)
            addParameterToTaskAction = new AddParameterToTaskAction();
        return addParameterToTaskAction;
    }
    
    public static AbstractAction createRemoveParameterFromTaskActionAction() {
        if (removeParameterFromTaskAction == null)
            removeParameterFromTaskAction = new RemoveParameterFromTaskAction();
        return removeParameterFromTaskAction;
    }
}
