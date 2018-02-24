
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getQueryResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getQueryResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://ws.interfaces.kuwaiba.org/}remoteQuery" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getQueryResponse", propOrder = {
    "_return"
})
public class GetQueryResponse {

    @XmlElement(name = "return")
    protected RemoteQuery _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteQuery }
     *     
     */
    public RemoteQuery getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteQuery }
     *     
     */
    public void setReturn(RemoteQuery value) {
        this._return = value;
    }

}
