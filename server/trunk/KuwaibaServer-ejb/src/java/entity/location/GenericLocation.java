package entity.location;

import core.interfaces.PhysicalContainer;
import core.interfaces.PhysicalNode;
import entity.core.RootObject;
import entity.multiple.states.StructuralState;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericLocation extends RootObject implements Serializable, PhysicalNode {

    /**
     * This one has all pipes and ducts connected to the node
     */
    protected List<PhysicalContainer> containers;
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

    @Override
    public void addPhysicalContainers(PhysicalContainer[] _containers) {
        if (containers == null)
            containers = new ArrayList<PhysicalContainer>();
        containers.addAll(Arrays.asList(_containers));
    }

    @Override
    public List<PhysicalContainer> getConnectedPhysicalContainers() {
        return containers;
    }
}
