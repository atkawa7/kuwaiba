
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deleteMPLSLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deleteMPLSLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="linkClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="linkId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "deleteMPLSLink", propOrder = {
    "linkClass",
    "linkId",
    "forceDelete",
    "sessionId"
})
public class DeleteMPLSLink {

    protected String linkClass;
    protected long linkId;
    protected boolean forceDelete;
    protected String sessionId;

    /**
     * Gets the value of the linkClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkClass() {
        return linkClass;
    }

    /**
     * Sets the value of the linkClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkClass(String value) {
        this.linkClass = value;
    }

    /**
     * Gets the value of the linkId property.
     * 
     */
    public long getLinkId() {
        return linkId;
    }

    /**
     * Sets the value of the linkId property.
     * 
     */
    public void setLinkId(long value) {
        this.linkId = value;
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
