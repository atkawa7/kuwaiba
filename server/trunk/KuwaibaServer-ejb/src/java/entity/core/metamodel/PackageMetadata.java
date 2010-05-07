/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.core.metamodel;

import core.annotations.Metadata;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

/**
 * Esta clase representa un paquete, que desde el punto de vista de la aplicación
 * vendrían a ser como categorías
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Metadata //Anotación personalizada para marcarla como que no se debe pasar a los
          //clientes para que administren su meta, ya que ella es una clase de utilidad
@NamedQuery(name="flushPackageMetadata", query="DELETE FROM PackageMetadata x")
public class PackageMetadata implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(nullable=false,unique=true,updatable=false)
    private String name;
    @Column(length=500)
    protected String description;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public PackageMetadata() {
    }

    public PackageMetadata(String _name, String _description){
        this.name = _name;
        this.description = _description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(object instanceof PackageMetadata)) {
            return false;
        }
        PackageMetadata other = (PackageMetadata) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.core.metamodel.PackageMetadata[id=" + id + "]";
    }

}
