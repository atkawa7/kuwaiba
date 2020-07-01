
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getCommonParent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getCommonParent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="aObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="aOid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bOid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "getCommonParent", propOrder = {
    "aObjectClass",
    "aOid",
    "bObjectClass",
    "bOid",
    "sessionId"
})
public class GetCommonParent {

    protected String aObjectClass;
    protected String aOid;
    protected String bObjectClass;
    protected String bOid;
    protected String sessionId;

    /**
     * Gets the value of the aObjectClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAObjectClass() {
        return aObjectClass;
    }

    /**
     * Sets the value of the aObjectClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAObjectClass(String value) {
        this.aObjectClass = value;
    }

    /**
     * Gets the value of the aOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAOid() {
        return aOid;
    }

    /**
     * Sets the value of the aOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAOid(String value) {
        this.aOid = value;
    }

    /**
     * Gets the value of the bObjectClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBObjectClass() {
        return bObjectClass;
    }

    /**
     * Sets the value of the bObjectClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBObjectClass(String value) {
        this.bObjectClass = value;
    }

    /**
     * Gets the value of the bOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBOid() {
        return bOid;
    }

    /**
     * Sets the value of the bOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBOid(String value) {
        this.bOid = value;
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
