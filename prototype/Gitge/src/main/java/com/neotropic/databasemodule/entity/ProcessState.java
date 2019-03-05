package com.neotropic.databasemodule.entity;

import org.springframework.data.annotation.Id;

/**
 * Entity ProcessState
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class ProcessState {

    @Id
    private String id;
    private String description;

    public ProcessState() {
        super();
    }

    public ProcessState(String description) {
        super();
        this.description = description;
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
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
