
package entity.core.metamodel;

import core.annotations.Metadata;
import entity.multiple.GenericObjectList;
import entity.relations.GenericRelation;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.metamodel.Attribute;

/**
 * Representa los atributos de cada clase, sirve para propósitos de documentación
 * TODO: Ver cómo hacer para que no se repitan atributos o al menos para reusar ya existentes (habrán muchos "name" que signifiquen lo mismo)
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Metadata //Anotación personalizada para marcarla como que no se debe pasar a los
          //clientes para que administren su meta, ya que ella es una clase de utilidad
@NamedQuery(name="flushAttributeMetadata", query="DELETE FROM AttributeMetadata x")
public class AttributeMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable=false,updatable=false)
    private String name="";
    @Column(nullable=false)
    private String type="String";
    private String displayName="";
    @Column(nullable=false)
    private Boolean isAdministrative=false; //Señala si el atributo es administrativo (cosas como id o relacionadas de alguna forma con el despliegue)
    @Column(nullable=false,updatable=false)
    private Boolean isMultiple = false;    //Señala si el atributo es un tipo básico o una enumeración/relación
    @Column(nullable=false)
    private Boolean isVisible=true; //Señala si el atributo se muestra en la interfaz
    private String description;

    public AttributeMetadata(){} //Requerido
    public AttributeMetadata(String _name, String _type, String _displayName,
            Boolean _isAdministrative, Boolean _isVisible, Boolean _isMultiple, String _description){
        this.name = _name;
        this.type= _type;
        this.displayName = _displayName;
        this.isAdministrative = _isAdministrative;
        this.isVisible = _isVisible;
        //De debería quitar y sacar esa info de la clase?
        this.isMultiple = _isMultiple;
        this.description = _description;
    }

    public AttributeMetadata(Attribute att){
        this.name = att.getName();
        this.type= att.getJavaType().getSimpleName();
        //TODO:Buscar una forma de buscar si una clase es subclase de otra
        //a través de varios niveles, esta comparación sólo busca en la superclase inmediata
        if (att.getJavaType().getSuperclass().equals(GenericRelation.class) ||
                att.getJavaType().getSuperclass().equals(GenericObjectList.class))
                this.isMultiple = true;
        this.description = "El atributo "+this.name;
    }

    public Boolean isAdministrative() {
        return isAdministrative;
    }

    public void setIsAdministrative(Boolean isAdministrative) {
        this.isAdministrative = isAdministrative;
    }

    public Boolean IsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isMultiple() {
        return isMultiple;
    }

    public void setIsMultiple(Boolean isMultiple) {
        this.isMultiple = isMultiple;
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
        if (!(object instanceof AttributeMetadata)) {
            return false;
        }
        AttributeMetadata other = (AttributeMetadata) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.core.metadata.AttributeRegistry[id=" + id + "]";
    }

}
