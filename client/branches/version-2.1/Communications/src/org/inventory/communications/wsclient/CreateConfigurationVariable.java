
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createConfigurationVariable complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createConfigurationVariable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="configVariablesPoolId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="masked" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="valueDefinition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "createConfigurationVariable", propOrder = {
    "configVariablesPoolId",
    "name",
    "description",
    "type",
    "masked",
    "valueDefinition",
    "sessionId"
})
public class CreateConfigurationVariable {

    protected String configVariablesPoolId;
    protected String name;
    protected String description;
    protected int type;
    protected boolean masked;
    protected String valueDefinition;
    protected String sessionId;

    /**
     * Gets the value of the configVariablesPoolId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfigVariablesPoolId() {
        return configVariablesPoolId;
    }

    /**
     * Sets the value of the configVariablesPoolId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfigVariablesPoolId(String value) {
        this.configVariablesPoolId = value;
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
     * Gets the value of the type property.
     * 
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     */
    public void setType(int value) {
        this.type = value;
    }

    /**
     * Gets the value of the masked property.
     * 
     */
    public boolean isMasked() {
        return masked;
    }

    /**
     * Sets the value of the masked property.
     * 
     */
    public void setMasked(boolean value) {
        this.masked = value;
    }

    /**
     * Gets the value of the valueDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueDefinition() {
        return valueDefinition;
    }

    /**
     * Sets the value of the valueDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueDefinition(String value) {
        this.valueDefinition = value;
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
