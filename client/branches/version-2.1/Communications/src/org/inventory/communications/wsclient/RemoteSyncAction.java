
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteSyncAction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteSyncAction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="finding" type="{http://ws.northbound.kuwaiba.neotropic.org/}remoteSyncFinding" minOccurs="0"/>
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
@XmlType(name = "remoteSyncAction", propOrder = {
    "finding",
    "type"
})
public class RemoteSyncAction {

    protected RemoteSyncFinding finding;
    protected int type;

    /**
     * Gets the value of the finding property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteSyncFinding }
     *     
     */
    public RemoteSyncFinding getFinding() {
        return finding;
    }

    /**
     * Sets the value of the finding property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteSyncFinding }
     *     
     */
    public void setFinding(RemoteSyncFinding value) {
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
