package entity.equipment.physicallayer.parts.ports;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 * Represents a port used for communication equipment to send/receive data
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericCommunicationsPort extends GenericPort implements Serializable {
}
