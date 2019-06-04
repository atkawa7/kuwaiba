
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for privilegeInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="privilegeInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="featureToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="accessLevel" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "privilegeInfo", propOrder = {
    "featureToken",
    "accessLevel"
})
public class PrivilegeInfo {

    protected String featureToken;
    protected int accessLevel;

    /**
     * Gets the value of the featureToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFeatureToken() {
        return featureToken;
    }

    /**
     * Sets the value of the featureToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFeatureToken(String value) {
        this.featureToken = value;
    }

    /**
     * Gets the value of the accessLevel property.
     * 
     */
    public int getAccessLevel() {
        return accessLevel;
    }

    /**
     * Sets the value of the accessLevel property.
     * 
     */
    public void setAccessLevel(int value) {
        this.accessLevel = value;
    }

}
