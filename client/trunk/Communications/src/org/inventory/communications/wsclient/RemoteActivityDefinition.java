
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteActivityDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteActivityDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="idling" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="arfifact" type="{http://ws.interfaces.kuwaiba.org/}remoteArtifactDefinition" minOccurs="0"/>
 *         &lt;element name="actor" type="{http://ws.interfaces.kuwaiba.org/}remoteActor" minOccurs="0"/>
 *         &lt;element name="nextActivity" type="{http://ws.interfaces.kuwaiba.org/}remoteActivityDefinition" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteActivityDefinition", propOrder = {
    "id",
    "name",
    "description",
    "type",
    "idling",
    "arfifact",
    "actor",
    "nextActivity"
})
public class RemoteActivityDefinition {

    protected long id;
    protected String name;
    protected String description;
    protected int type;
    protected boolean idling;
    protected RemoteArtifactDefinition arfifact;
    protected RemoteActor actor;
    protected RemoteActivityDefinition nextActivity;

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

    /**
     * Gets the value of the idling property.
     * 
     */
    public boolean isIdling() {
        return idling;
    }

    /**
     * Sets the value of the idling property.
     * 
     */
    public void setIdling(boolean value) {
        this.idling = value;
    }

    /**
     * Gets the value of the arfifact property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteArtifactDefinition }
     *     
     */
    public RemoteArtifactDefinition getArfifact() {
        return arfifact;
    }

    /**
     * Sets the value of the arfifact property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteArtifactDefinition }
     *     
     */
    public void setArfifact(RemoteArtifactDefinition value) {
        this.arfifact = value;
    }

    /**
     * Gets the value of the actor property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteActor }
     *     
     */
    public RemoteActor getActor() {
        return actor;
    }

    /**
     * Sets the value of the actor property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteActor }
     *     
     */
    public void setActor(RemoteActor value) {
        this.actor = value;
    }

    /**
     * Gets the value of the nextActivity property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteActivityDefinition }
     *     
     */
    public RemoteActivityDefinition getNextActivity() {
        return nextActivity;
    }

    /**
     * Sets the value of the nextActivity property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteActivityDefinition }
     *     
     */
    public void setNextActivity(RemoteActivityDefinition value) {
        this.nextActivity = value;
    }

}
