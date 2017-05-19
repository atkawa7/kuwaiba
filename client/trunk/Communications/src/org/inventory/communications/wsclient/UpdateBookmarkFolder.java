
package org.inventory.communications.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for updateBookmarkFolder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="updateBookmarkFolder">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bookmarkId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="bookmarkName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "updateBookmarkFolder", propOrder = {
    "bookmarkId",
    "bookmarkName",
    "sessionId"
})
public class UpdateBookmarkFolder {

    protected long bookmarkId;
    protected String bookmarkName;
    protected String sessionId;

    /**
     * Gets the value of the bookmarkId property.
     * 
     */
    public long getBookmarkId() {
        return bookmarkId;
    }

    /**
     * Sets the value of the bookmarkId property.
     * 
     */
    public void setBookmarkId(long value) {
        this.bookmarkId = value;
    }

    /**
     * Gets the value of the bookmarkName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookmarkName() {
        return bookmarkName;
    }

    /**
     * Sets the value of the bookmarkName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookmarkName(String value) {
        this.bookmarkName = value;
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
