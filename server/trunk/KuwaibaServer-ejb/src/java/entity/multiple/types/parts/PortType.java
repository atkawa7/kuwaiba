package entity.multiple.types.parts;

import entity.multiple.GenericObjectList;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 * Represents a connector type, like RJ45, RJ11, FC/PC, etc
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class PortType extends GenericObjectList implements Serializable {

}
