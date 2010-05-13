package entity.equipment.physicallayer.parts;

import entity.core.ConfigurationItem;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 * Represents a part of a equipment
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPart extends ConfigurationItem implements Serializable{

}
