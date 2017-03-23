
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getGeneralViews complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getGeneralViews">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="viewClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="limit" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
@XmlType(name = "getGeneralViews", propOrder = {
    "viewClass",
    "limit",
    "sessionId"
})
public class GetGeneralViews {

    protected String viewClass;
    protected int limit;
    protected String sessionId;

    /**
     * Gets the value of the viewClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getViewClass() {
        return viewClass;
    }

    /**
     * Sets the value of the viewClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setViewClass(String value) {
        this.viewClass = value;
    }

    /**
     * Gets the value of the limit property.
     * 
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the value of the limit property.
     * 
     */
    public void setLimit(int value) {
        this.limit = value;
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
