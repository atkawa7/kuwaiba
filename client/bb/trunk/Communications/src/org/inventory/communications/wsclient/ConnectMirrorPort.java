
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for connectMirrorPort complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="connectMirrorPort">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="aObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="aObjectId" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bObjectId" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "connectMirrorPort", propOrder = {
    "aObjectClass",
    "aObjectId",
    "bObjectClass",
    "bObjectId",
    "sessionId"
})
public class ConnectMirrorPort {

    @XmlElement(nillable = true)
    protected List<String> aObjectClass;
    @XmlElement(nillable = true)
    protected List<Long> aObjectId;
    @XmlElement(nillable = true)
    protected List<String> bObjectClass;
    @XmlElement(nillable = true)
    protected List<Long> bObjectId;
    protected String sessionId;

    /**
     * Gets the value of the aObjectClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aObjectClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAObjectClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAObjectClass() {
        if (aObjectClass == null) {
            aObjectClass = new ArrayList<String>();
        }
        return this.aObjectClass;
    }

    /**
     * Gets the value of the aObjectId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aObjectId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAObjectId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getAObjectId() {
        if (aObjectId == null) {
            aObjectId = new ArrayList<Long>();
        }
        return this.aObjectId;
    }

    /**
     * Gets the value of the bObjectClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bObjectClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBObjectClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBObjectClass() {
        if (bObjectClass == null) {
            bObjectClass = new ArrayList<String>();
        }
        return this.bObjectClass;
    }

    /**
     * Gets the value of the bObjectId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bObjectId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBObjectId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getBObjectId() {
        if (bObjectId == null) {
            bObjectId = new ArrayList<Long>();
        }
        return this.bObjectId;
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
