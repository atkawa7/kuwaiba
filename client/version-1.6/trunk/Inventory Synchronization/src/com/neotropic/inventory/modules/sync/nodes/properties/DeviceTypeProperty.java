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
package com.neotropic.inventory.modules.sync.nodes.properties;

import com.neotropic.inventory.modules.sync.nodes.SyncConfigurationNode;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeviceTypeProperty extends PropertySupport.ReadWrite<LocalObjectLight> {
    private final SyncConfigurationNode syncConfigNode;
    private LocalObjectLight value;
    private final PropertySupport propertyDeviceId;

    public DeviceTypeProperty(SyncConfigurationNode syncConfigNode, LocalObjectLight value, PropertySupport propertyDeviceId) {
        super("device", LocalObjectLight.class, "device", "device"); //NOI18N
        this.syncConfigNode = syncConfigNode;
        this.value = value;
        this.propertyDeviceId = propertyDeviceId;
    }

    @Override
    public LocalObjectLight getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }

    @Override
    public void setValue(LocalObjectLight val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.value = val;
        propertyDeviceId.setValue(String.valueOf(val.getOid()));
        
        if (((LocalObjectLight) val).getOid() != -1) {
            HashMap<String, String> parameters = new HashMap();

            LocalSyncDataSourceConfiguration localSyncDataSrcConfig = syncConfigNode.getLookup().lookup(LocalSyncDataSourceConfiguration.class);

            parameters.put("deviceId", Long.toString(((LocalObjectLight) val).getOid()));
            parameters.put("deviceClass", ((LocalObjectLight) val).getClassName());

            if (!CommunicationsStub.getInstance().updateSyncDataSourceConfiguration(localSyncDataSrcConfig.getId(), parameters))
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public PropertyEditor getPropertyEditor() {
        return new DevicePropertyEditor();
    }    
}
