package entity.core.metamodel;

import core.annotations.Metadata;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
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
@Metadata //Custom annotation to mark instances of this class as not business objects
          
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
    @ManyToOne//(cascade=CascadeType.PERSIST) We don't need cascade, since we're supposed to check
              //data integrity before performing a deletion
    private PackageMetadata packageInfo; //This is the package where the class belongs to. It's useful to reassemble the full-qualified
                                         //name in order to call Class.forName
    private String description;
    @Column(nullable=false)
    private Boolean isCustom=false;       //Shows if this is a core class (the ones provided in the official release) or a custom one
    @Column(nullable=false)
    private Boolean isAbstract=false;     //Indicates if a class can have instances by itself (base classes like GenericXXX or RootObject are used only for object orientation)
    @Column(nullable=false)
    private Boolean isAccountable=true;      //Indicates if the instance of this class is a physical active
    @Column(nullable=false)
    private Boolean isDummy=false;      //Is this a dummy class as described in the Dummy annotation?
    private byte[] smallIcon;
    private byte[] icon;

    /*
     * Note: In the container hierarchy there must be a dummy class to represent
     * the root node in the navigation tree
     */
    @OneToMany
    @JoinTable(name="ContainerHierarchy") //This is the name assigned to the table which implement the relationship
    private List<ClassMetadata> possibleChildren;

    @OneToMany(cascade=CascadeType.PERSIST) //If one deletes a class, the related attributes should be deleted too. 
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
            Boolean _isCustom, Boolean _isAbstract, Boolean _isDummy,List<ClassMetadata> _children, List <AttributeMetadata> _attributes, ClassMetadata _parent){
        this.name = _name;
        this.packageInfo = _myPackage;
        this.description = _description;
        this.isCustom = _isCustom;
        this.isAbstract = _isAbstract;
        this.isDummy = _isDummy;
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
        return "meta.ClassMetadata[id=" + id + "]";
    }

    public Boolean getIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(Boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public Boolean getIsAccountable() {
        return isAccountable;
    }

    public void setIsAccountable(Boolean isAccountable) {
        this.isAccountable = isAccountable;
    }

    public Boolean getIsDummy() {
        return isDummy;
    }

    public void setIsDummy(Boolean isDummy) {
        this.isDummy = isDummy;
    }

}
