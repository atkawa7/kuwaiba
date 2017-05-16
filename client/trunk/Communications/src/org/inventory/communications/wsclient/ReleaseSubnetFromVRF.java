
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for releaseSubnetFromVRF complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="releaseSubnetFromVRF">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="subnetId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="vrfId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "releaseSubnetFromVRF", propOrder = {
    "subnetId",
    "vrfId",
    "sessionId"
})
public class ReleaseSubnetFromVRF {

    protected long subnetId;
    protected long vrfId;
    protected String sessionId;

    /**
     * Gets the value of the subnetId property.
     * 
     */
    public long getSubnetId() {
        return subnetId;
    }

    /**
     * Sets the value of the subnetId property.
     * 
     */
    public void setSubnetId(long value) {
        this.subnetId = value;
    }

    /**
     * Gets the value of the vrfId property.
     * 
     */
    public long getVrfId() {
        return vrfId;
    }

    /**
     * Sets the value of the vrfId property.
     * 
     */
    public void setVrfId(long value) {
        this.vrfId = value;
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
