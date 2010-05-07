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
public abstract class GenericFacility extends RootObject implements Serializable {
    protected String position; //Posición geográfica (Coordenadas)

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    
    @Override
    public String toString() {
        return "entity.location.GenericFacility[id=" + id + "]";
    }

}
