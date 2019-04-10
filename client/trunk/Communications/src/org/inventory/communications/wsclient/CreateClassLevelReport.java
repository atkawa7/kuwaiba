
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createClassLevelReport complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createClassLevelReport">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="className" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reportName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reportDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="script" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="outputType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createClassLevelReport", propOrder = {
    "className",
    "reportName",
    "reportDescription",
    "script",
    "outputType",
    "enabled",
    "sessionId"
})
public class CreateClassLevelReport {

    protected String className;
    protected String reportName;
    protected String reportDescription;
    protected String script;
    protected int outputType;
    protected boolean enabled;
    protected String sessionId;

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the reportName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportName() {
        return reportName;
    }

    /**
     * Sets the value of the reportName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportName(String value) {
        this.reportName = value;
    }

    /**
     * Gets the value of the reportDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReportDescription() {
        return reportDescription;
    }

    /**
     * Sets the value of the reportDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReportDescription(String value) {
        this.reportDescription = value;
    }

    /**
     * Gets the value of the script property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScript() {
        return script;
    }

    /**
     * Sets the value of the script property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScript(String value) {
        this.script = value;
    }

    /**
     * Gets the value of the outputType property.
     * 
     */
    public int getOutputType() {
        return outputType;
    }

    /**
     * Sets the value of the outputType property.
     * 
     */
    public void setOutputType(int value) {
        this.outputType = value;
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
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

}
