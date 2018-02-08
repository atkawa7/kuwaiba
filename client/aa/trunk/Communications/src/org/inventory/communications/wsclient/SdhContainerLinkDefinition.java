
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sdhContainerLinkDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sdhContainerLinkDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="container" type="{http://ws.kuwaiba.org/}remoteBusinessObjectLight" minOccurs="0"/>
 *         &lt;element name="structured" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="positions" type="{http://ws.kuwaiba.org/}sdhPosition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sdhContainerLinkDefinition", propOrder = {
    "container",
    "structured",
    "positions"
})
public class SdhContainerLinkDefinition {

    protected RemoteBusinessObjectLight container;
    protected boolean structured;
    @XmlElement(nillable = true)
    protected List<SdhPosition> positions;

    /**
     * Gets the value of the container property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteBusinessObjectLight }
     *     
     */
    public RemoteBusinessObjectLight getContainer() {
        return container;
    }

    /**
     * Sets the value of the container property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteBusinessObjectLight }
     *     
     */
    public void setContainer(RemoteBusinessObjectLight value) {
        this.container = value;
    }

    /**
     * Gets the value of the structured property.
     * 
     */
    public boolean isStructured() {
        return structured;
    }

    /**
     * Sets the value of the structured property.
     * 
     */
    public void setStructured(boolean value) {
        this.structured = value;
    }

    /**
     * Gets the value of the positions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the positions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPositions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SdhPosition }
     * 
     * 
     */
    public List<SdhPosition> getPositions() {
        if (positions == null) {
            positions = new ArrayList<SdhPosition>();
        }
        return this.positions;
    }

}
