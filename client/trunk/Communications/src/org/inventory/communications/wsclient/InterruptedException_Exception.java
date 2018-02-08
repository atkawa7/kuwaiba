
package org.inventory.communications.wsclient;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "InterruptedException", targetNamespace = "http://ws.kuwaiba.org/")
public class InterruptedException_Exception
    extends java.lang.Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private InterruptedException faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public InterruptedException_Exception(String message, InterruptedException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public InterruptedException_Exception(String message, InterruptedException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.inventory.communications.wsclient.InterruptedException
     */
    public InterruptedException getFaultInfo() {
        return faultInfo;
    }

}
