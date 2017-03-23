
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createService complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createService">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="serviceClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="attributes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributeValues" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "createService", propOrder = {
    "serviceClass",
    "customerClass",
    "customerId",
    "attributes",
    "attributeValues",
    "sessionId"
})
public class CreateService {

    protected String serviceClass;
    protected String customerClass;
    protected long customerId;
    @XmlElement(nillable = true)
    protected List<String> attributes;
    @XmlElement(nillable = true)
    protected List<String> attributeValues;
    protected String sessionId;

    /**
     * Gets the value of the serviceClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceClass() {
        return serviceClass;
    }

    /**
     * Sets the value of the serviceClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceClass(String value) {
        this.serviceClass = value;
    }

    /**
     * Gets the value of the customerClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerClass() {
        return customerClass;
    }

    /**
     * Sets the value of the customerClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerClass(String value) {
        this.customerClass = value;
    }

    /**
     * Gets the value of the customerId property.
     * 
     */
    public long getCustomerId() {
        return customerId;
    }

    /**
     * Sets the value of the customerId property.
     * 
     */
    public void setCustomerId(long value) {
        this.customerId = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<String>();
        }
        return this.attributes;
    }

    /**
     * Gets the value of the attributeValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAttributeValues() {
        if (attributeValues == null) {
            attributeValues = new ArrayList<String>();
        }
        return this.attributeValues;
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
