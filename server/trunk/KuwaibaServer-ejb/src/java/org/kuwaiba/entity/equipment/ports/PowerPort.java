package org.kuwaiba.entity.equipment.ports;

import org.kuwaiba.entity.multiple.types.parts.PowerPortType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Used to represent a port used to be connected to a power source
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class PowerPort extends GenericPort implements Serializable {
    @ManyToOne
    protected PowerPortType connector; //RJ-45, RJ-11, FC/PC, etc

    public PowerPortType getConnector() {
        return connector;
    }

    public void setConnector(PowerPortType connector) {
        this.connector = connector;
    }
}
