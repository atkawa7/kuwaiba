/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.core.config.validators.nodes.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.core.LocalValidatorDefinition;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.PropertySupport;

/**
 * A property of a validator definition node
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ValidatorDefinitionNativeTypeProperty extends PropertySupport.Reflection {
    /**
     * Main listener for changes in this property
     */
    private VetoableChangeListener listenerNode;
    
    public ValidatorDefinitionNativeTypeProperty(LocalValidatorDefinition configVariable, 
            Class type, String propertyName, VetoableChangeListener listener) throws NoSuchMethodException{
        super(configVariable, type, propertyName);
        listenerNode = listener;
    }

    @Override
    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try {
            listenerNode.vetoableChange(new PropertyChangeEvent(instance, getName(), getValue(), val));
            super.setValue(val);
        } catch(PropertyVetoException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getLocalizedMessage());
        }
    }
}
