/*
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
 *  under the License.
 */
package org.inventory.automation.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalTaskNotificationDescriptor;
import org.inventory.communications.core.LocalTaskScheduleDescriptor;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Service class for this module. It also implements the property change listener for the TaskNodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TaskManagerService implements VetoableChangeListener {
    private static TaskManagerService instance;
    private TaskManagerService() {}
    
    public static TaskManagerService getInstance() {
        if (instance == null)
            instance = new TaskManagerService();
        return instance;
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        LocalTask theTask = (LocalTask)evt.getSource();
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        switch (evt.getPropertyName()) {
            case Constants.PROPERTY_NAME:
            case Constants.PROPERTY_DESCRIPTION:
            case Constants.PROPERTY_ENABLED:
            case Constants.PROPERTY_SCRIPT:
                if (!com.updateTaskProperties(theTask.getId(), evt.getPropertyName(), (String)evt.getNewValue())) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway
                }
                break;
            case Constants.PROPERTY_START_TIME:
                LocalTaskScheduleDescriptor schedule = new LocalTaskScheduleDescriptor((long)evt.getNewValue(), 
                        theTask.getEveryXMinutes(), theTask.getExecutionType());
                if (!com.updateTaskSchedule(theTask.getId(), schedule)) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway
                }
                break;
            case Constants.PROPERTY_EVERY_X_MINUTES:
                schedule = new LocalTaskScheduleDescriptor(theTask.getStartTime(), 
                        (int)evt.getNewValue(), theTask.getExecutionType());
                if (!com.updateTaskSchedule(theTask.getId(), schedule)) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway
                }
                break;
            case Constants.PROPERTY_EXECUTION_TYPE:
                schedule = new LocalTaskScheduleDescriptor(theTask.getStartTime(), 
                        theTask.getEveryXMinutes(), (int)evt.getNewValue());
                if (!com.updateTaskSchedule(theTask.getId(), schedule)) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway
                }
                break;
            case Constants.PROPERTY_NOTIFICATION_TYPE:
                LocalTaskNotificationDescriptor notification = new LocalTaskNotificationDescriptor(theTask.getEmail(), 
                        (int)evt.getNewValue());
                if (!com.updateTaskNotificationType(theTask.getId(), notification)) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway
                }
                break;
            case Constants.PROPERTY_EMAIL:
                notification = new LocalTaskNotificationDescriptor((String)evt.getNewValue(), 
                        theTask.getNotificationType());
                if (!com.updateTaskNotificationType(theTask.getId(), notification)) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway
                }
                break;
            default: //The rest are task parameters
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put(evt.getPropertyName(), (String)evt.getNewValue());
                if (!com.updateTaskParameters(theTask.getId(), parameters)) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(null, null); //This exception goes to /dev/null anyway
                }
        }           
    }
    
}
