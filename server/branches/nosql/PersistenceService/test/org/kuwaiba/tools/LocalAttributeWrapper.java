/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kuwaiba.tools;

import java.util.Random;

/**
 *
 * @author adrian
 */
public class LocalAttributeWrapper {

    public static int MODIFIER_NOCOPY = 1;
    public static int MODIFIER_NOSERIALIZE = 2;
    public static int MODIFIER_READONLY = 4;

    private int prefix;
    private String name;
    private int javaModifiers;
    private int applicationModifiers = 0;
    private String type;

    public LocalAttributeWrapper() {
        this.prefix = new Random().nextInt(1000000);
    }


    public int getApplicationModifiers() {
        return applicationModifiers;
    }

    public void setApplicationModifiers(int applicationModifiers) {
        this.applicationModifiers = applicationModifiers;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPrefix() {
        return prefix;
    }

    public void setPrefix(int prefix) {
        this.prefix = prefix;
    }

    public boolean canCopy(){
        return (applicationModifiers & MODIFIER_NOCOPY) != MODIFIER_NOCOPY;
    }

    public boolean canWrite() {
        return (applicationModifiers & MODIFIER_READONLY) != MODIFIER_READONLY;
    }

    public boolean canSerialize(){
        return (applicationModifiers & MODIFIER_NOSERIALIZE) != MODIFIER_NOSERIALIZE;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof LocalAttributeWrapper))
            return false;
        return (((LocalAttributeWrapper)obj).getName() + ((LocalAttributeWrapper)obj).getPrefix()).equals(getName()+getPrefix());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
