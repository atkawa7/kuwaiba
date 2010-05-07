/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.location;

import entity.core.RootObject;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
//El nombre de clase State podía tener problemas, al parecer
public class StateObject extends RootObject implements Serializable {

    @Override
    public String toString() {
        return "entity.location.State[id=" + id + "]";
    }

}
