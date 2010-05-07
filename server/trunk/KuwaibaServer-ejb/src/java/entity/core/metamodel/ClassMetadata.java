package entity.core.metamodel;

import core.annotations.Metadata;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * This class holds information about the existing classes
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Metadata //Anotación personalizada para marcarla como que no se debe pasar a los
          //clientes para que administren su meta, ya que ella es una clase de utilidad
@NamedQuery(name="flushClassMetadata", query="DELETE FROM ClassMetadata x")
public class ClassMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable=false,unique=true,updatable=false)
    private String name = "";
    private String displayName;

    @JoinColumn(nullable=false,name="package_id")
    @ManyToOne//(cascade=CascadeType.PERSIST) Si se le pone cascade, si se intenta meter un paquete cuyo nombre
            //(Columna única) ya existe, el man se totea porque no entiende que ignore la adicionada si ya hay uno con ese nombre
    private PackageMetadata packageInfo; //Paquete donde se encuentra. útil para crear luego
                                         //instancias a partir de su nombrte full-qualified usando class.forName
    private String description;
    @Column(nullable=false)
    private Boolean isCustom=false;       //Indica si es una clase del core o si es creada para un cliente en particular
    private byte[] smallIcon;
    private byte[] icon;
    @OneToMany
    @JoinTable(name="ContainerHierarchy") //Este es el nombre que tendrá la tabla que implementa la relación
    private List<ClassMetadata> possibleChildren;

    @OneToMany
    @JoinTable(name="AttributesMap")
    private List<AttributeMetadata> attributes; //Represents the relationship with the attributes metadata information

    @OneToOne
    @JoinColumn(name="parent_id")
    private ClassMetadata parent; //Represents the relation with the parent class
                                  //(actually necessary given the relationship already mapped by the Entity Manager?)
                                  //This attribute should be null for those classes whose parent is different than RootObject or one of its children


    public ClassMetadata() {
    }

    public ClassMetadata(String _name, PackageMetadata _myPackage, String _description,
            Boolean _isCustom, List<ClassMetadata> _children, List <AttributeMetadata> _attributes, ClassMetadata _parent){
        this.name = _name;
        this.packageInfo = _myPackage;
        this.description = _description;
        this.isCustom = _isCustom;
        this.possibleChildren = _children;
        this.attributes = _attributes;
        this.parent = _parent;
    }

    public List<AttributeMetadata> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeMetadata> attributes) {
        this.attributes = attributes;
    }

    public Boolean getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(Boolean isCustom) {
        this.isCustom = isCustom;
    }

    public PackageMetadata getPackageName() {
        return packageInfo;
    }

    public void setPackageName(PackageMetadata packageName) {
        this.packageInfo = packageName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClassMetadata> getPossibleChildren() {
        return possibleChildren;
    }

    public void setPossibleChildren(List<ClassMetadata> possibleChildren) {
        this.possibleChildren = possibleChildren;
    }

    public byte[] getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(byte[] smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public PackageMetadata getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageMetadata packageInfo) {
        this.packageInfo = packageInfo;
    }

    public ClassMetadata getParent() {
        return parent;
    }

    public void setParent(ClassMetadata parent) {
        this.parent = parent;
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
        if (!(object instanceof ClassMetadata)) {
            return false;
        }
        ClassMetadata other = (ClassMetadata) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "meta.ClassRegistry[id=" + id + "]";
    }

}
