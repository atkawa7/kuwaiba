/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
    private String displayName;
    @Column(length=500)
    private String description;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public PackageMetadata() {
    }

    public PackageMetadata(String _name,String _displayName, String _description){
        this.name = _name;
        this.displayName = _displayName;
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
