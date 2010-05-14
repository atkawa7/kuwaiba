/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.multiple;

import core.annotations.Administrative;
import entity.core.AdministrativeItem;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 * Represents a generic list type attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Administrative
@Entity
public abstract class GenericObjectList extends AdministrativeItem implements Serializable {
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
