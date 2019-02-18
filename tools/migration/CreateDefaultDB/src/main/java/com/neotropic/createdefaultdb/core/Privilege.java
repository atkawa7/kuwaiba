package com.neotropic.createdefaultdb.core;


import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
/**
 * A privilege is composed by a string token (unique id of the module or method
 * the privilege refers to, for example "nav-tree" or "create-object") and a number
 * that specifies the access level (see ACCESS_LEVEL* for possible values)
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Privilege {

    /**
     * The user can access the feature in a read-only mode
     */
    public static final int ACCESS_LEVEL_READ = 1;
    /**
     * The user can access the feature in a read and write mode
     */
    public static final int ACCESS_LEVEL_READ_WRITE = 2;
    /**
     * Feature token property name to be used in the data base
     */
    public static final String PROPERTY_FEATURE_TOKEN = "featureToken";
    /**
     * Access level property name to be used in the data base
     */
    public static final String PROPERTY_ACCESS_LEVEL = "accessLevel";
    /**
     * The unique id of the feature (for example, a web service method or a
     * simple string with the name of the module)
     */
    private String featureToken;
    /**
     * Access level. See ACCESS_LEVEL* for possible values
     */
    private int accessLevel;

    public Privilege(String featureToken, int accessLevel) {
        this.featureToken = featureToken;
        this.accessLevel = accessLevel;
    }

    public String getFeatureToken() {
        return featureToken;
    }

    public void setFeatureToken(String featureToken) {
        this.featureToken = featureToken;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Privilege) {
            return featureToken.equals(((Privilege) obj).getFeatureToken()); //Only the feature token is enough
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.featureToken);
        hash = 13 * hash + this.accessLevel;
        return hash;
    }
}
