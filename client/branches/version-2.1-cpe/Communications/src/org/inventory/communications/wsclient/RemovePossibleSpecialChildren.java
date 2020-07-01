
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for removePossibleSpecialChildren complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="removePossibleSpecialChildren">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parentClassId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="specialChildrenToBeRemoved" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "removePossibleSpecialChildren", propOrder = {
    "parentClassId",
    "specialChildrenToBeRemoved",
    "sessionId"
})
public class RemovePossibleSpecialChildren {

    protected long parentClassId;
    @XmlElement(nillable = true)
    protected List<Long> specialChildrenToBeRemoved;
    protected String sessionId;

    /**
     * Gets the value of the parentClassId property.
     * 
     */
    public long getParentClassId() {
        return parentClassId;
    }

    /**
     * Sets the value of the parentClassId property.
     * 
     */
    public void setParentClassId(long value) {
        this.parentClassId = value;
    }

    /**
     * Gets the value of the specialChildrenToBeRemoved property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the specialChildrenToBeRemoved property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpecialChildrenToBeRemoved().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getSpecialChildrenToBeRemoved() {
        if (specialChildrenToBeRemoved == null) {
            specialChildrenToBeRemoved = new ArrayList<Long>();
        }
        return this.specialChildrenToBeRemoved;
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
