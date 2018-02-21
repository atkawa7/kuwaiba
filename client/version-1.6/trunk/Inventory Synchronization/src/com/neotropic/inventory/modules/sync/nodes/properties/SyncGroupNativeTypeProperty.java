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
package com.neotropic.inventory.modules.sync.nodes.properties;

import com.neotropic.inventory.modules.sync.nodes.SyncGroupNode;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SyncGroupNativeTypeProperty extends PropertySupport.ReadWrite {
    private final SyncGroupNode syncGroupNode;
    private Object value;
    
    public SyncGroupNativeTypeProperty(String name, Class type, String displayName, String shortDescription, SyncGroupNode syncGroupNode, Object value) {
        super(name, type, displayName, shortDescription);
        this.syncGroupNode = syncGroupNode;
        this.value = value;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        HashMap<String, String> properties = new HashMap();
        properties.put(getName(), (String) val);
        
        LocalSyncGroup localSyncGroup = syncGroupNode.getLookup().lookup(LocalSyncGroup.class);
        
        if (CommunicationsStub.getInstance().updateSyncGroup(localSyncGroup.getId(), properties)) {
            if (Constants.PROPERTY_NAME.equals(getName())) {
                String oldValue = localSyncGroup.getName();
                localSyncGroup.setName((String) val);
                syncGroupNode.propertyChange(new PropertyChangeEvent(localSyncGroup, Constants.PROPERTY_NAME, oldValue, val));
                
            }
            value = val;
        } else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }
    
    @Override
    public boolean canWrite() {
        return !getName().equals("syncProvider");
    }
}