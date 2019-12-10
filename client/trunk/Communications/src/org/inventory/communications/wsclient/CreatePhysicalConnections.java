
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createPhysicalConnections complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createPhysicalConnections">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="aObjectClasses" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="aObjectIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bObjectClasses" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bObjectIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="connectionClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="templateId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "createPhysicalConnections", propOrder = {
    "aObjectClasses",
    "aObjectIds",
    "bObjectClasses",
    "bObjectIds",
    "name",
    "connectionClass",
    "templateId",
    "sessionId"
})
public class CreatePhysicalConnections {

    @XmlElement(nillable = true)
    protected List<String> aObjectClasses;
    @XmlElement(nillable = true)
    protected List<String> aObjectIds;
    @XmlElement(nillable = true)
    protected List<String> bObjectClasses;
    @XmlElement(nillable = true)
    protected List<String> bObjectIds;
    protected String name;
    protected String connectionClass;
    protected String templateId;
    protected String sessionId;

    /**
     * Gets the value of the aObjectClasses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aObjectClasses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAObjectClasses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAObjectClasses() {
        if (aObjectClasses == null) {
            aObjectClasses = new ArrayList<String>();
        }
        return this.aObjectClasses;
    }

    /**
     * Gets the value of the aObjectIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aObjectIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAObjectIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAObjectIds() {
        if (aObjectIds == null) {
            aObjectIds = new ArrayList<String>();
        }
        return this.aObjectIds;
    }

    /**
     * Gets the value of the bObjectClasses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bObjectClasses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBObjectClasses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBObjectClasses() {
        if (bObjectClasses == null) {
            bObjectClasses = new ArrayList<String>();
        }
        return this.bObjectClasses;
    }

    /**
     * Gets the value of the bObjectIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bObjectIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBObjectIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBObjectIds() {
        if (bObjectIds == null) {
            bObjectIds = new ArrayList<String>();
        }
        return this.bObjectIds;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the connectionClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionClass() {
        return connectionClass;
    }

    /**
     * Sets the value of the connectionClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionClass(String value) {
        this.connectionClass = value;
    }

    /**
     * Gets the value of the templateId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * Sets the value of the templateId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateId(String value) {
        this.templateId = value;
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
