/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.multiple.companies;

import entity.multiple.GenericObjectList;
import java.io.Serializable;
import javax.persistence.Entity;


/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Vendor extends GenericObjectList implements Serializable {


    @Override
    public String toString() {
        return "entity.multichoice.companies.Vendor[id=" + id + "]";
    }

}
