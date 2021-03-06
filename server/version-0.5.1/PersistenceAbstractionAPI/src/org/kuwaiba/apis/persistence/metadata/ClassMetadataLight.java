/**
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.metadata;

import java.io.Serializable;

/**
 * Contains the basic meta data information about a class
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ClassMetadataLight implements Serializable{

    /**
     * ClassMetada's Id
     */
    private long id;
    /**
     * ClassMetada's Name
     */
    private String name;
    /**
     *  Classmetada's displayName
     */
    private String displayName;
    /**
     * Instances of this class can have views associated (this going to be "true" for all subclasses of ViewableObject)
     */
    private Boolean viewable;
    /**
     * Indicates if a class can have instances by itself (All GenericXXX classes
     * and others in package entity.core are used to take advantage of OOP)
     */
    private Boolean _abstract;
    /**
     *  Is this class a list type (Vendor, LocationOwner, OpticalLinkType, etc)
     */
    private Boolean listType;
    /**
     *  Parent ClassMetada name
     */
    private String parentClassName;
    /**
     *  Icon to show in trees and lists
     */
    private byte[] smallIcon;
    /**
     * Class metadata state default false operational or in design true
     */
    private Boolean inDesign;
    /**
     *  Shows if this is a core class (the ones provided in the official release) or a custom one
     */
    private Boolean custom;

    public ClassMetadataLight(){
    }
    
    public ClassMetadataLight(long id, String name, String displayName){
        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }

    public ClassMetadataLight(long id, String name, Boolean inDesign, Boolean custom) {
        this.id = id;
        this.name = name;
        this.inDesign = inDesign;
        this.custom = custom;
    }

    
    public ClassMetadataLight(long id, String name, Boolean viewable, Boolean _abstract, Boolean listType, String parentClassName, Boolean inDesign, Boolean custom) {
        this.id = id;
        this.name = name;
        this.viewable = viewable;
        this._abstract = _abstract;
        this.listType = listType;
        this.parentClassName = parentClassName;
        this.inDesign = inDesign;
        this.custom = custom;
    }
        
    // <editor-fold defaultstate="collapsed" desc="getters and setters methods. Click on the + sign on the left to edit the code.">
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean isListType() {
        return listType;
    }

    public void setListType(Boolean listType) {
        this.listType = listType;
    }

    public void setAbstract(Boolean _abstract) {
        this._abstract = _abstract;
    }

    public Boolean isAbstract() {
        return _abstract;
    }

    public Boolean isViewable() {
        return viewable;
    }

    public void setViewable(Boolean viewable) {
        this.viewable = viewable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
   
    public String getParentClassName() {
        return parentClassName;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

    public byte[] getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(byte[] smallIcon) {
        this.smallIcon = smallIcon;
    }
    
    public Boolean isInDesign() {
        return inDesign;
    }

    public void setInDesign(Boolean inDesing) {
        this.inDesign = inDesing;
    }

    public Boolean isCustom() {
        return custom;
    }

    public void setCustom(Boolean custom) {
        this.custom = custom;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }// </editor-fold>
}
