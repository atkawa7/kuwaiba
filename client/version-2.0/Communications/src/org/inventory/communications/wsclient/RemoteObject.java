
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteObject">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight">
 *       &lt;sequence>
 *         &lt;element name="attributes" type="{http://ws.interfaces.kuwaiba.org/}stringPair" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteObject", propOrder = {
    "attributes"
})
@XmlSeeAlso({
    RemoteContact.class
})
public class RemoteObject
    extends RemoteObjectLight
{

    @XmlElement(nillable = true)
    protected List<StringPair> attributes;

    /**
     * Gets the value of the attributes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StringPair }
     * 
     * 
     */
    public List<StringPair> getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<StringPair>();
        }
        return this.attributes;
    }

}
