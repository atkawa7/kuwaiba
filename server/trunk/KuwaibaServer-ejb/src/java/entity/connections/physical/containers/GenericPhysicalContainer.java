/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.connections.physical.containers;

import core.interfaces.PhysicalContainer;
import core.interfaces.PhysicalNode;
import entity.connections.GenericConnection;
import entity.core.ConfigurationItem;
import entity.multiple.types.containers.PhysicalContainerType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * This class represents a generic physical connection container
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPhysicalContainer extends GenericConnection implements Serializable,PhysicalContainer {

    @OneToOne
    protected ConfigurationItem nodeA;
    @OneToOne
    protected ConfigurationItem nodeB;
    @OneToOne
    protected PhysicalContainerType type;

    @Override
    public PhysicalNode getNodeA() {
        return (PhysicalNode) nodeA;
    }

    @Override
    public PhysicalNode getNodeB() {
        return (PhysicalNode) nodeB;
    }

    @Override
    public void connectNodeA(PhysicalNode nodeA) {
        this.nodeA = (ConfigurationItem) nodeA;
    }

    @Override
    public void connectNodeB(PhysicalNode nodeB) {
        this.nodeB = (ConfigurationItem) nodeB;
    }

    @Override
    public PhysicalContainerType getType() {
        return type;
    }

}
