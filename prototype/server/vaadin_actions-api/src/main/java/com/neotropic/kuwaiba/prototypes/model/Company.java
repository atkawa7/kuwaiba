/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.model;

import java.util.Objects;

/**
 * Represents a business domain object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Company {
    private String name;
    private String fieldOfExpertise;

    public Company(String name, String fieldOfExpertise) {
        this.name = name;
        this.fieldOfExpertise = fieldOfExpertise;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFieldOfExpertise() {
        return fieldOfExpertise;
    }

    public void setFieldOfExpertise(String fieldOfExpertise) {
        this.fieldOfExpertise = fieldOfExpertise;
    }

    @Override
    public String toString(){
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Company) {
            return name.equals(((Company)obj).getName());
        } else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
