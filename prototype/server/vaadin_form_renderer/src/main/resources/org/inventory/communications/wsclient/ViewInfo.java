
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for viewInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="viewInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ws.interfaces.kuwaiba.org/}viewInfoLight">
 *       &lt;sequence>
 *         &lt;element name="background" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="structure" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "viewInfo", propOrder = {
    "background",
    "structure"
})
public class ViewInfo
    extends ViewInfoLight
{

    protected byte[] background;
    protected byte[] structure;

    /**
     * Gets the value of the background property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getBackground() {
        return background;
    }

    /**
     * Sets the value of the background property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setBackground(byte[] value) {
        this.background = value;
    }

    /**
     * Gets the value of the structure property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getStructure() {
        return structure;
    }

    /**
     * Sets the value of the structure property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setStructure(byte[] value) {
        this.structure = value;
    }

}
