/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.persistence;

/**
 *
 * @author adrian
 */
public class CategoryMetadata {
    
    public static String PROPERTY_NAME ="name"; //NOI18N
    public static String PROPERTY_DISPLAY_NAME ="displayName"; //NOI18N
    public static String PROPERTY_DESCRIPTION ="description"; //NOI18N
    
    String name;
    String displayName;
    String description;

    // <editor-fold defaultstate="collapsed" desc="getters and setters methods. Click on the + sign on the left to edit the code.">
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
    }// </editor-fold>
    
}
