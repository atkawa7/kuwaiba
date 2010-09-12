/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.location;

import core.interfaces.PhysicalNode;
import entity.connections.physical.containers.GenericPhysicalContainer;
import entity.core.RootObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Floor extends RootObject implements Serializable,PhysicalNode {
    @ManyToMany
    protected List<GenericPhysicalContainer> containers;

    @Override
    public List<GenericPhysicalContainer> getConnectedPhysicalContainers() {
        return containers;
    }

    @Override
    public void addPhysicalContainers(GenericPhysicalContainer[] _containers) {
        if (containers == null)
            containers = new ArrayList<GenericPhysicalContainer>();
        containers.addAll(Arrays.asList(_containers));
    }
}
