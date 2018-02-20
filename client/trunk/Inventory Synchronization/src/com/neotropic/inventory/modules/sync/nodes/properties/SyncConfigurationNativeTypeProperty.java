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

import com.neotropic.inventory.modules.sync.nodes.SyncConfigurationNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.PropertySupport;

/**
 * Properties to Sync Configuration Nodes
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SyncConfigurationNativeTypeProperty extends PropertySupport.ReadWrite {
    private final SyncConfigurationNode syncConfigNode;
    private Object value;
    
    public SyncConfigurationNativeTypeProperty(String name, Class type, String displayName, String shortDescription, SyncConfigurationNode syncConfigNode, Object value) {
        super(name, type, displayName, shortDescription);
        this.syncConfigNode = syncConfigNode;
        this.value = value;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        if (Constants.PROPERTY_AUTH_PASS.equals(getName()) || 
            Constants.PROPERTY_PRIVACY_PASS.equals(getName())
            ) {
            
            if (value != null && value instanceof String && !((String) value).isEmpty())
                return "********"; //NOI18N
        }
        return value;
    }

    @Override
    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        HashMap<String, String> parameters = new HashMap();        
        parameters.put(getName(), (String) val);
        
        LocalSyncDataSourceConfiguration localSyncDataSrcConfig = syncConfigNode.getLookup().lookup(LocalSyncDataSourceConfiguration.class);
        
        if (CommunicationsStub.getInstance().updateSyncDataSourceConfiguration(localSyncDataSrcConfig.getId(), parameters)) {
            if (Constants.PROPERTY_NAME.equals(getName())) {
                String oldValue = localSyncDataSrcConfig.getName();
                localSyncDataSrcConfig.setName((String) val);
                syncConfigNode.propertyChange(new PropertyChangeEvent(localSyncDataSrcConfig, Constants.PROPERTY_NAME, oldValue, val));
            }
            localSyncDataSrcConfig.getParameters().put(getName(), (String) val);
            value = val;
        } else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }
    
    @Override
    public boolean canWrite() {
        return !getName().equals("deviceName") && !getName().equals("deviceId") && !getName().equals("deviceClass");
    }
    
    @Override
    public PropertyEditor getPropertyEditor() {
        if (Constants.PROPERTY_VERSION.equals(getName()) || 
            Constants.PROPERTY_AUTH_PROTOCOL.equals(getName()) || 
            Constants.PROPERTY_SECURITY_LEVEL.equals(getName()) || 
            Constants.PROPERTY_PRIVACY_PROTOCOL.equals(getName())) {
            
            return new SyncDataSourceConfigPropertyEditor(getName());
        }
        return super.getPropertyEditor();
    }
}