
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteProcessDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteProcessDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="creationDate" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="startActivity" type="{http://ws.interfaces.kuwaiba.org/}remoteActivityDefinition" minOccurs="0"/>
 *         &lt;element name="kpis" type="{http://ws.interfaces.kuwaiba.org/}remoteKpi" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="kpiActions" type="{http://ws.interfaces.kuwaiba.org/}remoteKpiAction" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteProcessDefinition", propOrder = {
    "id",
    "name",
    "description",
    "creationDate",
    "version",
    "enabled",
    "startActivity",
    "kpis",
    "kpiActions"
})
public class RemoteProcessDefinition {

    protected long id;
    protected String name;
    protected String description;
    protected long creationDate;
    protected String version;
    protected boolean enabled;
    protected RemoteActivityDefinition startActivity;
    protected List<RemoteKpi> kpis;
    protected List<RemoteKpiAction> kpiActions;

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the creationDate property.
     * 
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of the creationDate property.
     * 
     */
    public void setCreationDate(long value) {
        this.creationDate = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the enabled property.
     * 
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the startActivity property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteActivityDefinition }
     *     
     */
    public RemoteActivityDefinition getStartActivity() {
        return startActivity;
    }

    /**
     * Sets the value of the startActivity property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteActivityDefinition }
     *     
     */
    public void setStartActivity(RemoteActivityDefinition value) {
        this.startActivity = value;
    }

    /**
     * Gets the value of the kpis property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the kpis property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKpis().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteKpi }
     * 
     * 
     */
    public List<RemoteKpi> getKpis() {
        if (kpis == null) {
            kpis = new ArrayList<RemoteKpi>();
        }
        return this.kpis;
    }

    /**
     * Gets the value of the kpiActions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the kpiActions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKpiActions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteKpiAction }
     * 
     * 
     */
    public List<RemoteKpiAction> getKpiActions() {
        if (kpiActions == null) {
            kpiActions = new ArrayList<RemoteKpiAction>();
        }
        return this.kpiActions;
    }

}
