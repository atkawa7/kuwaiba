
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createBulkSpecialTemplateElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createBulkSpecialTemplateElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stElementClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stElementParentClassName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stElementParentId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stElementNamePattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "createBulkSpecialTemplateElement", propOrder = {
    "stElementClass",
    "stElementParentClassName",
    "stElementParentId",
    "stElementNamePattern",
    "sessionId"
})
public class CreateBulkSpecialTemplateElement {

    protected String stElementClass;
    protected String stElementParentClassName;
    protected String stElementParentId;
    protected String stElementNamePattern;
    protected String sessionId;

    /**
     * Gets the value of the stElementClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStElementClass() {
        return stElementClass;
    }

    /**
     * Sets the value of the stElementClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStElementClass(String value) {
        this.stElementClass = value;
    }

    /**
     * Gets the value of the stElementParentClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStElementParentClassName() {
        return stElementParentClassName;
    }

    /**
     * Sets the value of the stElementParentClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStElementParentClassName(String value) {
        this.stElementParentClassName = value;
    }

    /**
     * Gets the value of the stElementParentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStElementParentId() {
        return stElementParentId;
    }

    /**
     * Sets the value of the stElementParentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStElementParentId(String value) {
        this.stElementParentId = value;
    }

    /**
     * Gets the value of the stElementNamePattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStElementNamePattern() {
        return stElementNamePattern;
    }

    /**
     * Sets the value of the stElementNamePattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStElementNamePattern(String value) {
        this.stElementNamePattern = value;
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
