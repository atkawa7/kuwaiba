
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPoolsInPool complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getPoolsInPool">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parentPoolId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="poolClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "getPoolsInPool", propOrder = {
    "parentPoolId",
    "poolClass",
    "sessionId"
})
public class GetPoolsInPool {

    protected long parentPoolId;
    protected String poolClass;
    protected String sessionId;

    /**
     * Gets the value of the parentPoolId property.
     * 
     */
    public long getParentPoolId() {
        return parentPoolId;
    }

    /**
     * Sets the value of the parentPoolId property.
     * 
     */
    public void setParentPoolId(long value) {
        this.parentPoolId = value;
    }

    /**
     * Gets the value of the poolClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPoolClass() {
        return poolClass;
    }

    /**
     * Sets the value of the poolClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPoolClass(String value) {
        this.poolClass = value;
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
