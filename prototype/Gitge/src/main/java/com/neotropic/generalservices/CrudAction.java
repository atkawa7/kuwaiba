/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.generalservices;

/**
 * Enum class description for crud possibilities
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public enum CrudAction {
    CREATE("Create"), READ("Read"), UPDATE("Update"), DELETE("Delete");

    private final String action;

    CrudAction(String value) {
        this.action = value;
    }

    public String getAction() {
        return action;
    }
}
