/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.location;

import entity.core.RootObject;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Country extends RootObject implements Serializable {
    @Column(length=3)
    protected String acronym;

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }
    
    @Override
    public String toString() {
        return "entity.location.Country[id=" + id + "]";
    }

}
