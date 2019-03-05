package com.neotropic.databasemodule.entity;

import com.arangodb.springframework.annotation.Document;
import org.springframework.data.annotation.Id;

/**
 * Entity operator
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Document("operators")//collection name
public class Operator {

    @Id
    private String id;
    private String name;
    private String operatorcode;
    private String icon;
    private boolean enabled;
    private boolean deleted;

    public Operator() {
        super();
    }

    public Operator(String name, String operatorcode, boolean enabled, boolean deleted) {
        super();
        this.name = name;
        this.operatorcode = operatorcode;
        this.enabled = enabled;
        this.deleted = deleted;
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
     * @return the operatorcode
     */
    public String getOperatorcode() {
        return operatorcode;
    }

    /**
     * @param operatorcode the operatorcode to set
     */
    public void setOperatorcode(String operatorcode) {
        this.operatorcode = operatorcode;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
