
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getContainersBetweenObjects complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getContainersBetweenObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectAClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="objectAId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="objectBClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="objectBId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="containerClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "getContainersBetweenObjects", propOrder = {
    "objectAClass",
    "objectAId",
    "objectBClass",
    "objectBId",
    "containerClass",
    "sessionId"
})
public class GetContainersBetweenObjects {

    protected String objectAClass;
    protected long objectAId;
    protected String objectBClass;
    protected long objectBId;
    protected String containerClass;
    protected String sessionId;

    /**
     * Gets the value of the objectAClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectAClass() {
        return objectAClass;
    }

    /**
     * Sets the value of the objectAClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectAClass(String value) {
        this.objectAClass = value;
    }

    /**
     * Gets the value of the objectAId property.
     * 
     */
    public long getObjectAId() {
        return objectAId;
    }

    /**
     * Sets the value of the objectAId property.
     * 
     */
    public void setObjectAId(long value) {
        this.objectAId = value;
    }

    /**
     * Gets the value of the objectBClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectBClass() {
        return objectBClass;
    }

    /**
     * Sets the value of the objectBClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectBClass(String value) {
        this.objectBClass = value;
    }

    /**
     * Gets the value of the objectBId property.
     * 
     */
    public long getObjectBId() {
        return objectBId;
    }

    /**
     * Sets the value of the objectBId property.
     * 
     */
    public void setObjectBId(long value) {
        this.objectBId = value;
    }

    /**
     * Gets the value of the containerClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContainerClass() {
        return containerClass;
    }

    /**
     * Sets the value of the containerClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContainerClass(String value) {
        this.containerClass = value;
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
