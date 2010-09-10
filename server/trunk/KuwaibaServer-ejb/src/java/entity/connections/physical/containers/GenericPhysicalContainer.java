/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.connections.physical.containers;

import core.interfaces.PhysicalContainer;
import core.interfaces.PhysicalNode;
import entity.connections.GenericConnection;
import entity.multiple.types.containers.PhysicalContainerType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * This class represents
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPhysicalContainer extends GenericConnection implements Serializable,PhysicalContainer {

    protected PhysicalNode nodeA;
    protected PhysicalNode nodeB;
    @OneToOne
    protected PhysicalContainerType type;

    @Override
    public PhysicalNode getNodeA() {
        return nodeA;
    }

    @Override
    public PhysicalNode getNodeB() {
        return nodeB;
    }

    @Override
    public PhysicalContainerType getType() {
        return type;
    }

}
