
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for serviceLevelCorrelatedInformation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="serviceLevelCorrelatedInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customer" type="{http://ws.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="services" type="{http://ws.kuwaiba.org/}remoteObjectLight" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "serviceLevelCorrelatedInformation", propOrder = {
    "customer",
    "services"
})
public class ServiceLevelCorrelatedInformation {

    protected RemoteObjectLight customer;
    @XmlElement(nillable = true)
    protected List<RemoteObjectLight> services;

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

    /**
     * Gets the value of the services property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the services property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServices().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteObjectLight }
     * 
     * 
     */
    public List<RemoteObjectLight> getServices() {
        if (services == null) {
            services = new ArrayList<RemoteObjectLight>();
        }
        return this.services;
    }

}
