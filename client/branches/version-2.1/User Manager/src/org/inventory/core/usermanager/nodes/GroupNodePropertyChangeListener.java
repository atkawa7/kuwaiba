/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Listens for property changes in groups and sends them to the server for processing
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GroupNodePropertyChangeListener implements VetoableChangeListener {
    private static GroupNodePropertyChangeListener instance; //This class is a singleton
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    private GroupNodePropertyChangeListener() { }

    public static GroupNodePropertyChangeListener getInstance() {
        if (instance == null)
            instance = new GroupNodePropertyChangeListener();
        return instance;
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (evt.getPropertyName().equals(LocalUserGroupObject.PROPERTY_PRIVILEGES)) {
            if (evt.getNewValue() == null) { //Remove privilege
                if (!com.removePrivilegeFromGroup(((LocalUserGroupObject)evt.getSource()).getId(), ((LocalPrivilege)evt.getOldValue()).getFeatureToken())) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(com.getError(), evt);
                } else
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Privilege removed successfully");
            } else {
                if (!com.setPrivilegeToGroup(((LocalUserGroupObject)evt.getSource()).getId(), ((LocalPrivilege)evt.getNewValue()).getFeatureToken(), 
                        ((LocalPrivilege)evt.getNewValue()).getAccessLevel())) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    throw new PropertyVetoException(com.getError(), evt);
                } else
                    NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Privilege updated successfully");
            }
        } else {
            if (!com.setGroupProperties(((LocalUserGroupObject)evt.getSource()).getId(), 
                    LocalUserGroupObject.PROPERTY_NAME.equals(evt.getPropertyName()) ? (String)evt.getNewValue() : null , 
                    LocalUserGroupObject.PROPERTY_DESCRIPTION.equals(evt.getPropertyName()) ? (String)evt.getNewValue() : null)) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                throw new PropertyVetoException(com.getError(), evt);
            }
        }
    }
}
