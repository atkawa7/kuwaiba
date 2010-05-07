/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.equipment.forniture;

import entity.core.RootObject;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericForniture extends RootObject implements Serializable {

    @Override
    public String toString() {
        return "entity.equipment.forniture.GenericForniture[id=" + id + "]";
    }

}
