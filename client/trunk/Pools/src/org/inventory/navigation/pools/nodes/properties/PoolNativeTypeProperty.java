/*
 *  Copyright 2010-2019, Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.pools.nodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.pools.nodes.PoolNode;
import org.openide.nodes.PropertySupport.ReadWrite;

/**
 * Provides a valid representation of LocalPools attributes as Properties,
 * as LocalPool is just a proxy and can't be a bean itself
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PoolNativeTypeProperty extends ReadWrite {

    private final PoolNode pool;
    private Object value;
    
    public PoolNativeTypeProperty(String name, Class type, String displayName, 
            String shortDescription, PoolNode pool, Object value) {
        super(name, type, displayName, shortDescription);
        this.pool = pool;
        this.value = value;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        if (value == null && getValueType() == Boolean.class)
            return false;
        return value;
    }

    @Override
    public void setValue(Object value)  {
        boolean flag = false;
        if (this.getName().equals(Constants.PROPERTY_NAME))
            flag = CommunicationsStub.getInstance().setPoolProperties(pool.getPool().getId(), (String)value, null);
        if (this.getName().equals(Constants.PROPERTY_DESCRIPTION))
            flag = CommunicationsStub.getInstance().setPoolProperties(pool.getPool().getId(), null, (String)value);

        if (flag) {
            this.value = value;
            if (this.getName().equals(Constants.PROPERTY_NAME))
                pool.getPool().setName((String) value);
            if (this.getName().equals(Constants.PROPERTY_DESCRIPTION))
                pool.getPool().setDescription((String) value);
        }
        else {
            NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    @Override
    public PropertyEditor getPropertyEditor() {
        return super.getPropertyEditor();
    }
    
    @Override
    public boolean canWrite(){
        return !getName().equals(Constants.PROPERTY_CLASSNAME);
    }
}
