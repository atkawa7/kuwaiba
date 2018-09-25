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
package org.kuwaiba.core.variables.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.core.LocalConfigurationVariable;
import org.openide.nodes.PropertySupport;

/**
 * A property of a configuration variable node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ConfigurationVariableNativeTypeProperty extends PropertySupport.Reflection {
    
    public ConfigurationVariableNativeTypeProperty(LocalConfigurationVariable configVariable, Class type, String propertyName) throws NoSuchMethodException{
        super(configVariable, type, propertyName);
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return ((LocalConfigurationVariable)instance).isMasked() ? "This property is masked" : super.getValue();
    }

    @Override
    public boolean canRead(){
        return !((LocalConfigurationVariable)instance).isMasked();
    }
}
