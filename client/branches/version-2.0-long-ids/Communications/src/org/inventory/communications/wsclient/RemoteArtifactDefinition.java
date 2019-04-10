
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for remoteArtifactDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteArtifactDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="definition" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="sharedInformation" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="preconditionsScript" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="postconditionsScript" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="printable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="printableTemplate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="externalScripts" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteArtifactDefinition", propOrder = {
    "id",
    "name",
    "description",
    "version",
    "type",
    "definition",
    "sharedInformation",
    "preconditionsScript",
    "postconditionsScript",
    "printable",
    "printableTemplate",
    "externalScripts"
})
public class RemoteArtifactDefinition {

    protected long id;
    protected String name;
    protected String description;
    protected String version;
    protected int type;
    protected byte[] definition;
    @XmlElement(nillable = true)
    protected List<String> sharedInformation;
    protected byte[] preconditionsScript;
    protected byte[] postconditionsScript;
    protected Boolean printable;
    protected String printableTemplate;
    protected String externalScripts;

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
     * Gets the value of the definition property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getDefinition() {
        return definition;
    }

    /**
     * Sets the value of the definition property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setDefinition(byte[] value) {
        this.definition = value;
    }

    /**
     * Gets the value of the sharedInformation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sharedInformation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSharedInformation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSharedInformation() {
        if (sharedInformation == null) {
            sharedInformation = new ArrayList<String>();
        }
        return this.sharedInformation;
    }

    /**
     * Gets the value of the preconditionsScript property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getPreconditionsScript() {
        return preconditionsScript;
    }

    /**
     * Sets the value of the preconditionsScript property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setPreconditionsScript(byte[] value) {
        this.preconditionsScript = value;
    }

    /**
     * Gets the value of the postconditionsScript property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getPostconditionsScript() {
        return postconditionsScript;
    }

    /**
     * Sets the value of the postconditionsScript property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setPostconditionsScript(byte[] value) {
        this.postconditionsScript = value;
    }

    /**
     * Gets the value of the printable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPrintable() {
        return printable;
    }

    /**
     * Sets the value of the printable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPrintable(Boolean value) {
        this.printable = value;
    }

    /**
     * Gets the value of the printableTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrintableTemplate() {
        return printableTemplate;
    }

    /**
     * Sets the value of the printableTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrintableTemplate(String value) {
        this.printableTemplate = value;
    }

    /**
     * Gets the value of the externalScripts property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalScripts() {
        return externalScripts;
    }

    /**
     * Sets the value of the externalScripts property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalScripts(String value) {
        this.externalScripts = value;
    }

}
