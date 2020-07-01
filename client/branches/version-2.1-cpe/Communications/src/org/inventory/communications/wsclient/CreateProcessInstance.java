
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createProcessInstance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createProcessInstance">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processDefinitionId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="processInstancename" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="processInstanceDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "createProcessInstance", propOrder = {
    "processDefinitionId",
    "processInstancename",
    "processInstanceDescription",
    "sessionId"
})
public class CreateProcessInstance {

    protected long processDefinitionId;
    protected String processInstancename;
    protected String processInstanceDescription;
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
     * Gets the value of the processInstancename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessInstancename() {
        return processInstancename;
    }

    /**
     * Sets the value of the processInstancename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessInstancename(String value) {
        this.processInstancename = value;
    }

    /**
     * Gets the value of the processInstanceDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessInstanceDescription() {
        return processInstanceDescription;
    }

    /**
     * Sets the value of the processInstanceDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessInstanceDescription(String value) {
        this.processInstanceDescription = value;
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
