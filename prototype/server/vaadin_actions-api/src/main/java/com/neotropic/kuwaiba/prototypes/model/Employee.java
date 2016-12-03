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
public class Employee {
    private String name;
    private String lastName;
    private int age = 0;

    public Employee(String name, String lastName, int age) {
        this.name = name;
        this.lastName = lastName;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    
    @Override
    public String toString(){
        return lastName == null || lastName.isEmpty() ? name : lastName.toUpperCase() + ", " + name + " (" + age + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Employee) {
            return name.equals(((Employee)obj).getName());
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
