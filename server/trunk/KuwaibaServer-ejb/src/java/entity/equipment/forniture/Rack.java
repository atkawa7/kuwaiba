/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.equipment.forniture;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Rack extends GenericForniture implements Serializable {
    @Column(length=3)
    protected Integer rackUnits;
}
