/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.multiple.systems;

import entity.multiple.GenericObjectList;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class OperatingSystem extends GenericObjectList implements Serializable {

    @Temporal(TemporalType.DATE)
    protected Date releaseDate;

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }


    @Override
    public String toString() {
        return "entity.multichoice.systems.OperatingSystem[id=" + id + "]";
    }

}
