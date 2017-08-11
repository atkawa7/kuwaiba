
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for removePrivilegeFromGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="removePrivilegeFromGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="groupId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="featureToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "removePrivilegeFromGroup", propOrder = {
    "groupId",
    "featureToken",
    "sessionId"
})
public class RemovePrivilegeFromGroup {

    protected long groupId;
    protected String featureToken;
    protected String sessionId;

    /**
     * Gets the value of the groupId property.
     * 
     */
    public long getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     * 
     */
    public void setGroupId(long value) {
        this.groupId = value;
    }

    /**
     * Gets the value of the featureToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFeatureToken() {
        return featureToken;
    }

    /**
     * Sets the value of the featureToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFeatureToken(String value) {
        this.featureToken = value;
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
