package entity.equipment.physicallayer.parts.ports;

import entity.multiple.types.parts.CommunicationsPortType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Represents a port used for communication equipment to send/receive data
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericCommunicationsPort extends GenericPort implements Serializable {
    //private boolean vendor; //Reuse the field as private to hide it. Uses a boolean to save diskspace
    //private boolean conditions; //same here

    @ManyToOne
    protected CommunicationsPortType connector; //RJ-45, RJ-11, FC/PC, etc

    public CommunicationsPortType getConnector() {
        return connector;
    }

    public void setConnector(CommunicationsPortType connector) {
        this.connector = connector;
    }
}
