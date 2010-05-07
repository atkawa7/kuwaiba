/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.equipment.applicationlayer;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Server extends GenericApplicationElement implements Serializable {

    @Override
    public String toString() {
        return "entity.equipment.applicationlayer.Server[id=" + id + "]";
    }

}
