
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for findSDHRoutesUsingContainerLinks complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="findSDHRoutesUsingContainerLinks">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="communicationsEquipmentClassA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="communicationsEquipmentIdA" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="communicationsEquipmentClassB" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="communicationsEquipmentIB" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "findSDHRoutesUsingContainerLinks", propOrder = {
    "communicationsEquipmentClassA",
    "communicationsEquipmentIdA",
    "communicationsEquipmentClassB",
    "communicationsEquipmentIB",
    "sessionId"
})
public class FindSDHRoutesUsingContainerLinks {

    protected String communicationsEquipmentClassA;
    protected long communicationsEquipmentIdA;
    protected String communicationsEquipmentClassB;
    protected long communicationsEquipmentIB;
    protected String sessionId;

    /**
     * Gets the value of the communicationsEquipmentClassA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommunicationsEquipmentClassA() {
        return communicationsEquipmentClassA;
    }

    /**
     * Sets the value of the communicationsEquipmentClassA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommunicationsEquipmentClassA(String value) {
        this.communicationsEquipmentClassA = value;
    }

    /**
     * Gets the value of the communicationsEquipmentIdA property.
     * 
     */
    public long getCommunicationsEquipmentIdA() {
        return communicationsEquipmentIdA;
    }

    /**
     * Sets the value of the communicationsEquipmentIdA property.
     * 
     */
    public void setCommunicationsEquipmentIdA(long value) {
        this.communicationsEquipmentIdA = value;
    }

    /**
     * Gets the value of the communicationsEquipmentClassB property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommunicationsEquipmentClassB() {
        return communicationsEquipmentClassB;
    }

    /**
     * Sets the value of the communicationsEquipmentClassB property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommunicationsEquipmentClassB(String value) {
        this.communicationsEquipmentClassB = value;
    }

    /**
     * Gets the value of the communicationsEquipmentIB property.
     * 
     */
    public long getCommunicationsEquipmentIB() {
        return communicationsEquipmentIB;
    }

    /**
     * Sets the value of the communicationsEquipmentIB property.
     * 
     */
    public void setCommunicationsEquipmentIB(long value) {
        this.communicationsEquipmentIB = value;
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
