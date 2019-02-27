/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.entity;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.HashIndex;
import org.springframework.data.annotation.Id;

/**
 * Lets create our first bean which will represent a collection in our database.
 * With the @Document annotation we define the collection as a document
 * collection. In our case we also define the alternative name characters for
 * the collection. By default the collection name is determined by the class
 * name. @Document also provides additional options for the collection which
 * will be used at creation time of the collection.
 *
 * Because many operations on documents require a document handle, it’s
 * recommended to add a field of type String annotated wit @Id to every entity.
 * The name doesn’t matter. It’s further recommended to not set or
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Document("characters")
@HashIndex(fields = { "name", "surname" }, unique = true)
public class Character {

    @Id
    private String id;

    private String name;
    private String surname;
    private boolean alive;
    private Integer age;

    public Character() {
        super();
    }

    public Character(final String name, final String surname) {
        super();
        this.name = name;
        this.surname = surname;
    }
    public Character(final String name, final String surname, final boolean alive) {
        super();
        this.name = name;
        this.surname = surname;
        this.alive = alive;
    }

    public Character(final String name, final String surname, final boolean alive, final Integer age) {
        super();
        this.name = name;
        this.surname = surname;
        this.alive = alive;
        this.age = age;
    }

    // getter & setter
    @Override
    public String toString() {
        return "Character [id=" + getId() + ", name=" + getName() + ", surname=" + getSurname() + ", alive=" + isAlive() + ", age=" + getAge() + "]";
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the alive
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * @param alive the alive to set
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * @return the age
     */
    public Integer getAge() {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(Integer age) {
        this.age = age;
    }

}
