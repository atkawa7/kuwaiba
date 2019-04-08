
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relatePseudowires complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="relatePseudowires">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pseudoWireId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="interfaceClassName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="interfaceId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "relatePseudowires", propOrder = {
    "pseudoWireId",
    "interfaceClassName",
    "interfaceId",
    "sessionId"
})
public class RelatePseudowires {

    protected String pseudoWireId;
    protected String interfaceClassName;
    protected String interfaceId;
    protected String sessionId;

    /**
     * Gets the value of the pseudoWireId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPseudoWireId() {
        return pseudoWireId;
    }

    /**
     * Sets the value of the pseudoWireId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPseudoWireId(String value) {
        this.pseudoWireId = value;
    }

    /**
     * Gets the value of the interfaceClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInterfaceClassName() {
        return interfaceClassName;
    }

    /**
     * Sets the value of the interfaceClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInterfaceClassName(String value) {
        this.interfaceClassName = value;
    }

    /**
     * Gets the value of the interfaceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Sets the value of the interfaceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInterfaceId(String value) {
        this.interfaceId = value;
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
