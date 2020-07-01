
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for updateSynchronizationGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="updateSynchronizationGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="syncGroupId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="syncGroupProperties" type="{http://ws.northbound.kuwaiba.neotropic.org/}stringPair" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "updateSynchronizationGroup", propOrder = {
    "syncGroupId",
    "syncGroupProperties",
    "sessionId"
})
public class UpdateSynchronizationGroup {

    protected long syncGroupId;
    protected List<StringPair> syncGroupProperties;
    protected String sessionId;

    /**
     * Gets the value of the syncGroupId property.
     * 
     */
    public long getSyncGroupId() {
        return syncGroupId;
    }

    /**
     * Sets the value of the syncGroupId property.
     * 
     */
    public void setSyncGroupId(long value) {
        this.syncGroupId = value;
    }

    /**
     * Gets the value of the syncGroupProperties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the syncGroupProperties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSyncGroupProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StringPair }
     * 
     * 
     */
    public List<StringPair> getSyncGroupProperties() {
        if (syncGroupProperties == null) {
            syncGroupProperties = new ArrayList<StringPair>();
        }
        return this.syncGroupProperties;
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
