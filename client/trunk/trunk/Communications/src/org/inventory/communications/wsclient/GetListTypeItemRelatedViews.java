
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getListTypeItemRelatedViews complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getListTypeItemRelatedViews">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="listTypeItemId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="listTypeItemClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "getListTypeItemRelatedViews", propOrder = {
    "listTypeItemId",
    "listTypeItemClass",
    "limit",
    "sessionId"
})
public class GetListTypeItemRelatedViews {

    protected long listTypeItemId;
    protected String listTypeItemClass;
    protected int limit;
    protected String sessionId;

    /**
     * Gets the value of the listTypeItemId property.
     * 
     */
    public long getListTypeItemId() {
        return listTypeItemId;
    }

    /**
     * Sets the value of the listTypeItemId property.
     * 
     */
    public void setListTypeItemId(long value) {
        this.listTypeItemId = value;
    }

    /**
     * Gets the value of the listTypeItemClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListTypeItemClass() {
        return listTypeItemClass;
    }

    /**
     * Sets the value of the listTypeItemClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListTypeItemClass(String value) {
        this.listTypeItemClass = value;
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
