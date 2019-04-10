
package org.inventory.communications.wsclient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createClass complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createClass">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="className" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isAbstract" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isCustom" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isCountable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isInDesign" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="parentClassName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="icon" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="smallIcon" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="color" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
@XmlType(name = "createClass", propOrder = {
    "className",
    "displayName",
    "description",
    "isAbstract",
    "isCustom",
    "isCountable",
    "isInDesign",
    "parentClassName",
    "icon",
    "smallIcon",
    "color",
    "sessionId"
})
public class CreateClass {

    protected String className;
    protected String displayName;
    protected String description;
    protected boolean isAbstract;
    protected boolean isCustom;
    protected boolean isCountable;
    protected boolean isInDesign;
    protected String parentClassName;
    @XmlElementRef(name = "icon", type = JAXBElement.class, required = false)
    protected JAXBElement<byte[]> icon;
    @XmlElementRef(name = "smallIcon", type = JAXBElement.class, required = false)
    protected JAXBElement<byte[]> smallIcon;
    protected int color;
    protected String sessionId;

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
     * Gets the value of the isAbstract property.
     * 
     */
    public boolean isIsAbstract() {
        return isAbstract;
    }

    /**
     * Sets the value of the isAbstract property.
     * 
     */
    public void setIsAbstract(boolean value) {
        this.isAbstract = value;
    }

    /**
     * Gets the value of the isCustom property.
     * 
     */
    public boolean isIsCustom() {
        return isCustom;
    }

    /**
     * Sets the value of the isCustom property.
     * 
     */
    public void setIsCustom(boolean value) {
        this.isCustom = value;
    }

    /**
     * Gets the value of the isCountable property.
     * 
     */
    public boolean isIsCountable() {
        return isCountable;
    }

    /**
     * Sets the value of the isCountable property.
     * 
     */
    public void setIsCountable(boolean value) {
        this.isCountable = value;
    }

    /**
     * Gets the value of the isInDesign property.
     * 
     */
    public boolean isIsInDesign() {
        return isInDesign;
    }

    /**
     * Sets the value of the isInDesign property.
     * 
     */
    public void setIsInDesign(boolean value) {
        this.isInDesign = value;
    }

    /**
     * Gets the value of the parentClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentClassName() {
        return parentClassName;
    }

    /**
     * Sets the value of the parentClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentClassName(String value) {
        this.parentClassName = value;
    }

    /**
     * Gets the value of the icon property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public JAXBElement<byte[]> getIcon() {
        return icon;
    }

    /**
     * Sets the value of the icon property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public void setIcon(JAXBElement<byte[]> value) {
        this.icon = value;
    }

    /**
     * Gets the value of the smallIcon property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public JAXBElement<byte[]> getSmallIcon() {
        return smallIcon;
    }

    /**
     * Sets the value of the smallIcon property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public void setSmallIcon(JAXBElement<byte[]> value) {
        this.smallIcon = value;
    }

    /**
     * Gets the value of the color property.
     * 
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     */
    public void setColor(int value) {
        this.color = value;
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
