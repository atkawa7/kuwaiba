
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getSubnetPools complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSubnetPools">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="limit" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="parentId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="className" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "getSubnetPools", propOrder = {
    "limit",
    "parentId",
    "className",
    "sessionId"
})
public class GetSubnetPools {

    protected int limit;
    protected long parentId;
    protected String className;
    protected String sessionId;

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
     * Gets the value of the parentId property.
     * 
     */
    public long getParentId() {
        return parentId;
    }

    /**
     * Sets the value of the parentId property.
     * 
     */
    public void setParentId(long value) {
        this.parentId = value;
    }

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
