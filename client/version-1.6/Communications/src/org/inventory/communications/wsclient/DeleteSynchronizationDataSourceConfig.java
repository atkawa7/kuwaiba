
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deleteSynchronizationDataSourceConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deleteSynchronizationDataSourceConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="syncDataSourceConfigId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "deleteSynchronizationDataSourceConfig", propOrder = {
    "syncDataSourceConfigId",
    "sessionId"
})
public class DeleteSynchronizationDataSourceConfig {

    protected long syncDataSourceConfigId;
    protected String sessionId;

    /**
     * Gets the value of the syncDataSourceConfigId property.
     * 
     */
    public long getSyncDataSourceConfigId() {
        return syncDataSourceConfigId;
    }

    /**
     * Sets the value of the syncDataSourceConfigId property.
     * 
     */
    public void setSyncDataSourceConfigId(long value) {
        this.syncDataSourceConfigId = value;
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
