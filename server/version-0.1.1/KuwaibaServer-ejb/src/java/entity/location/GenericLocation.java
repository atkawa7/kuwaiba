package entity.location;

import entity.core.RootObject;
import entity.multiple.states.StructuralState;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericLocation extends RootObject implements Serializable {
    protected String position; //Geo position (coordinates)
    @ManyToOne
    protected StructuralState state;


    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public StructuralState getState() {
        return state;
    }

    public void setState(StructuralState state) {
        this.state = state;
    }

}
