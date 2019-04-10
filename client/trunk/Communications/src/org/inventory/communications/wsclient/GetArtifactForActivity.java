
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getArtifactForActivity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getArtifactForActivity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processinstanceId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="activityId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "getArtifactForActivity", propOrder = {
    "processinstanceId",
    "activityId",
    "sessionId"
})
public class GetArtifactForActivity {

    protected long processinstanceId;
    protected long activityId;
    protected String sessionId;

    /**
     * Gets the value of the processinstanceId property.
     * 
     */
    public long getProcessinstanceId() {
        return processinstanceId;
    }

    /**
     * Sets the value of the processinstanceId property.
     * 
     */
    public void setProcessinstanceId(long value) {
        this.processinstanceId = value;
    }

    /**
     * Gets the value of the activityId property.
     * 
     */
    public long getActivityId() {
        return activityId;
    }

    /**
     * Sets the value of the activityId property.
     * 
     */
    public void setActivityId(long value) {
        this.activityId = value;
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
