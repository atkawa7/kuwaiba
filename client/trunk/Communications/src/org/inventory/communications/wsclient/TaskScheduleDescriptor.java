
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for taskScheduleDescriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="taskScheduleDescriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="everyXMinutes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="executionType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taskScheduleDescriptor", propOrder = {
    "startTime",
    "everyXMinutes",
    "executionType"
})
public class TaskScheduleDescriptor {

    protected long startTime;
    protected int everyXMinutes;
    protected int executionType;

    /**
     * Gets the value of the startTime property.
     * 
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     */
    public void setStartTime(long value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the everyXMinutes property.
     * 
     */
    public int getEveryXMinutes() {
        return everyXMinutes;
    }

    /**
     * Sets the value of the everyXMinutes property.
     * 
     */
    public void setEveryXMinutes(int value) {
        this.everyXMinutes = value;
    }

    /**
     * Gets the value of the executionType property.
     * 
     */
    public int getExecutionType() {
        return executionType;
    }

    /**
     * Sets the value of the executionType property.
     * 
     */
    public void setExecutionType(int value) {
        this.executionType = value;
    }

}
