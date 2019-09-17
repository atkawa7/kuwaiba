
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deleteListTypeItemRelatedView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deleteListTypeItemRelatedView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="listTypeItemId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="listTypeItemClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="viewId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "deleteListTypeItemRelatedView", propOrder = {
    "listTypeItemId",
    "listTypeItemClass",
    "viewId",
    "sessionId"
})
public class DeleteListTypeItemRelatedView {

    protected String listTypeItemId;
    protected String listTypeItemClass;
    protected long viewId;
    protected String sessionId;

    /**
     * Gets the value of the listTypeItemId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListTypeItemId() {
        return listTypeItemId;
    }

    /**
     * Sets the value of the listTypeItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListTypeItemId(String value) {
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
     * Gets the value of the viewId property.
     * 
     */
    public long getViewId() {
        return viewId;
    }

    /**
     * Sets the value of the viewId property.
     * 
     */
    public void setViewId(long value) {
        this.viewId = value;
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
