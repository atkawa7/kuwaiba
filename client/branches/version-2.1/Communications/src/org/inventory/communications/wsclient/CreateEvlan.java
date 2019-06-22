
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createEvlan complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createEvlan">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="evlansPoolId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="evlanAttrNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="evlanAttrValues" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "createEvlan", propOrder = {
    "evlansPoolId",
    "evlanAttrNames",
    "evlanAttrValues",
    "sessionId"
})
public class CreateEvlan {

    protected String evlansPoolId;
    @XmlElement(nillable = true)
    protected List<String> evlanAttrNames;
    @XmlElement(nillable = true)
    protected List<String> evlanAttrValues;
    protected String sessionId;

    /**
     * Gets the value of the evlansPoolId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvlansPoolId() {
        return evlansPoolId;
    }

    /**
     * Sets the value of the evlansPoolId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvlansPoolId(String value) {
        this.evlansPoolId = value;
    }

    /**
     * Gets the value of the evlanAttrNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the evlanAttrNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEvlanAttrNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getEvlanAttrNames() {
        if (evlanAttrNames == null) {
            evlanAttrNames = new ArrayList<String>();
        }
        return this.evlanAttrNames;
    }

    /**
     * Gets the value of the evlanAttrValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the evlanAttrValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEvlanAttrValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getEvlanAttrValues() {
        if (evlanAttrValues == null) {
            evlanAttrValues = new ArrayList<String>();
        }
        return this.evlanAttrValues;
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
