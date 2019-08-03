
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getChildrenOfClassLight complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getChildrenOfClassLight">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parentOid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parentClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="childrenClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maxResults" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
@XmlType(name = "getChildrenOfClassLight", propOrder = {
    "parentOid",
    "parentClass",
    "childrenClass",
    "maxResults",
    "sessionId"
})
public class GetChildrenOfClassLight {

    protected String parentOid;
    protected String parentClass;
    protected String childrenClass;
    protected int maxResults;
    protected String sessionId;

    /**
     * Gets the value of the parentOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentOid() {
        return parentOid;
    }

    /**
     * Sets the value of the parentOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentOid(String value) {
        this.parentOid = value;
    }

    /**
     * Gets the value of the parentClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentClass() {
        return parentClass;
    }

    /**
     * Sets the value of the parentClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentClass(String value) {
        this.parentClass = value;
    }

    /**
     * Gets the value of the childrenClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChildrenClass() {
        return childrenClass;
    }

    /**
     * Sets the value of the childrenClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChildrenClass(String value) {
        this.childrenClass = value;
    }

    /**
     * Gets the value of the maxResults property.
     * 
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the value of the maxResults property.
     * 
     */
    public void setMaxResults(int value) {
        this.maxResults = value;
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
