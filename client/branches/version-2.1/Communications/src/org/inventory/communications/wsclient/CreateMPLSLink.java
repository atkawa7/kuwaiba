
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createMPLSLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createMPLSLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="classNameEndpointA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="idEndpointA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="classNameEndpointB" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="idEndpointB" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parameters" type="{http://ws.northbound.kuwaiba.neotropic.org/}stringPair" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "createMPLSLink", propOrder = {
    "classNameEndpointA",
    "idEndpointA",
    "classNameEndpointB",
    "idEndpointB",
    "parameters",
    "sessionId"
})
public class CreateMPLSLink {

    protected String classNameEndpointA;
    protected String idEndpointA;
    protected String classNameEndpointB;
    protected String idEndpointB;
    protected List<StringPair> parameters;
    protected String sessionId;

    /**
     * Gets the value of the classNameEndpointA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassNameEndpointA() {
        return classNameEndpointA;
    }

    /**
     * Sets the value of the classNameEndpointA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassNameEndpointA(String value) {
        this.classNameEndpointA = value;
    }

    /**
     * Gets the value of the idEndpointA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdEndpointA() {
        return idEndpointA;
    }

    /**
     * Sets the value of the idEndpointA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdEndpointA(String value) {
        this.idEndpointA = value;
    }

    /**
     * Gets the value of the classNameEndpointB property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassNameEndpointB() {
        return classNameEndpointB;
    }

    /**
     * Sets the value of the classNameEndpointB property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassNameEndpointB(String value) {
        this.classNameEndpointB = value;
    }

    /**
     * Gets the value of the idEndpointB property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdEndpointB() {
        return idEndpointB;
    }

    /**
     * Sets the value of the idEndpointB property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdEndpointB(String value) {
        this.idEndpointB = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StringPair }
     * 
     * 
     */
    public List<StringPair> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<StringPair>();
        }
        return this.parameters;
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
