
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for moveObjectsToWarehousePool complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="moveObjectsToWarehousePool">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="targetClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="targetOid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="objectsClasses" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="objectsOids" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "moveObjectsToWarehousePool", propOrder = {
    "targetClass",
    "targetOid",
    "objectsClasses",
    "objectsOids",
    "sessionId"
})
public class MoveObjectsToWarehousePool {

    protected String targetClass;
    protected String targetOid;
    @XmlElement(nillable = true)
    protected List<String> objectsClasses;
    @XmlElement(nillable = true)
    protected List<String> objectsOids;
    protected String sessionId;

    /**
     * Gets the value of the targetClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetClass() {
        return targetClass;
    }

    /**
     * Sets the value of the targetClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetClass(String value) {
        this.targetClass = value;
    }

    /**
     * Gets the value of the targetOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetOid() {
        return targetOid;
    }

    /**
     * Sets the value of the targetOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetOid(String value) {
        this.targetOid = value;
    }

    /**
     * Gets the value of the objectsClasses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectsClasses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectsClasses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getObjectsClasses() {
        if (objectsClasses == null) {
            objectsClasses = new ArrayList<String>();
        }
        return this.objectsClasses;
    }

    /**
     * Gets the value of the objectsOids property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectsOids property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectsOids().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getObjectsOids() {
        if (objectsOids == null) {
            objectsOids = new ArrayList<String>();
        }
        return this.objectsOids;
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
