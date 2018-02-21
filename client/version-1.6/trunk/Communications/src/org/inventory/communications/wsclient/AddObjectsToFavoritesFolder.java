
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for addObjectsToFavoritesFolder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="addObjectsToFavoritesFolder">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectClass" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="objectId" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="favoritesFolderId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addObjectsToFavoritesFolder", propOrder = {
    "objectClass",
    "objectId",
    "favoritesFolderId",
    "userId",
    "sessionId"
})
public class AddObjectsToFavoritesFolder {

    @XmlElement(nillable = true)
    protected List<String> objectClass;
    @XmlElement(nillable = true)
    protected List<Long> objectId;
    protected long favoritesFolderId;
    protected long userId;
    protected String sessionId;

    /**
     * Gets the value of the objectClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getObjectClass() {
        if (objectClass == null) {
            objectClass = new ArrayList<String>();
        }
        return this.objectClass;
    }

    /**
     * Gets the value of the objectId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getObjectId() {
        if (objectId == null) {
            objectId = new ArrayList<Long>();
        }
        return this.objectId;
    }

    /**
     * Gets the value of the favoritesFolderId property.
     * 
     */
    public long getFavoritesFolderId() {
        return favoritesFolderId;
    }

    /**
     * Sets the value of the favoritesFolderId property.
     * 
     */
    public void setFavoritesFolderId(long value) {
        this.favoritesFolderId = value;
    }

    /**
     * Gets the value of the userId property.
     * 
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     */
    public void setUserId(long value) {
        this.userId = value;
    }

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

}
