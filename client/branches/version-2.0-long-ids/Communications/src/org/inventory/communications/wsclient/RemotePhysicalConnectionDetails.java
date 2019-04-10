
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remotePhysicalConnectionDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remotePhysicalConnectionDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionObject" type="{http://ws.interfaces.kuwaiba.org/}remoteObject" minOccurs="0"/>
 *         &lt;element name="physicalPathForEndpointA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="physicalPathForEndpointB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remotePhysicalConnectionDetails", propOrder = {
    "connectionObject",
    "physicalPathForEndpointA",
    "physicalPathForEndpointB"
})
public class RemotePhysicalConnectionDetails {

    protected RemoteObject connectionObject;
    @XmlElement(nillable = true)
    protected List<RemoteObjectLight> physicalPathForEndpointA;
    @XmlElement(nillable = true)
    protected List<RemoteObjectLight> physicalPathForEndpointB;

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

}
