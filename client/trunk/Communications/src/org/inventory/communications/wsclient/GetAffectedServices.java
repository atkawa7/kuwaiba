
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAffectedServices complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getAffectedServices">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="resourceType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="resourceDefinition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "getAffectedServices", propOrder = {
    "resourceType",
    "resourceDefinition",
    "sessionId"
})
public class GetAffectedServices {

    protected int resourceType;
    protected String resourceDefinition;
    protected String sessionId;

    /**
     * Gets the value of the resourceType property.
     * 
     */
    public int getResourceType() {
        return resourceType;
    }

    /**
     * Sets the value of the resourceType property.
     * 
     */
    public void setResourceType(int value) {
        this.resourceType = value;
    }

    /**
     * Gets the value of the resourceDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceDefinition() {
        return resourceDefinition;
    }

    /**
     * Sets the value of the resourceDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceDefinition(String value) {
        this.resourceDefinition = value;
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
