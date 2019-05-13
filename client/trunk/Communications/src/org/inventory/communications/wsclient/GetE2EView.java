
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getE2EView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getE2EView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="linkClasses" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="linkIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="includeVLANs" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="includeBDIs" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
@XmlType(name = "getE2EView", propOrder = {
    "linkClasses",
    "linkIds",
    "includeVLANs",
    "includeBDIs",
    "sessionId"
})
public class GetE2EView {

    protected List<String> linkClasses;
    protected List<String> linkIds;
    protected boolean includeVLANs;
    protected boolean includeBDIs;
    protected String sessionId;

    /**
     * Gets the value of the linkClasses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkClasses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkClasses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkClasses() {
        if (linkClasses == null) {
            linkClasses = new ArrayList<String>();
        }
        return this.linkClasses;
    }

    /**
     * Gets the value of the linkIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linkIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinkIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLinkIds() {
        if (linkIds == null) {
            linkIds = new ArrayList<String>();
        }
        return this.linkIds;
    }

    /**
     * Gets the value of the includeVLANs property.
     * 
     */
    public boolean isIncludeVLANs() {
        return includeVLANs;
    }

    /**
     * Sets the value of the includeVLANs property.
     * 
     */
    public void setIncludeVLANs(boolean value) {
        this.includeVLANs = value;
    }

    /**
     * Gets the value of the includeBDIs property.
     * 
     */
    public boolean isIncludeBDIs() {
        return includeBDIs;
    }

    /**
     * Sets the value of the includeBDIs property.
     * 
     */
    public void setIncludeBDIs(boolean value) {
        this.includeBDIs = value;
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
