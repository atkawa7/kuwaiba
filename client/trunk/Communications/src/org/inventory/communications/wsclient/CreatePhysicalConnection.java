
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createPhysicalConnection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createPhysicalConnection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="aObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="aObjectId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="bObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bObjectId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="parentClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parentId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="attributeNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributeValues" type="{http://jaxb.dev.java.net/array}stringArray" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="connectionClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "createPhysicalConnection", propOrder = {
    "aObjectClass",
    "aObjectId",
    "bObjectClass",
    "bObjectId",
    "parentClass",
    "parentId",
    "attributeNames",
    "attributeValues",
    "connectionClass",
    "sessionId"
})
public class CreatePhysicalConnection {

    protected String aObjectClass;
    protected long aObjectId;
    protected String bObjectClass;
    protected long bObjectId;
    protected String parentClass;
    protected long parentId;
    @XmlElement(nillable = true)
    protected List<String> attributeNames;
    @XmlElement(nillable = true)
    protected List<StringArray> attributeValues;
    protected String connectionClass;
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
     * Gets the value of the parentClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentClass() {
        return parentClass;
    }

    /**
     * Sets the value of the parentClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentClass(String value) {
        this.parentClass = value;
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
     * Gets the value of the attributeNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAttributeNames() {
        if (attributeNames == null) {
            attributeNames = new ArrayList<String>();
        }
        return this.attributeNames;
    }

    /**
     * Gets the value of the attributeValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StringArray }
     * 
     * 
     */
    public List<StringArray> getAttributeValues() {
        if (attributeValues == null) {
            attributeValues = new ArrayList<StringArray>();
        }
        return this.attributeValues;
    }

    /**
     * Gets the value of the connectionClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionClass() {
        return connectionClass;
    }

    /**
     * Sets the value of the connectionClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionClass(String value) {
        this.connectionClass = value;
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
