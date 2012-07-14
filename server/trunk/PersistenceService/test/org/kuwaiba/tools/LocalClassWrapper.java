/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kuwaiba.tools;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author adrian
 */
public class LocalClassWrapper {

    public static int MODIFIER_DUMMY = 1;
    public static int MODIFIER_NOCOUNT = 2;

    private String name;
    private int javaModifiers;
    private int applicationModifiers;
    private int classType;
    private List<LocalClassWrapper> directSubClasses;
    private List<LocalAttributeWrapper> attributes;

    public LocalClassWrapper() {
        directSubClasses = new ArrayList<LocalClassWrapper>();
        attributes = new ArrayList<LocalAttributeWrapper>();
    }

    public int getApplicationModifiers() {
        return applicationModifiers;
    }

    public void setApplicationModifiers(int applicationModifiers) {
        this.applicationModifiers = applicationModifiers;
    }

    public List<LocalAttributeWrapper> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<LocalAttributeWrapper> attributes) {
        this.attributes = attributes;
    }

    public List<LocalClassWrapper> getDirectSubClasses() {
        return directSubClasses;
    }

    public void setDirectSubClasses(List<LocalClassWrapper> directSubClasses) {
        this.directSubClasses = directSubClasses;
    }

    public int getJavaModifiers() {
        return javaModifiers;
    }

    public void setJavaModifiers(int javaModifiers) {
        this.javaModifiers = javaModifiers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

    public boolean isDummy(){
        return (applicationModifiers & MODIFIER_DUMMY) == MODIFIER_DUMMY;
    }

    public boolean isCountable() {
        return (applicationModifiers & MODIFIER_NOCOUNT) != MODIFIER_NOCOUNT;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
        return false;
        if (!(obj instanceof LocalClassWrapper))
            return false;
        return ((LocalClassWrapper)obj).getName().equals(getName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
