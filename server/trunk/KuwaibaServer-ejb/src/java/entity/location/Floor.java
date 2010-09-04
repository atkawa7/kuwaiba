/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.location;

import core.interfaces.PhysicalContainer;
import core.interfaces.PhysicalNode;
import entity.core.RootObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Entity;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Floor extends RootObject implements Serializable,PhysicalNode {

    protected List<PhysicalContainer> containers;

    @Override
    public List<PhysicalContainer> getConnectedPhysicalContainers() {
        return containers;
    }

    @Override
    public void addPhysicalContainers(PhysicalContainer[] _containers) {
        if (containers == null)
            containers = new ArrayList<PhysicalContainer>();
        containers.addAll(Arrays.asList(_containers));
    }
}
