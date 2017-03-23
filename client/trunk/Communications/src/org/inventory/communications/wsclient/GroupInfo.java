
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for groupInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="groupInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ws.kuwaiba.org/}groupInfoLight">
 *       &lt;sequence>
 *         &lt;element name="users" type="{http://ws.kuwaiba.org/}userInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="creationDate" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="privileges" type="{http://ws.kuwaiba.org/}privilegeInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "groupInfo", propOrder = {
    "users",
    "creationDate",
    "description",
    "privileges"
})
public class GroupInfo
    extends GroupInfoLight
{

    @XmlElement(nillable = true)
    protected List<UserInfo> users;
    protected long creationDate;
    protected String description;
    @XmlElement(nillable = true)
    protected List<PrivilegeInfo> privileges;

    /**
     * Gets the value of the users property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the users property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUsers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserInfo }
     * 
     * 
     */
    public List<UserInfo> getUsers() {
        if (users == null) {
            users = new ArrayList<UserInfo>();
        }
        return this.users;
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
     * Gets the value of the privileges property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the privileges property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPrivileges().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PrivilegeInfo }
     * 
     * 
     */
    public List<PrivilegeInfo> getPrivileges() {
        if (privileges == null) {
            privileges = new ArrayList<PrivilegeInfo>();
        }
        return this.privileges;
    }

}
