/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
    private Long id;
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
    private boolean viewable;

    /**
     * Indicates if a class can have instances by itself (All GenericXXX classes
     * and others in package entity.core are used to take advantage of OOP)
     */
    private boolean abstractClass;
    /**
     *  Is this class a list type (Vendor, LocationOwner, OpticalLinkType, etc)
     */
    private boolean listType;
    /**
     *  The parent ClassMetada name
     */
    private String parentClassName;
    /**
     *  Icon to show in trees and lists
     */
    private byte[] smallIcon;

    public ClassMetadataLight() {
    }

    
    public ClassMetadataLight(Long id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.abstractClass = false;
        this.viewable =false;
    }

    

    // <editor-fold defaultstate="collapsed" desc="getters and setters methods. Click on the + sign on the left to edit the code.">
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isListType() {
        return listType;
    }

    public void setListType(boolean listType) {
        this.listType = listType;
    }

    public void setAbstractClass(Boolean abstractClass) {
        this.abstractClass = abstractClass;
    }

    public Boolean isAbstractClass() {
        return abstractClass;
    }

    public boolean isViewable() {
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAbstractClass(boolean abstractClass) {
        this.abstractClass = abstractClass;
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
    // </editor-fold>
}
