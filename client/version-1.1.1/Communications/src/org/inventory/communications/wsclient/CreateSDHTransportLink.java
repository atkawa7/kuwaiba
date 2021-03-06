
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createSDHTransportLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createSDHTransportLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="classNameEndpointA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="idEndpointA" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="classNameEndpointB" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="idEndpointB" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="linkType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="defaultName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "createSDHTransportLink", propOrder = {
    "classNameEndpointA",
    "idEndpointA",
    "classNameEndpointB",
    "idEndpointB",
    "linkType",
    "defaultName",
    "sessionId"
})
public class CreateSDHTransportLink {

    protected String classNameEndpointA;
    protected long idEndpointA;
    protected String classNameEndpointB;
    protected long idEndpointB;
    protected String linkType;
    protected String defaultName;
    protected String sessionId;

    /**
     * Gets the value of the classNameEndpointA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassNameEndpointA() {
        return classNameEndpointA;
    }

    /**
     * Sets the value of the classNameEndpointA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassNameEndpointA(String value) {
        this.classNameEndpointA = value;
    }

    /**
     * Gets the value of the idEndpointA property.
     * 
     */
    public long getIdEndpointA() {
        return idEndpointA;
    }

    /**
     * Sets the value of the idEndpointA property.
     * 
     */
    public void setIdEndpointA(long value) {
        this.idEndpointA = value;
    }

    /**
     * Gets the value of the classNameEndpointB property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassNameEndpointB() {
        return classNameEndpointB;
    }

    /**
     * Sets the value of the classNameEndpointB property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassNameEndpointB(String value) {
        this.classNameEndpointB = value;
    }

    /**
     * Gets the value of the idEndpointB property.
     * 
     */
    public long getIdEndpointB() {
        return idEndpointB;
    }

    /**
     * Sets the value of the idEndpointB property.
     * 
     */
    public void setIdEndpointB(long value) {
        this.idEndpointB = value;
    }

    /**
     * Gets the value of the linkType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkType() {
        return linkType;
    }

    /**
     * Sets the value of the linkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkType(String value) {
        this.linkType = value;
    }

    /**
     * Gets the value of the defaultName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultName() {
        return defaultName;
    }

    /**
     * Sets the value of the defaultName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultName(String value) {
        this.defaultName = value;
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
