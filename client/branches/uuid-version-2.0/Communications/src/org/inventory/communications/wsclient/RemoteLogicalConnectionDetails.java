
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteLogicalConnectionDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteLogicalConnectionDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionObject" type="{http://ws.interfaces.kuwaiba.org/}remoteObject" minOccurs="0"/>
 *         &lt;element name="endpointA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="endpointB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="physicalPathForEndpointA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="physicalPathForEndpointB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="physicalPathForVlansEndpointA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectRelatedObjects" minOccurs="0"/>
 *         &lt;element name="physicalPathForVlansEndpointB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectRelatedObjects" minOccurs="0"/>
 *         &lt;element name="physicalPathForBDisEndpointA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectRelatedObjects" minOccurs="0"/>
 *         &lt;element name="physicalPathForBDisEndpointB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectRelatedObjects" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteLogicalConnectionDetails", propOrder = {
    "connectionObject",
    "endpointA",
    "endpointB",
    "physicalPathForEndpointA",
    "physicalPathForEndpointB",
    "physicalPathForVlansEndpointA",
    "physicalPathForVlansEndpointB",
    "physicalPathForBDisEndpointA",
    "physicalPathForBDisEndpointB"
})
public class RemoteLogicalConnectionDetails {

    protected RemoteObject connectionObject;
    protected RemoteObjectLight endpointA;
    protected RemoteObjectLight endpointB;
    @XmlElement(nillable = true)
    protected List<RemoteObjectLight> physicalPathForEndpointA;
    @XmlElement(nillable = true)
    protected List<RemoteObjectLight> physicalPathForEndpointB;
    protected RemoteObjectRelatedObjects physicalPathForVlansEndpointA;
    protected RemoteObjectRelatedObjects physicalPathForVlansEndpointB;
    protected RemoteObjectRelatedObjects physicalPathForBDisEndpointA;
    protected RemoteObjectRelatedObjects physicalPathForBDisEndpointB;

    /**
     * Gets the value of the connectionObject property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObject }
     *     
     */
    public RemoteObject getConnectionObject() {
        return connectionObject;
    }

    /**
     * Sets the value of the connectionObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObject }
     *     
     */
    public void setConnectionObject(RemoteObject value) {
        this.connectionObject = value;
    }

    /**
     * Gets the value of the endpointA property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getEndpointA() {
        return endpointA;
    }

    /**
     * Sets the value of the endpointA property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setEndpointA(RemoteObjectLight value) {
        this.endpointA = value;
    }

    /**
     * Gets the value of the endpointB property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getEndpointB() {
        return endpointB;
    }

    /**
     * Sets the value of the endpointB property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setEndpointB(RemoteObjectLight value) {
        this.endpointB = value;
    }

    /**
     * Gets the value of the physicalPathForEndpointA property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the physicalPathForEndpointA property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhysicalPathForEndpointA().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteObjectLight }
     * 
     * 
     */
    public List<RemoteObjectLight> getPhysicalPathForEndpointA() {
        if (physicalPathForEndpointA == null) {
            physicalPathForEndpointA = new ArrayList<RemoteObjectLight>();
        }
        return this.physicalPathForEndpointA;
    }

    /**
     * Gets the value of the physicalPathForEndpointB property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the physicalPathForEndpointB property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhysicalPathForEndpointB().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteObjectLight }
     * 
     * 
     */
    public List<RemoteObjectLight> getPhysicalPathForEndpointB() {
        if (physicalPathForEndpointB == null) {
            physicalPathForEndpointB = new ArrayList<RemoteObjectLight>();
        }
        return this.physicalPathForEndpointB;
    }

    /**
     * Gets the value of the physicalPathForVlansEndpointA property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectRelatedObjects }
     *     
     */
    public RemoteObjectRelatedObjects getPhysicalPathForVlansEndpointA() {
        return physicalPathForVlansEndpointA;
    }

    /**
     * Sets the value of the physicalPathForVlansEndpointA property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectRelatedObjects }
     *     
     */
    public void setPhysicalPathForVlansEndpointA(RemoteObjectRelatedObjects value) {
        this.physicalPathForVlansEndpointA = value;
    }

    /**
     * Gets the value of the physicalPathForVlansEndpointB property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectRelatedObjects }
     *     
     */
    public RemoteObjectRelatedObjects getPhysicalPathForVlansEndpointB() {
        return physicalPathForVlansEndpointB;
    }

    /**
     * Sets the value of the physicalPathForVlansEndpointB property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectRelatedObjects }
     *     
     */
    public void setPhysicalPathForVlansEndpointB(RemoteObjectRelatedObjects value) {
        this.physicalPathForVlansEndpointB = value;
    }

    /**
     * Gets the value of the physicalPathForBDisEndpointA property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectRelatedObjects }
     *     
     */
    public RemoteObjectRelatedObjects getPhysicalPathForBDisEndpointA() {
        return physicalPathForBDisEndpointA;
    }

    /**
     * Sets the value of the physicalPathForBDisEndpointA property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectRelatedObjects }
     *     
     */
    public void setPhysicalPathForBDisEndpointA(RemoteObjectRelatedObjects value) {
        this.physicalPathForBDisEndpointA = value;
    }

    /**
     * Gets the value of the physicalPathForBDisEndpointB property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectRelatedObjects }
     *     
     */
    public RemoteObjectRelatedObjects getPhysicalPathForBDisEndpointB() {
        return physicalPathForBDisEndpointB;
    }

    /**
     * Sets the value of the physicalPathForBDisEndpointB property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectRelatedObjects }
     *     
     */
    public void setPhysicalPathForBDisEndpointB(RemoteObjectRelatedObjects value) {
        this.physicalPathForBDisEndpointB = value;
    }

}
