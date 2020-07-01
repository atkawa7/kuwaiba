
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteContact complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteContact">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ws.northbound.kuwaiba.neotropic.org/}remoteObject">
 *       &lt;sequence>
 *         &lt;element name="customer" type="{http://ws.northbound.kuwaiba.neotropic.org/}remoteObjectLight" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteContact", propOrder = {
    "customer"
})
public class RemoteContact
    extends RemoteObject
{

    protected RemoteObjectLight customer;

    /**
     * Gets the value of the customer property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getCustomer() {
        return customer;
    }

    /**
     * Sets the value of the customer property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setCustomer(RemoteObjectLight value) {
        this.customer = value;
    }

}
