
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for connectMirrorPort complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="connectMirrorPort">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="aObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="aObjectId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="bObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bObjectId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "connectMirrorPort", propOrder = {
    "aObjectClass",
    "aObjectId",
    "bObjectClass",
    "bObjectId",
    "sessionId"
})
public class ConnectMirrorPort {

    protected String aObjectClass;
    protected long aObjectId;
    protected String bObjectClass;
    protected long bObjectId;
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
     * Gets the value of the aObjectId property.
     * 
     */
    public long getAObjectId() {
        return aObjectId;
    }

    /**
     * Sets the value of the aObjectId property.
     * 
     */
    public void setAObjectId(long value) {
        this.aObjectId = value;
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
     * Gets the value of the bObjectId property.
     * 
     */
    public long getBObjectId() {
        return bObjectId;
    }

    /**
     * Sets the value of the bObjectId property.
     * 
     */
    public void setBObjectId(long value) {
        this.bObjectId = value;
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
