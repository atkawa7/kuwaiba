
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for updateFavoritesFolder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="updateFavoritesFolder">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="favoritesFolderId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="favoritesFolderName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "updateFavoritesFolder", propOrder = {
    "favoritesFolderId",
    "favoritesFolderName",
    "userId",
    "sessionId"
})
public class UpdateFavoritesFolder {

    protected long favoritesFolderId;
    protected String favoritesFolderName;
    protected long userId;
    protected String sessionId;

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
     * Gets the value of the favoritesFolderName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFavoritesFolderName() {
        return favoritesFolderName;
    }

    /**
     * Sets the value of the favoritesFolderName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFavoritesFolderName(String value) {
        this.favoritesFolderName = value;
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
