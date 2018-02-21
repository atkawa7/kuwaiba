
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for resultRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resultRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="object" type="{http://ws.kuwaiba.org/}remoteObjectLight" minOccurs="0"/>
 *         &lt;element name="extraColumns" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resultRecord", propOrder = {
    "object",
    "extraColumns"
})
public class ResultRecord {

    protected RemoteObjectLight object;
    @XmlElement(nillable = true)
    protected List<String> extraColumns;

    /**
     * Gets the value of the object property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteObjectLight }
     *     
     */
    public RemoteObjectLight getObject() {
        return object;
    }

    /**
     * Sets the value of the object property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteObjectLight }
     *     
     */
    public void setObject(RemoteObjectLight value) {
        this.object = value;
    }

    /**
     * Gets the value of the extraColumns property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extraColumns property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtraColumns().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getExtraColumns() {
        if (extraColumns == null) {
            extraColumns = new ArrayList<String>();
        }
        return this.extraColumns;
    }

}
