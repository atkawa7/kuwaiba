
package org.inventory.communications.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for transientQuery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="transientQuery">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="className" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="logicalConnector" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="attributeNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="visibleAttributeNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attributeValues" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="conditions" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="joins" type="{http://ws.interfaces.kuwaiba.org/}transientQuery" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="parent" type="{http://ws.interfaces.kuwaiba.org/}transientQuery" minOccurs="0"/>
 *         &lt;element name="join" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="limit" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="page" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "transientQuery", propOrder = {
    "className",
    "logicalConnector",
    "attributeNames",
    "visibleAttributeNames",
    "attributeValues",
    "conditions",
    "joins",
    "parent",
    "join",
    "limit",
    "page"
})
public class TransientQuery {

    protected String className;
    protected int logicalConnector;
    @XmlElement(nillable = true)
    protected List<String> attributeNames;
    @XmlElement(nillable = true)
    protected List<String> visibleAttributeNames;
    @XmlElement(nillable = true)
    protected List<String> attributeValues;
    @XmlElement(nillable = true)
    protected List<Integer> conditions;
    @XmlElement(nillable = true)
    protected List<TransientQuery> joins;
    protected TransientQuery parent;
    protected boolean join;
    protected int limit;
    protected int page;

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
     * Gets the value of the logicalConnector property.
     * 
     */
    public int getLogicalConnector() {
        return logicalConnector;
    }

    /**
     * Sets the value of the logicalConnector property.
     * 
     */
    public void setLogicalConnector(int value) {
        this.logicalConnector = value;
    }

    /**
     * Gets the value of the attributeNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAttributeNames() {
        if (attributeNames == null) {
            attributeNames = new ArrayList<String>();
        }
        return this.attributeNames;
    }

    /**
     * Gets the value of the visibleAttributeNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the visibleAttributeNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVisibleAttributeNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getVisibleAttributeNames() {
        if (visibleAttributeNames == null) {
            visibleAttributeNames = new ArrayList<String>();
        }
        return this.visibleAttributeNames;
    }

    /**
     * Gets the value of the attributeValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAttributeValues() {
        if (attributeValues == null) {
            attributeValues = new ArrayList<String>();
        }
        return this.attributeValues;
    }

    /**
     * Gets the value of the conditions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conditions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConditions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getConditions() {
        if (conditions == null) {
            conditions = new ArrayList<Integer>();
        }
        return this.conditions;
    }

    /**
     * Gets the value of the joins property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the joins property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJoins().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TransientQuery }
     * 
     * 
     */
    public List<TransientQuery> getJoins() {
        if (joins == null) {
            joins = new ArrayList<TransientQuery>();
        }
        return this.joins;
    }

    /**
     * Gets the value of the parent property.
     * 
     * @return
     *     possible object is
     *     {@link TransientQuery }
     *     
     */
    public TransientQuery getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransientQuery }
     *     
     */
    public void setParent(TransientQuery value) {
        this.parent = value;
    }

    /**
     * Gets the value of the join property.
     * 
     */
    public boolean isJoin() {
        return join;
    }

    /**
     * Sets the value of the join property.
     * 
     */
    public void setJoin(boolean value) {
        this.join = value;
    }

    /**
     * Gets the value of the limit property.
     * 
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the value of the limit property.
     * 
     */
    public void setLimit(int value) {
        this.limit = value;
    }

    /**
     * Gets the value of the page property.
     * 
     */
    public int getPage() {
        return page;
    }

    /**
     * Sets the value of the page property.
     * 
     */
    public void setPage(int value) {
        this.page = value;
    }
public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public void setAttributeValues(List<String> attributeValues) {
        this.attributeValues = attributeValues;
    }

    public void setConditions(List<Integer> conditions) {
        this.conditions = conditions;
    }

    public void setJoins(List<TransientQuery> joins) {
        this.joins = joins;
    }

    public void setVisibleAttributeNames(List<String> visibleAttributeNames) {
        this.visibleAttributeNames = visibleAttributeNames;
    }

}
