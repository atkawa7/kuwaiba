/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.kuwaiba.entity.multiple;

import org.kuwaiba.core.exceptions.InvalidArgumentException;
import org.kuwaiba.entity.core.ApplicationObject;
import java.util.logging.Level;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Represents a generic list type attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericObjectList extends ApplicationObject{
    protected String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * This method
     * @param _value
     * @return
     */
    public static Long valueOf(Object _value) throws InvalidArgumentException{
        if (_value == null) return null;
        else {
            if (_value instanceof Long)
                return (Long)_value;
            else
                throw new InvalidArgumentException("The object provided is not a reference to an list type item: "+ _value,Level.WARNING);
        }
    }
}
