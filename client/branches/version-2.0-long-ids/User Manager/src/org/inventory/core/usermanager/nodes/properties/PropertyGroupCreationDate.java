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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.inventory.communications.core.LocalUserGroupObject;
import org.openide.nodes.PropertySupport;

/**
 * The group creation date property
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertyGroupCreationDate extends PropertySupport.ReadOnly<String>{
    private LocalUserGroupObject group;
    private DateFormat formatter;
    
    public PropertyGroupCreationDate(LocalUserGroupObject group) {
        super("creationDate", String.class, "Creation Date", "The group's creation date");
        this.group = group;
        this.formatter = new SimpleDateFormat("MMMM dd, yyyy");
    }

    @Override
    public String getValue() throws IllegalAccessException, InvocationTargetException {
        return formatter.format(group.getCreationDate());
    }
}
