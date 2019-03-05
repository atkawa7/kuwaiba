package com.neotropic.databasemodule.entity;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.HashIndex;
import java.util.Date;
import org.springframework.data.annotation.Id;

/**
 * Entity user
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Document("users")//collection name
@HashIndex(fields = { "id", "login", "email" }, unique = true)
public class User {

    @Id
    private String id;

    private String firstName;
    private String firstLastname;
    private String secondName;
    private String secondLastname;    
    private String password;
    private Date passwordDate;
    private String email;
    private String identification;
    private String phoneNumber;
    private boolean enabled;
    private boolean deleted;

    public User() {
        super();
    }
    
    public User(String firstName, String firstLastname, String login, String password, String email, boolean enabled, boolean deleted) {
        super();
        this.firstName = firstName;
        this.firstLastname = firstLastname;        
        this.password = password;
        this.email = email;
        this.enabled = enabled;
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", firstName=" + firstName + ", firstLastname=" + firstLastname + ", secondName=" + secondName + ", secondLastname=" + secondLastname + ", password=" + password + ", passwordDate=" + passwordDate + ", email=" + email + ", identification=" + identification + ", phoneNumber=" + phoneNumber + ", enabled=" + enabled + ", deleted=" + deleted + '}';
    }
    
    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the firstLastname
     */
    public String getFirstLastname() {
        return firstLastname;
    }

    /**
     * @param firstLastname the firstLastname to set
     */
    public void setFirstLastname(String firstLastname) {
        this.firstLastname = firstLastname;
    }

    /**
     * @return the secondName
     */
    public String getSecondName() {
        return secondName;
    }

    /**
     * @param secondName the secondName to set
     */
    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    /**
     * @return the secondLastname
     */
    public String getSecondLastname() {
        return secondLastname;
    }

    /**
     * @param secondLastname the secondLastname to set
     */
    public void setSecondLastname(String secondLastname) {
        this.secondLastname = secondLastname;
    }   

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the identification
     */
    public String getIdentification() {
        return identification;
    }

    /**
     * @param identification the identification to set
     */
    public void setIdentification(String identification) {
        this.identification = identification;
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
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return the passwordDate
     */
    public Date getPasswordDate() {
        return passwordDate;
    }

    /**
     * @param passwordDate the passwordDate to set
     */
    public void setPasswordDate(Date passwordDate) {
        this.passwordDate = passwordDate;
    }
}
