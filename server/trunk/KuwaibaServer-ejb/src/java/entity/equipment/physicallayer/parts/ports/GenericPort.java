package entity.equipment.physicallayer.parts.ports;

import entity.equipment.physicallayer.parts.GenericPart;
import entity.multiple.equipment.parts.PlugType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Represents a generic Port
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPort extends GenericPart implements Serializable {
    @ManyToOne
    private PlugType connector; //RJ-45, RJ-11, FC/PC, etc
}
