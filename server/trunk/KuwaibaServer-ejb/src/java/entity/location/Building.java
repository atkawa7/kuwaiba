package entity.location;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 * Represents a simple building
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Building extends GenericLocation implements Serializable {
    protected String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
