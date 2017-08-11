/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.persistence.application;

/**
 * A POJO representation of a category in a list of favorites.
 * @author johnyortega
 */
public class FavoritesFolder {
    /**
     * favoritesFolder id
     */
    private long id;
    /**
     * Favorites Folder name
     */
    private String name;
    
    public FavoritesFolder(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
