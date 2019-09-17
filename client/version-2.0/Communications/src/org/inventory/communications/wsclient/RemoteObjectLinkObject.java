
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteObjectLinkObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteObjectLinkObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="connectionObject" type="{http://ws.interfaces.kuwaiba.org/}remoteObject" minOccurs="0"/>
 *         &lt;element name="deviceA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="deviceB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="logicalEndpointObjectA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="logicalEndpointObjectB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="physicalEndpointObjectA" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="physicalEndpointObjectB" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteObjectLinkObject", propOrder = {
    "connectionObject",
    "deviceA",
    "deviceB",
    "logicalEndpointObjectA",
    "logicalEndpointObjectB",
    "physicalEndpointObjectA",
    "physicalEndpointObjectB"
})
public class RemoteObjectLinkObject {

    protected RemoteObject connectionObject;
    protected RemoteObjectLight deviceA;
    protected RemoteObjectLight deviceB;
    protected RemoteObjectLight logicalEndpointObjectA;
    protected RemoteObjectLight logicalEndpointObjectB;
    protected RemoteObjectLight physicalEndpointObjectA;
    protected RemoteObjectLight physicalEndpointObjectB;

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
     * Gets the value of the logicalEndpointObjectA property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getLogicalEndpointObjectA() {
        return logicalEndpointObjectA;
    }

    /**
     * Sets the value of the logicalEndpointObjectA property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setLogicalEndpointObjectA(RemoteObjectLight value) {
        this.logicalEndpointObjectA = value;
    }

    /**
     * Gets the value of the logicalEndpointObjectB property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getLogicalEndpointObjectB() {
        return logicalEndpointObjectB;
    }

    /**
     * Sets the value of the logicalEndpointObjectB property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setLogicalEndpointObjectB(RemoteObjectLight value) {
        this.logicalEndpointObjectB = value;
    }

    /**
     * Gets the value of the physicalEndpointObjectA property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getPhysicalEndpointObjectA() {
        return physicalEndpointObjectA;
    }

    /**
     * Sets the value of the physicalEndpointObjectA property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setPhysicalEndpointObjectA(RemoteObjectLight value) {
        this.physicalEndpointObjectA = value;
    }

    /**
     * Gets the value of the physicalEndpointObjectB property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getPhysicalEndpointObjectB() {
        return physicalEndpointObjectB;
    }

    /**
     * Sets the value of the physicalEndpointObjectB property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setPhysicalEndpointObjectB(RemoteObjectLight value) {
        this.physicalEndpointObjectB = value;
    }

}
