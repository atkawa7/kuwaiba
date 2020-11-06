/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.kuwaiba.modules.core.containmentman.api;

import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;

/**
 *
 * @author rchingal
 */
public class ClassMetadataLightCustom {
    private boolean root;
    private ClassMetadataLight classMetadataLight; 

    public ClassMetadataLightCustom(ClassMetadataLight classMetadataLight) {
        this.classMetadataLight = classMetadataLight;
        this.root = true;
    }
    
    public ClassMetadataLightCustom(ClassMetadataLight classMetadataLight, boolean root) {
        this.classMetadataLight = classMetadataLight;
        this.root = root;
    }
    
    
    public String toString() {
        return (this.classMetadataLight.getDisplayName() == null || this.classMetadataLight.getDisplayName().isEmpty())
                ? this.classMetadataLight.getName() : this.classMetadataLight.getDisplayName().trim();
    }

    /**
     * ClassMetada's Name
     */
    public String getName() {
        return classMetadataLight.getName();
    }
    
    /**
     * ClassMetada 
     */
    public ClassMetadataLight getClassMetadataLight() {
        return classMetadataLight;
    }
    
    /**
     * @return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(boolean root) {
        this.root = root;
    }
    
}
