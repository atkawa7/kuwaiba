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

/**
 * This class holds information about the existing classes
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Metadata //Custom annotation to mark instances of this class as not business objects
          
@NamedQuery(name="flushClassMetadata", query="DELETE FROM ClassMetadata x")
public class ClassMetadata implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final Long ROOT_CLASS_ID = new Long(0);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable=false,unique=true,updatable=false)
    private String name = "";
    private String displayName;

    @JoinColumn(nullable=false,name="package_id")
    @ManyToOne//(cascade=CascadeType.PERSIST) We don't need cascade, since we're supposed to check
              //data integrity before performing a deletion
    /**
     * This is the package where the class belongs to. It's useful to reassemble the full-qualified
     * name in order to call Class.forName
     */
    private PackageMetadata packageInfo; 
    private String description;
    /**
     * Shows if this is a core class (the ones provided in the official release) or a custom one
     */
    @Column(nullable=false)
    private Boolean isCustom=false;
    /**
     * Indicates if a class can have instances by itself (base classes like GenericXXX or RootObject are used only for object orientation)
     */
    @Column(nullable=false)
    private Boolean isAbstract=false;
    /**
     * Indicates if the instance of this class is a physical active
     */
    @Column(nullable=false)
    private Boolean isAccountable=true;
    /**
     * Is this a dummy class as described in the Dummy annotation?
     */
    @Column(nullable=false)
    private Boolean isDummy=false;
    /**
     * Is this an class used for representing objects not related to the inventory itself, but for holding auxiliar information (marked with the annotation Administrative)
     */
    @Column(nullable=false)
    private Boolean isAdministrative = false;
    /**
     * Indicates if the current class implements the interface PhysicalNode
     */
    @Column(nullable=false)
    private Boolean isPhysicalNode = false;
    /**
     * Indicates if the current class implements the interface PhysicalConnection
     */
    @Column(nullable=false)
    private Boolean isPhysicalConnection = false;
    /**
     * Indicates if the current class implements the interface PhysicalEndpoint
     */
    @Column(nullable=false)
    private Boolean isPhysicalEndpoint = false;
    /**
     * Classes decorated with "hidden" annotation shouldn't be returned by getMetadata or getLightMetadata
     */
    @Column(nullable=false)
    private Boolean isHidden = false;
    private byte[] smallIcon;
    private byte[] icon;
    
    private Integer color;              //Color assigned to the instances when displayed

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

    //@JoinColumn(nullable=false,name="parent_id",updatable=false) //This mapping won't let me query for results usin JPQL
    @Column(nullable=false,name="parent_id",updatable=false)
    private Long parent; //Represents the relation with the parent class
                                  //The top should be RootObject, except of course, for RootObject itself


    public ClassMetadata() {
    }

    public ClassMetadata(String _name, PackageMetadata _myPackage, String _description,
            Boolean _isCustom, Boolean _isAbstract, Boolean _isDummy, Boolean _isAdministrative,Boolean _isHidden,
            Boolean _isPhysicalNode, Boolean _isPhysicalConnection, Boolean _isPhysicalEndpoint,
            List<ClassMetadata> _children, List <AttributeMetadata> _attributes, Long _parent){
        this.name = _name;
        this.packageInfo = _myPackage;
        this.description = _description;
        this.isCustom = _isCustom;
        this.isAbstract = _isAbstract;
        this.isDummy = _isDummy;
        this.isAdministrative =_isAdministrative;
        this.isHidden = _isHidden;
        this.isPhysicalNode = _isPhysicalNode;
        this.isPhysicalConnection = _isPhysicalConnection;
        this.isPhysicalEndpoint = _isPhysicalEndpoint;
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

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
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

    public Boolean getIsAdministrative() {
        return isAdministrative;
    }

    public void setIsAdministrative(Boolean isAdministrative) {
        this.isAdministrative = isAdministrative;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Boolean getIsPhysicalConnection() {
        return isPhysicalConnection;
    }

    public void setIsPhysicalConnection(Boolean isPhysicalConnection) {
        this.isPhysicalConnection = isPhysicalConnection;
    }

    public Boolean getIsPhysicalEndpoint() {
        return isPhysicalEndpoint;
    }

    public void setIsPhysicalEndpoint(Boolean isPhysicalEndpoint) {
        this.isPhysicalEndpoint = isPhysicalEndpoint;
    }

    public Boolean getIsPhysicalNode() {
        return isPhysicalNode;
    }

    public void setIsPhysicalNode(Boolean isPhysicalNode) {
        this.isPhysicalNode = isPhysicalNode;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}
