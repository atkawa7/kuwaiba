/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.usermanager.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.core.LocalUserGroupObject;
import org.openide.nodes.PropertySupport;

/**
 * The group name property
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertyGroupName extends PropertySupport.ReadWrite<String>{
    private LocalUserGroupObject group;
    public PropertyGroupName(LocalUserGroupObject group) {
        super("name", String.class, "Name", "The group's name. Use only letters, numbers, spaces and dots");
        this.group = group;
    }

    @Override
    public String getValue() throws IllegalAccessException, InvocationTargetException {
        return group.getName();
    }

    @Override
    public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.group.setName(val);
    }

}
