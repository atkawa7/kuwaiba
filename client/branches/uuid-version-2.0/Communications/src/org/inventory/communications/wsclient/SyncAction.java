
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for syncAction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="syncAction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="finding" type="{http://ws.interfaces.kuwaiba.org/}syncFinding" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "syncAction", propOrder = {
    "finding",
    "type"
})
public class SyncAction {

    protected SyncFinding finding;
    protected int type;

    /**
     * Gets the value of the finding property.
     * 
     * @return
     *     possible object is
     *     {@link SyncFinding }
     *     
     */
    public SyncFinding getFinding() {
        return finding;
    }

    /**
     * Sets the value of the finding property.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncFinding }
     *     
     */
    public void setFinding(SyncFinding value) {
        this.finding = value;
    }

    /**
     * Gets the value of the type property.
     * 
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     */
    public void setType(int value) {
        this.type = value;
    }

}
