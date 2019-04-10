
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getTemplateElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getTemplateElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="templateElementClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="templateElementId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "getTemplateElement", propOrder = {
    "templateElementClass",
    "templateElementId",
    "sessionId"
})
public class GetTemplateElement {

    protected String templateElementClass;
    protected long templateElementId;
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
     * Gets the value of the templateElementId property.
     * 
     */
    public long getTemplateElementId() {
        return templateElementId;
    }

    /**
     * Sets the value of the templateElementId property.
     * 
     */
    public void setTemplateElementId(long value) {
        this.templateElementId = value;
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
