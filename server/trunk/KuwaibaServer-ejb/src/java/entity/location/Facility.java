/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.location;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Facility extends GenericLocation implements Serializable {

    @Override
    public String toString() {
        return "entity.location.Facility[id=" + id + "]";
    }

}
