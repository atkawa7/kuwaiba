
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for connectMirrorMultiplePort complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="connectMirrorMultiplePort">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="aObjectClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="aObjectId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bObjectClasses" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bObjectIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "connectMirrorMultiplePort", propOrder = {
    "aObjectClass",
    "aObjectId",
    "bObjectClasses",
    "bObjectIds",
    "sessionId"
})
public class ConnectMirrorMultiplePort {

    protected String aObjectClass;
    protected String aObjectId;
    protected List<String> bObjectClasses;
    protected List<String> bObjectIds;
    protected String sessionId;

    /**
     * Gets the value of the aObjectClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAObjectClass() {
        return aObjectClass;
    }

    /**
     * Sets the value of the aObjectClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAObjectClass(String value) {
        this.aObjectClass = value;
    }

    /**
     * Gets the value of the aObjectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAObjectId() {
        return aObjectId;
    }

    /**
     * Sets the value of the aObjectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAObjectId(String value) {
        this.aObjectId = value;
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
