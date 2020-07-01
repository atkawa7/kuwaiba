
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for associateObjectsToEvlan complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="associateObjectsToEvlan">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectClasses" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="objectIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="evlanClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="evlanId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "associateObjectsToEvlan", propOrder = {
    "objectClasses",
    "objectIds",
    "evlanClass",
    "evlanId",
    "sessionId"
})
public class AssociateObjectsToEvlan {

    @XmlElement(nillable = true)
    protected List<String> objectClasses;
    @XmlElement(nillable = true)
    protected List<String> objectIds;
    protected String evlanClass;
    protected String evlanId;
    protected String sessionId;

    /**
     * Gets the value of the objectClasses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectClasses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectClasses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getObjectClasses() {
        if (objectClasses == null) {
            objectClasses = new ArrayList<String>();
        }
        return this.objectClasses;
    }

    /**
     * Gets the value of the objectIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getObjectIds() {
        if (objectIds == null) {
            objectIds = new ArrayList<String>();
        }
        return this.objectIds;
    }

    /**
     * Gets the value of the evlanClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvlanClass() {
        return evlanClass;
    }

    /**
     * Sets the value of the evlanClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvlanClass(String value) {
        this.evlanClass = value;
    }

    /**
     * Gets the value of the evlanId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvlanId() {
        return evlanId;
    }

    /**
     * Sets the value of the evlanId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvlanId(String value) {
        this.evlanId = value;
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
