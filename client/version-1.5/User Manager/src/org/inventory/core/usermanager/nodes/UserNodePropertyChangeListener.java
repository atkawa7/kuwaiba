/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.core.usermanager.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.LocalUserObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Listens for property changes in users and sends them to the server for processing
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UserNodePropertyChangeListener implements VetoableChangeListener {
    private static UserNodePropertyChangeListener instance; //This class is a singleton
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    private UserNodePropertyChangeListener() { }

    public static UserNodePropertyChangeListener getInstance() {
        if (instance == null)
            instance = new UserNodePropertyChangeListener();
        return instance;
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        
        if (evt.getPropertyName().equals(LocalUserObject.PROPERTY_PRIVILEGES)) {
            if (evt.getNewValue() == null) { //Remove privilege
                if (!com.removePrivilegeFromUser(((LocalUserObjectLight)evt.getSource()).getId(), ((LocalPrivilege)evt.getOldValue()).getFeatureToken())) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(com.getError(), evt);
                } else
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Privilege removed successfully");
            } else {
                if (!com.setPrivilegeToUser(((LocalUserObjectLight)evt.getSource()).getId(), ((LocalPrivilege)evt.getNewValue()).getFeatureToken(), 
                        ((LocalPrivilege)evt.getNewValue()).getAccessLevel())) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(com.getError(), evt);
                } else
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Privilege updated successfully");
            }
        }
        else {
            if (!com.setUserProperties(((LocalUserObjectLight)evt.getSource()).getId(), 
                    LocalUserObjectLight.PROPERTY_USER_NAME.equals(evt.getPropertyName()) ? (String)evt.getNewValue() : null, 
                    LocalUserObjectLight.PROPERTY_PASSWORD.equals(evt.getPropertyName()) ? (String)evt.getNewValue() : null,
                    LocalUserObjectLight.PROPERTY_FIRST_NAME.equals(evt.getPropertyName()) ? (String)evt.getNewValue() : null,
                    LocalUserObjectLight.PROPERTY_LAST_NAME.equals(evt.getPropertyName()) ? (String)evt.getNewValue() : null,
                    LocalUserObjectLight.PROPERTY_ENABLED.equals(evt.getPropertyName()) ?  ((boolean)evt.getNewValue() ? 1 : 0) : -1,
                    LocalUserObjectLight.PROPERTY_TYPE.equals(evt.getPropertyName()) ? (int)evt.getNewValue() : -1)) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                throw new PropertyVetoException(com.getError(), evt);
            }
        }
    }
}
