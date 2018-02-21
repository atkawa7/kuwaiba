/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.core.LocalUserObject;
import org.openide.nodes.PropertySupport;

/**
 * The user first name property
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PropertyUserFirstName extends PropertySupport.ReadWrite<String>{
    private LocalUserObject user;
    public PropertyUserFirstName(LocalUserObject user) {
        super("firstName", String.class, "First Name", "The user's first name");
        this.user = user;
    }

    @Override
    public String getValue() throws IllegalAccessException, InvocationTargetException {
        return user.getFirstName();
    }

    @Override
    public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.user.setFirstName(val);
    }

}
