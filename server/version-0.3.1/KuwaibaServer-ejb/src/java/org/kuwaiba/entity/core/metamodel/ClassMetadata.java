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
package org.kuwaiba.entity.core.metamodel;

import org.kuwaiba.entity.core.MetadataObject;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@NamedQuery(name="flushClassMetadata", query="DELETE FROM ClassMetadata x")
public class ClassMetadata extends MetadataObject {
    public static final Long ROOT_CLASS_ID = new Long(0);

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
     * Indicates if a class can have instances by itself (All GenericXXX classes and others in package entity.core are used to take advantage of OOP)
     */
    @Column(nullable=false)
    private Boolean isAbstract=false;
    /**
     * Indicates if the instances of this class are physical assets (in other words, if it's meaningful to have a count on them)
     * Classes marked with the annotation NoCount (Slot, Port and the like) have this attribute set as false
     */
    @Column(nullable=false)
    private Boolean isCountable=true;
    /**
     * Is this a dummy class as described in the Dummy annotation?
     */
    @Column(nullable=false)
    private Boolean isDummy=false;
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
     * Is this class a list type (Vendor, LocationOwner, OpticalLinkType, etc)
     */
    @Column(nullable=false)
    private Boolean isListType = false;
    /**
     * Instances of this class can have views associated (this going to be "true" for all subclasses of ViewableObject)
     */
    @Column(nullable=false)
    private Boolean isViewable = true;
    /**
     * Icon to show in trees and lists
     */
    private byte[] smallIcon;
    /**
     * Icon to show in views
     */
    private byte[] icon;
    /**
     * Color assigned to the instances when displayed
     */
    private Integer color;

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


    public ClassMetadata() {
    }

    public ClassMetadata(String _name, PackageMetadata _myPackage, String _description,
            Boolean _isCustom, Boolean _isAbstract, Boolean _isDummy, Boolean _isPhysicalNode,
            Boolean _isPhysicalConnection, Boolean _isPhysicalEndpoint, Boolean _isListType,
            Boolean _isCountable, Boolean _isRelatableToService, Boolean _isViewable, List<ClassMetadata> _children, List <AttributeMetadata> _attributes){
        this.name = _name;
        this.packageInfo = _myPackage;
        this.description = _description;
        this.isCustom = _isCustom;
        this.isAbstract = _isAbstract;
        this.isDummy = _isDummy;
        this.isPhysicalNode = _isPhysicalNode;
        this.isPhysicalConnection = _isPhysicalConnection;
        this.isPhysicalEndpoint = _isPhysicalEndpoint;
        this.isListType = _isListType;
        this.isCountable = _isCountable;
        this.isViewable = _isViewable;
        this.possibleChildren = _children;
        this.attributes = _attributes;
    }

    public List<AttributeMetadata> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeMetadata> attributes) {
        this.attributes = attributes;
    }

    public Boolean isCustom() {
        return isCustom;
    }

    public void setAsCustom(Boolean isCustom) {
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
        return getName();
    }

    public Boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(Boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public Boolean isAccountable() {
        return isCountable;
    }

    public void setAccountable(Boolean isCountable) {
        this.isCountable = isCountable;
    }

    public Boolean isDummy() {
        return isDummy;
    }

    public void setDummy(Boolean isDummy) {
        this.isDummy = isDummy;
    }

    public Boolean isViewable() {
        return isViewable;
    }

    public void setViewable(Boolean isViewable) {
        this.isViewable = isViewable;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Boolean isPhysicalConnection() {
        return isPhysicalConnection;
    }

    public void setAsPhysicalConnection(Boolean isPhysicalConnection) {
        this.isPhysicalConnection = isPhysicalConnection;
    }

    public Boolean isPhysicalEndpoint() {
        return isPhysicalEndpoint;
    }

    public void setAsPhysicalEndpoint(Boolean isPhysicalEndpoint) {
        this.isPhysicalEndpoint = isPhysicalEndpoint;
    }

    public Boolean isPhysicalNode() {
        return isPhysicalNode;
    }

    public void setAsPhysicalNode(Boolean isPhysicalNode) {
        this.isPhysicalNode = isPhysicalNode;
    }

    public Boolean isListType() {
        return isListType;
    }

    public void setAsListType(Boolean isListType) {
        this.isListType = isListType;
    }
}
