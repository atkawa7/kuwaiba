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
 *  under the License.
 */
package org.inventory.automation.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Service class for this module. It also implements the property change listener for the TaskNodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TaskManagerService implements PropertyChangeListener {
    private static TaskManagerService instance;
    private TaskManagerService() {}
    
    public static TaskManagerService getInstance() {
        if (instance == null)
            instance = new TaskManagerService();
        return instance;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LocalTask theTask = (LocalTask)evt.getSource();
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        switch (evt.getPropertyName()) {
            case Constants.PROPERTY_NAME:
            case Constants.PROPERTY_DESCRIPTION:
            case Constants.PROPERTY_ENABLED:
            case Constants.PROPERTY_SCRIPT:
                if (!com.updateTaskProperties(theTask.getId(), evt.getPropertyName(), (String)evt.getNewValue()))
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                break;
            case Constants.PROPERTY_START_TIME:
            case Constants.PROPERTY_EVERY_X_MINUTES:
            case Constants.PROPERTY_EXECUTION_TYPE:
                break;
            case Constants.PROPERTY_NOTIFICATION_TYPE:
            case Constants.PROPERTY_EMAIL:
                break;
        }           
    }
    
}
