package entity.location;

import entity.core.RootObject;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class City extends RootObject implements Serializable {
    @Override
    public String toString() {
        return "entity.location.City[id=" + id + "]";
    }

}
