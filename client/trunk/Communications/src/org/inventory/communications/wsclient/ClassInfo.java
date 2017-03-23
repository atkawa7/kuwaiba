
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for classInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="classInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ws.kuwaiba.org/}classInfoLight">
 *       &lt;sequence>
 *         &lt;element name="attributeIds" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributeNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributeTypes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributeDisplayNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributesIsVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributesDescription" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="icon" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="creationDate" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="countable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "classInfo", propOrder = {
    "attributeIds",
    "attributeNames",
    "attributeTypes",
    "attributeDisplayNames",
    "attributesIsVisible",
    "attributesDescription",
    "icon",
    "description",
    "creationDate",
    "countable"
})
public class ClassInfo
    extends ClassInfoLight
{

    @XmlElement(nillable = true)
    protected List<Long> attributeIds;
    @XmlElement(nillable = true)
    protected List<String> attributeNames;
    @XmlElement(nillable = true)
    protected List<String> attributeTypes;
    @XmlElement(nillable = true)
    protected List<String> attributeDisplayNames;
    @XmlElement(nillable = true)
    protected List<Boolean> attributesIsVisible;
    @XmlElement(nillable = true)
    protected List<String> attributesDescription;
    protected byte[] icon;
    protected String description;
    protected long creationDate;
    protected Boolean countable;

    /**
     * Gets the value of the attributeIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getAttributeIds() {
        if (attributeIds == null) {
            attributeIds = new ArrayList<Long>();
        }
        return this.attributeIds;
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
     * Gets the value of the attributeTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAttributeTypes() {
        if (attributeTypes == null) {
            attributeTypes = new ArrayList<String>();
        }
        return this.attributeTypes;
    }

    /**
     * Gets the value of the attributeDisplayNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeDisplayNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeDisplayNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAttributeDisplayNames() {
        if (attributeDisplayNames == null) {
            attributeDisplayNames = new ArrayList<String>();
        }
        return this.attributeDisplayNames;
    }

    /**
     * Gets the value of the attributesIsVisible property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributesIsVisible property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributesIsVisible().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getAttributesIsVisible() {
        if (attributesIsVisible == null) {
            attributesIsVisible = new ArrayList<Boolean>();
        }
        return this.attributesIsVisible;
    }

    /**
     * Gets the value of the attributesDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributesDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributesDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAttributesDescription() {
        if (attributesDescription == null) {
            attributesDescription = new ArrayList<String>();
        }
        return this.attributesDescription;
    }

    /**
     * Gets the value of the icon property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getIcon() {
        return icon;
    }

    /**
     * Sets the value of the icon property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setIcon(byte[] value) {
        this.icon = value;
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
     * Gets the value of the creationDate property.
     * 
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of the creationDate property.
     * 
     */
    public void setCreationDate(long value) {
        this.creationDate = value;
    }

    /**
     * Gets the value of the countable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCountable() {
        return countable;
    }

    /**
     * Sets the value of the countable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCountable(Boolean value) {
        this.countable = value;
    }

}
