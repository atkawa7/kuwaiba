
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for setAttributePropertiesForClassWithId complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="setAttributePropertiesForClassWithId">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="classId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="attributeId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isAdministrative" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="isVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="isReadOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="noCopy" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="isUnique" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
@XmlType(name = "setAttributePropertiesForClassWithId", propOrder = {
    "classId",
    "attributeId",
    "name",
    "displayName",
    "type",
    "description",
    "isAdministrative",
    "isVisible",
    "isReadOnly",
    "noCopy",
    "isUnique",
    "sessionId"
})
public class SetAttributePropertiesForClassWithId {

    protected long classId;
    protected long attributeId;
    protected String name;
    protected String displayName;
    protected String type;
    protected String description;
    protected Boolean isAdministrative;
    protected Boolean isVisible;
    protected Boolean isReadOnly;
    protected Boolean noCopy;
    protected Boolean isUnique;
    protected String sessionId;

    /**
     * Gets the value of the classId property.
     * 
     */
    public long getClassId() {
        return classId;
    }

    /**
     * Sets the value of the classId property.
     * 
     */
    public void setClassId(long value) {
        this.classId = value;
    }

    /**
     * Gets the value of the attributeId property.
     * 
     */
    public long getAttributeId() {
        return attributeId;
    }

    /**
     * Sets the value of the attributeId property.
     * 
     */
    public void setAttributeId(long value) {
        this.attributeId = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the isAdministrative property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsAdministrative() {
        return isAdministrative;
    }

    /**
     * Sets the value of the isAdministrative property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsAdministrative(Boolean value) {
        this.isAdministrative = value;
    }

    /**
     * Gets the value of the isVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsVisible() {
        return isVisible;
    }

    /**
     * Sets the value of the isVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsVisible(Boolean value) {
        this.isVisible = value;
    }

    /**
     * Gets the value of the isReadOnly property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsReadOnly() {
        return isReadOnly;
    }

    /**
     * Sets the value of the isReadOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsReadOnly(Boolean value) {
        this.isReadOnly = value;
    }

    /**
     * Gets the value of the noCopy property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNoCopy() {
        return noCopy;
    }

    /**
     * Sets the value of the noCopy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoCopy(Boolean value) {
        this.noCopy = value;
    }

    /**
     * Gets the value of the isUnique property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsUnique() {
        return isUnique;
    }

    /**
     * Sets the value of the isUnique property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsUnique(Boolean value) {
        this.isUnique = value;
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
