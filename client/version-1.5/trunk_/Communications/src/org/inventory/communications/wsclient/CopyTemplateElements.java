
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for copyTemplateElements complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="copyTemplateElements">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sourceObjectsClassNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sourceObjectsIds" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="newParentClassName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newParentId" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
@XmlType(name = "copyTemplateElements", propOrder = {
    "sourceObjectsClassNames",
    "sourceObjectsIds",
    "newParentClassName",
    "newParentId",
    "sessionId"
})
public class CopyTemplateElements {

    @XmlElement(nillable = true)
    protected List<String> sourceObjectsClassNames;
    @XmlElement(nillable = true)
    protected List<Long> sourceObjectsIds;
    protected String newParentClassName;
    protected long newParentId;
    protected String sessionId;

    /**
     * Gets the value of the sourceObjectsClassNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sourceObjectsClassNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSourceObjectsClassNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSourceObjectsClassNames() {
        if (sourceObjectsClassNames == null) {
            sourceObjectsClassNames = new ArrayList<String>();
        }
        return this.sourceObjectsClassNames;
    }

    /**
     * Gets the value of the sourceObjectsIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sourceObjectsIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSourceObjectsIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getSourceObjectsIds() {
        if (sourceObjectsIds == null) {
            sourceObjectsIds = new ArrayList<Long>();
        }
        return this.sourceObjectsIds;
    }

    /**
     * Gets the value of the newParentClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewParentClassName() {
        return newParentClassName;
    }

    /**
     * Sets the value of the newParentClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewParentClassName(String value) {
        this.newParentClassName = value;
    }

    /**
     * Gets the value of the newParentId property.
     * 
     */
    public long getNewParentId() {
        return newParentId;
    }

    /**
     * Sets the value of the newParentId property.
     * 
     */
    public void setNewParentId(long value) {
        this.newParentId = value;
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
