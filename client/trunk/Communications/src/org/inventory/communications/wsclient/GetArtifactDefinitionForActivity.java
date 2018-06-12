
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getArtifactDefinitionForActivity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getArtifactDefinitionForActivity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processDefinitionId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="activityDefinitionId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "getArtifactDefinitionForActivity", propOrder = {
    "processDefinitionId",
    "activityDefinitionId",
    "sessionId"
})
public class GetArtifactDefinitionForActivity {

    protected long processDefinitionId;
    protected long activityDefinitionId;
    protected String sessionId;

    /**
     * Gets the value of the processDefinitionId property.
     * 
     */
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    /**
     * Sets the value of the processDefinitionId property.
     * 
     */
    public void setProcessDefinitionId(long value) {
        this.processDefinitionId = value;
    }

    /**
     * Gets the value of the activityDefinitionId property.
     * 
     */
    public long getActivityDefinitionId() {
        return activityDefinitionId;
    }

    /**
     * Sets the value of the activityDefinitionId property.
     * 
     */
    public void setActivityDefinitionId(long value) {
        this.activityDefinitionId = value;
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
