
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for reconnectPhysicalConnection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="reconnectPhysicalConnection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="connectionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newASideClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newASideId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newBSideClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newBSideId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "reconnectPhysicalConnection", propOrder = {
    "connectionClass",
    "connectionId",
    "newASideClass",
    "newASideId",
    "newBSideClass",
    "newBSideId",
    "sessionId"
})
public class ReconnectPhysicalConnection {

    protected String connectionClass;
    protected String connectionId;
    protected String newASideClass;
    protected String newASideId;
    protected String newBSideClass;
    protected String newBSideId;
    protected String sessionId;

    /**
     * Gets the value of the connectionClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionClass() {
        return connectionClass;
    }

    /**
     * Sets the value of the connectionClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionClass(String value) {
        this.connectionClass = value;
    }

    /**
     * Gets the value of the connectionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Sets the value of the connectionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionId(String value) {
        this.connectionId = value;
    }

    /**
     * Gets the value of the newASideClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewASideClass() {
        return newASideClass;
    }

    /**
     * Sets the value of the newASideClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewASideClass(String value) {
        this.newASideClass = value;
    }

    /**
     * Gets the value of the newASideId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewASideId() {
        return newASideId;
    }

    /**
     * Sets the value of the newASideId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewASideId(String value) {
        this.newASideId = value;
    }

    /**
     * Gets the value of the newBSideClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewBSideClass() {
        return newBSideClass;
    }

    /**
     * Sets the value of the newBSideClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewBSideClass(String value) {
        this.newBSideClass = value;
    }

    /**
     * Gets the value of the newBSideId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewBSideId() {
        return newBSideId;
    }

    /**
     * Sets the value of the newBSideId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewBSideId(String value) {
        this.newBSideId = value;
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
