
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
 *     &lt;extension base="{http://ws.interfaces.kuwaiba.org/}groupInfoLight">
 *       &lt;sequence>
 *         &lt;element name="users" type="{http://ws.interfaces.kuwaiba.org/}userInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="privileges" type="{http://ws.interfaces.kuwaiba.org/}privilegeInfo" maxOccurs="unbounded" minOccurs="0"/>
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
    "privileges"
})
public class GroupInfo
    extends GroupInfoLight
{

    @XmlElement(nillable = true)
    protected List<UserInfo> users;
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
