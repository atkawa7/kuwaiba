
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getTemplateSpecialElementChildren complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getTemplateSpecialElementChildren">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tsElementClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tsElementId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "getTemplateSpecialElementChildren", propOrder = {
    "tsElementClass",
    "tsElementId",
    "sessionId"
})
public class GetTemplateSpecialElementChildren {

    protected String tsElementClass;
    protected String tsElementId;
    protected String sessionId;

    /**
     * Gets the value of the tsElementClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTsElementClass() {
        return tsElementClass;
    }

    /**
     * Sets the value of the tsElementClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTsElementClass(String value) {
        this.tsElementClass = value;
    }

    /**
     * Gets the value of the tsElementId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTsElementId() {
        return tsElementId;
    }

    /**
     * Sets the value of the tsElementId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTsElementId(String value) {
        this.tsElementId = value;
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
