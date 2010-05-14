package entity.location;

import entity.core.RootObject;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericLocation extends RootObject implements Serializable {
    protected String position; //Geo position (coordinates)

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
