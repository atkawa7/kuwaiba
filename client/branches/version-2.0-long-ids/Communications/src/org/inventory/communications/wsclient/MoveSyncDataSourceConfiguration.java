
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for moveSyncDataSourceConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="moveSyncDataSourceConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oldSyncGroupId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="newSyncGroupId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="syncDataSourceConfiguration" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "moveSyncDataSourceConfiguration", propOrder = {
    "oldSyncGroupId",
    "newSyncGroupId",
    "syncDataSourceConfiguration",
    "sessionId"
})
public class MoveSyncDataSourceConfiguration {

    protected long oldSyncGroupId;
    protected long newSyncGroupId;
    @XmlElement(nillable = true)
    protected List<Long> syncDataSourceConfiguration;
    protected String sessionId;

    /**
     * Gets the value of the oldSyncGroupId property.
     * 
     */
    public long getOldSyncGroupId() {
        return oldSyncGroupId;
    }

    /**
     * Sets the value of the oldSyncGroupId property.
     * 
     */
    public void setOldSyncGroupId(long value) {
        this.oldSyncGroupId = value;
    }

    /**
     * Gets the value of the newSyncGroupId property.
     * 
     */
    public long getNewSyncGroupId() {
        return newSyncGroupId;
    }

    /**
     * Sets the value of the newSyncGroupId property.
     * 
     */
    public void setNewSyncGroupId(long value) {
        this.newSyncGroupId = value;
    }

    /**
     * Gets the value of the syncDataSourceConfiguration property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the syncDataSourceConfiguration property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSyncDataSourceConfiguration().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getSyncDataSourceConfiguration() {
        if (syncDataSourceConfiguration == null) {
            syncDataSourceConfiguration = new ArrayList<Long>();
        }
        return this.syncDataSourceConfiguration;
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
