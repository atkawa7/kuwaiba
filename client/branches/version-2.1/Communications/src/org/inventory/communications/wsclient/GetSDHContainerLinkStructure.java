
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getSDHContainerLinkStructure complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSDHContainerLinkStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="containerLinkClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="containerLinkId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "getSDHContainerLinkStructure", propOrder = {
    "containerLinkClass",
    "containerLinkId",
    "sessionId"
})
public class GetSDHContainerLinkStructure {

    protected String containerLinkClass;
    protected String containerLinkId;
    protected String sessionId;

    /**
     * Gets the value of the containerLinkClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContainerLinkClass() {
        return containerLinkClass;
    }

    /**
     * Sets the value of the containerLinkClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContainerLinkClass(String value) {
        this.containerLinkClass = value;
    }

    /**
     * Gets the value of the containerLinkId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContainerLinkId() {
        return containerLinkId;
    }

    /**
     * Sets the value of the containerLinkId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContainerLinkId(String value) {
        this.containerLinkId = value;
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
