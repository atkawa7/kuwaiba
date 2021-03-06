
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getSubClassesLightNoRecursive complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSubClassesLightNoRecursive">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="className" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="includeAbstractClasses" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="includeSelf" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
@XmlType(name = "getSubClassesLightNoRecursive", propOrder = {
    "className",
    "includeAbstractClasses",
    "includeSelf",
    "sessionId"
})
public class GetSubClassesLightNoRecursive {

    protected String className;
    protected boolean includeAbstractClasses;
    protected boolean includeSelf;
    protected String sessionId;

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the includeAbstractClasses property.
     * 
     */
    public boolean isIncludeAbstractClasses() {
        return includeAbstractClasses;
    }

    /**
     * Sets the value of the includeAbstractClasses property.
     * 
     */
    public void setIncludeAbstractClasses(boolean value) {
        this.includeAbstractClasses = value;
    }

    /**
     * Gets the value of the includeSelf property.
     * 
     */
    public boolean isIncludeSelf() {
        return includeSelf;
    }

    /**
     * Sets the value of the includeSelf property.
     * 
     */
    public void setIncludeSelf(boolean value) {
        this.includeSelf = value;
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
