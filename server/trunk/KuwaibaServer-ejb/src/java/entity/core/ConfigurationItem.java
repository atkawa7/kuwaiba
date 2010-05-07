/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.core;

import entity.multiple.companies.Vendor;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
//import javax.persistence.Table;
//import javax.persistence.Column;


/**
 *
 * @author dib
 */
@Entity
//@Table (name="Nombre_de_tabla")
public abstract class ConfigurationItem extends RootObject implements Serializable  { //El problema con colocarlo a heredar de root object es que no crea una tabla para ella sola, sino que crea una llamada rootobject que usa los campos personalizados
    //@Column(name="serialNumber")
    protected String serialNumber;
    @OneToOne
    protected Vendor vendor;

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getSerialNumber(){
        return this.serialNumber;
    }

    public void setSerialNumber(String serialNumber){
            this.serialNumber =serialNumber;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ConfigurationItem)) {
            return false;
        }
        ConfigurationItem other = (ConfigurationItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.ConfigurationItem[id=" + id + "]";
    }

}
