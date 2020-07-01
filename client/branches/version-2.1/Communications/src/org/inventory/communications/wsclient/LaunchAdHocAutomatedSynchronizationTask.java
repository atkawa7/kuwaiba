
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for launchAdHocAutomatedSynchronizationTask complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="launchAdHocAutomatedSynchronizationTask">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="synDsConfigIds" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "launchAdHocAutomatedSynchronizationTask", propOrder = {
    "synDsConfigIds",
    "providersName",
    "sessionId"
})
public class LaunchAdHocAutomatedSynchronizationTask {

    @XmlElement(type = Long.class)
    protected List<Long> synDsConfigIds;
    protected String providersName;
    protected String sessionId;

    /**
     * Gets the value of the synDsConfigIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the synDsConfigIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSynDsConfigIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getSynDsConfigIds() {
        if (synDsConfigIds == null) {
            synDsConfigIds = new ArrayList<Long>();
        }
        return this.synDsConfigIds;
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
