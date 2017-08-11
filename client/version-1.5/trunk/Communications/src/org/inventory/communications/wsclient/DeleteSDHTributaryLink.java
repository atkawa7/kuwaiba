
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deleteSDHTributaryLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deleteSDHTributaryLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tributaryLinkClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tributaryLinkId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="forceDelete" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
@XmlType(name = "deleteSDHTributaryLink", propOrder = {
    "tributaryLinkClass",
    "tributaryLinkId",
    "forceDelete",
    "sessionId"
})
public class DeleteSDHTributaryLink {

    protected String tributaryLinkClass;
    protected long tributaryLinkId;
    protected boolean forceDelete;
    protected String sessionId;

    /**
     * Gets the value of the tributaryLinkClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTributaryLinkClass() {
        return tributaryLinkClass;
    }

    /**
     * Sets the value of the tributaryLinkClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTributaryLinkClass(String value) {
        this.tributaryLinkClass = value;
    }

    /**
     * Gets the value of the tributaryLinkId property.
     * 
     */
    public long getTributaryLinkId() {
        return tributaryLinkId;
    }

    /**
     * Sets the value of the tributaryLinkId property.
     * 
     */
    public void setTributaryLinkId(long value) {
        this.tributaryLinkId = value;
    }

    /**
     * Gets the value of the forceDelete property.
     * 
     */
    public boolean isForceDelete() {
        return forceDelete;
    }

    /**
     * Sets the value of the forceDelete property.
     * 
     */
    public void setForceDelete(boolean value) {
        this.forceDelete = value;
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
