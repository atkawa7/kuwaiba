
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for launchAutomatedSynchronizationTask complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="launchAutomatedSynchronizationTask">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="syncGroupId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="providersName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "launchAutomatedSynchronizationTask", propOrder = {
    "syncGroupId",
    "providersName",
    "sessionId"
})
public class LaunchAutomatedSynchronizationTask {

    protected long syncGroupId;
    protected String providersName;
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
     * Gets the value of the providersName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProvidersName() {
        return providersName;
    }

    /**
     * Sets the value of the providersName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProvidersName(String value) {
        this.providersName = value;
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
