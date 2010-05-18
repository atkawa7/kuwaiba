/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.equipment.networklayer;

import entity.core.ConfigurationItem;
import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author dib
 */
@Entity
public abstract class GenericNetworkElement extends ConfigurationItem implements Serializable {
    private static final long serialVersionUID = 1L;

}
