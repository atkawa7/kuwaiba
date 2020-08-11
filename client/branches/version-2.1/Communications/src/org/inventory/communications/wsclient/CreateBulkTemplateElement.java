
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createBulkTemplateElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createBulkTemplateElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="templateElementClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="templateElementParentClassName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="templateElementParentId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="templateElementNamePattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "createBulkTemplateElement", propOrder = {
    "templateElementClass",
    "templateElementParentClassName",
    "templateElementParentId",
    "templateElementNamePattern",
    "sessionId"
})
public class CreateBulkTemplateElement {

    protected String templateElementClass;
    protected String templateElementParentClassName;
    protected String templateElementParentId;
    protected String templateElementNamePattern;
    protected String sessionId;

    /**
     * Gets the value of the templateElementClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateElementClass() {
        return templateElementClass;
    }

    /**
     * Sets the value of the templateElementClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateElementClass(String value) {
        this.templateElementClass = value;
    }

    /**
     * Gets the value of the templateElementParentClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateElementParentClassName() {
        return templateElementParentClassName;
    }

    /**
     * Sets the value of the templateElementParentClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateElementParentClassName(String value) {
        this.templateElementParentClassName = value;
    }

    /**
     * Gets the value of the templateElementParentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateElementParentId() {
        return templateElementParentId;
    }

    /**
     * Sets the value of the templateElementParentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateElementParentId(String value) {
        this.templateElementParentId = value;
    }

    /**
     * Gets the value of the templateElementNamePattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateElementNamePattern() {
        return templateElementNamePattern;
    }

    /**
     * Sets the value of the templateElementNamePattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateElementNamePattern(String value) {
        this.templateElementNamePattern = value;
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
