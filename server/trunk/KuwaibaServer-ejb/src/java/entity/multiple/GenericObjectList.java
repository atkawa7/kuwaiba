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
package entity.multiple;

import core.annotations.Administrative;
import core.annotations.Hidden;
import entity.core.AdministrativeItem;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Represents a generic list type attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Administrative
@Hidden
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericObjectList extends AdministrativeItem{
    protected String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    //Al final, las relaciones se identifican con un id, que es una clave foránea
    //dentro del elemento que los referencia y obviamente es la clave primaria
    //de su tabla
    public static Long valueOf(Object _value){
        if (_value == null) return null;
        else return (Long)_value;
    }

    @Override
    public String toString() {
        return "entity.multichoice.GenericMultichoice[id=" + id + "]";
    }

}
