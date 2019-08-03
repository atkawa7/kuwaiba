
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteMPLSConnectionDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteMPLSConnectionDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionObject" type="{http://ws.interfaces.kuwaiba.org/}remoteObject" minOccurs="0"/>
 *         &lt;element name="endpointA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="deviceA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="endpointB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="deviceB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="vfiA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="vfiB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteMPLSConnectionDetails", propOrder = {
    "connectionObject",
    "endpointA",
    "deviceA",
    "endpointB",
    "deviceB",
    "vfiA",
    "vfiB"
})
public class RemoteMPLSConnectionDetails {

    protected RemoteObject connectionObject;
    protected RemoteObjectLight endpointA;
    protected RemoteObjectLight deviceA;
    protected RemoteObjectLight endpointB;
    protected RemoteObjectLight deviceB;
    protected RemoteObjectLight vfiA;
    protected RemoteObjectLight vfiB;

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
     * Gets the value of the deviceA property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getDeviceA() {
        return deviceA;
    }

    /**
     * Sets the value of the deviceA property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setDeviceA(RemoteObjectLight value) {
        this.deviceA = value;
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
     * Gets the value of the deviceB property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getDeviceB() {
        return deviceB;
    }

    /**
     * Sets the value of the deviceB property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setDeviceB(RemoteObjectLight value) {
        this.deviceB = value;
    }

    /**
     * Gets the value of the vfiA property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getVfiA() {
        return vfiA;
    }

    /**
     * Sets the value of the vfiA property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setVfiA(RemoteObjectLight value) {
        this.vfiA = value;
    }

    /**
     * Gets the value of the vfiB property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getVfiB() {
        return vfiB;
    }

    /**
     * Sets the value of the vfiB property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setVfiB(RemoteObjectLight value) {
        this.vfiB = value;
    }

}
