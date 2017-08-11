
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deleteSDHContainerLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deleteSDHContainerLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="containerLinkClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="containerLinkId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="forceDelete" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
@XmlType(name = "deleteSDHContainerLink", propOrder = {
    "containerLinkClass",
    "containerLinkId",
    "forceDelete",
    "sessionId"
})
public class DeleteSDHContainerLink {

    protected String containerLinkClass;
    protected long containerLinkId;
    protected boolean forceDelete;
    protected String sessionId;

    /**
     * Gets the value of the containerLinkClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContainerLinkClass() {
        return containerLinkClass;
    }

    /**
     * Sets the value of the containerLinkClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContainerLinkClass(String value) {
        this.containerLinkClass = value;
    }

    /**
     * Gets the value of the containerLinkId property.
     * 
     */
    public long getContainerLinkId() {
        return containerLinkId;
    }

    /**
     * Sets the value of the containerLinkId property.
     * 
     */
    public void setContainerLinkId(long value) {
        this.containerLinkId = value;
    }

    /**
     * Gets the value of the forceDelete property.
     * 
     */
    public boolean isForceDelete() {
        return forceDelete;
    }

    /**
     * Sets the value of the forceDelete property.
     * 
     */
    public void setForceDelete(boolean value) {
        this.forceDelete = value;
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
