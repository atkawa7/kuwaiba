/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.databasemodule.entity;

import com.arangodb.springframework.annotation.Document;
import java.util.Date;
import org.springframework.data.annotation.Id;

/**
 * Entity process
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Document("process")//collection name
public class Process {

    @Id
    private String id;
    private String Description;
    private Date creationDate;
    private Date modifyDate;
    private boolean sendedNotification;
    private Date dateSendNotification;

    public Process() {
        super();
    }

    public Process(String Description, Date creationDate) {
        super();
        this.Description = Description;
        this.creationDate = creationDate;
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
     * @return the Description
     */
    public String getDescription() {
        return Description;
    }

    /**
     * @param Description the Description to set
     */
    public void setDescription(String Description) {
        this.Description = Description;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the modifyDate
     */
    public Date getModifyDate() {
        return modifyDate;
    }

    /**
     * @param modifyDate the modifyDate to set
     */
    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    /**
     * @return the sendedNotification
     */
    public boolean isSendedNotification() {
        return sendedNotification;
    }

    /**
     * @param sendedNotification the sendedNotification to set
     */
    public void setSendedNotification(boolean sendedNotification) {
        this.sendedNotification = sendedNotification;
    }

    /**
     * @return the dateSendNotification
     */
    public Date getDateSendNotification() {
        return dateSendNotification;
    }

    /**
     * @param dateSendNotification the dateSendNotification to set
     */
    public void setDateSendNotification(Date dateSendNotification) {
        this.dateSendNotification = dateSendNotification;
    }

}
