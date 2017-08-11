
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for connectPhysicalLinks complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="connectPhysicalLinks">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sideAClassNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sideAIds" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="linksClassNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="linksIds" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sideBClassNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="sideBIds" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "connectPhysicalLinks", propOrder = {
    "sideAClassNames",
    "sideAIds",
    "linksClassNames",
    "linksIds",
    "sideBClassNames",
    "sideBIds",
    "sessionId"
})
public class ConnectPhysicalLinks {

    @XmlElement(nillable = true)
    protected List<String> sideAClassNames;
    @XmlElement(nillable = true)
    protected List<Long> sideAIds;
    @XmlElement(nillable = true)
    protected List<String> linksClassNames;
    @XmlElement(nillable = true)
    protected List<Long> linksIds;
    @XmlElement(nillable = true)
    protected List<String> sideBClassNames;
    @XmlElement(nillable = true)
    protected List<Long> sideBIds;
    protected String sessionId;

    /**
     * Gets the value of the sideAClassNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sideAClassNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSideAClassNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSideAClassNames() {
        if (sideAClassNames == null) {
            sideAClassNames = new ArrayList<String>();
        }
        return this.sideAClassNames;
    }

    /**
     * Gets the value of the sideAIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sideAIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSideAIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getSideAIds() {
        if (sideAIds == null) {
            sideAIds = new ArrayList<Long>();
        }
        return this.sideAIds;
    }

    /**
     * Gets the value of the linksClassNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linksClassNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinksClassNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinksClassNames() {
        if (linksClassNames == null) {
            linksClassNames = new ArrayList<String>();
        }
        return this.linksClassNames;
    }

    /**
     * Gets the value of the linksIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linksIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinksIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getLinksIds() {
        if (linksIds == null) {
            linksIds = new ArrayList<Long>();
        }
        return this.linksIds;
    }

    /**
     * Gets the value of the sideBClassNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sideBClassNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSideBClassNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSideBClassNames() {
        if (sideBClassNames == null) {
            sideBClassNames = new ArrayList<String>();
        }
        return this.sideBClassNames;
    }

    /**
     * Gets the value of the sideBIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sideBIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSideBIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getSideBIds() {
        if (sideBIds == null) {
            sideBIds = new ArrayList<Long>();
        }
        return this.sideBIds;
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
