
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for assetLevelCorrelatedInformation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="assetLevelCorrelatedInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="inventoryObjects" type="{http://ws.kuwaiba.org/}remoteObject" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="services" type="{http://ws.kuwaiba.org/}serviceLevelCorrelatedInformation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assetLevelCorrelatedInformation", propOrder = {
    "inventoryObjects",
    "services"
})
public class AssetLevelCorrelatedInformation {

    @XmlElement(nillable = true)
    protected List<RemoteObject> inventoryObjects;
    @XmlElement(nillable = true)
    protected List<ServiceLevelCorrelatedInformation> services;

    /**
     * Gets the value of the inventoryObjects property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inventoryObjects property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInventoryObjects().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteObject }
     * 
     * 
     */
    public List<RemoteObject> getInventoryObjects() {
        if (inventoryObjects == null) {
            inventoryObjects = new ArrayList<RemoteObject>();
        }
        return this.inventoryObjects;
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
     * {@link ServiceLevelCorrelatedInformation }
     * 
     * 
     */
    public List<ServiceLevelCorrelatedInformation> getServices() {
        if (services == null) {
            services = new ArrayList<ServiceLevelCorrelatedInformation>();
        }
        return this.services;
    }

}
