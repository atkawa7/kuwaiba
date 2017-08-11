
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deleteSDHTransportLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deleteSDHTransportLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="transportLinkClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transportLinkId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "deleteSDHTransportLink", propOrder = {
    "transportLinkClass",
    "transportLinkId",
    "forceDelete",
    "sessionId"
})
public class DeleteSDHTransportLink {

    protected String transportLinkClass;
    protected long transportLinkId;
    protected boolean forceDelete;
    protected String sessionId;

    /**
     * Gets the value of the transportLinkClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransportLinkClass() {
        return transportLinkClass;
    }

    /**
     * Sets the value of the transportLinkClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransportLinkClass(String value) {
        this.transportLinkClass = value;
    }

    /**
     * Gets the value of the transportLinkId property.
     * 
     */
    public long getTransportLinkId() {
        return transportLinkId;
    }

    /**
     * Sets the value of the transportLinkId property.
     * 
     */
    public void setTransportLinkId(long value) {
        this.transportLinkId = value;
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
