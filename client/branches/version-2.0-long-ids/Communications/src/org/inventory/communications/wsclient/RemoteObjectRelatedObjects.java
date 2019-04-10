
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteObjectRelatedObjects complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteObjectRelatedObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objs" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLight" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="relatedObjects" type="{http://ws.interfaces.kuwaiba.org/}remoteObjectLightList" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteObjectRelatedObjects", propOrder = {
    "objs",
    "relatedObjects"
})
public class RemoteObjectRelatedObjects {

    @XmlElement(nillable = true)
    protected List<RemoteObjectLight> objs;
    @XmlElement(nillable = true)
    protected List<RemoteObjectLightList> relatedObjects;

    /**
     * Gets the value of the objs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteObjectLight }
     * 
     * 
     */
    public List<RemoteObjectLight> getObjs() {
        if (objs == null) {
            objs = new ArrayList<RemoteObjectLight>();
        }
        return this.objs;
    }

    /**
     * Gets the value of the relatedObjects property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relatedObjects property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelatedObjects().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteObjectLightList }
     * 
     * 
     */
    public List<RemoteObjectLightList> getRelatedObjects() {
        if (relatedObjects == null) {
            relatedObjects = new ArrayList<RemoteObjectLightList>();
        }
        return this.relatedObjects;
    }

}
