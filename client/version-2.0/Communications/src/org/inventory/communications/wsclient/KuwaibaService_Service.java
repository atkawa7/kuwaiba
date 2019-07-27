
package org.inventory.communications.wsclient;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "KuwaibaService", targetNamespace = "http://ws.interfaces.kuwaiba.org/", wsdlLocation = "http://localhost:8080/kuwaiba/KuwaibaService?wsdl")
public class KuwaibaService_Service
    extends Service
{

    private final static URL KUWAIBASERVICE_WSDL_LOCATION;
    private final static WebServiceException KUWAIBASERVICE_EXCEPTION;
    private final static QName KUWAIBASERVICE_QNAME = new QName("http://ws.interfaces.kuwaiba.org/", "KuwaibaService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/kuwaiba/KuwaibaService?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        KUWAIBASERVICE_WSDL_LOCATION = url;
        KUWAIBASERVICE_EXCEPTION = e;
    }

    public KuwaibaService_Service() {
        super(__getWsdlLocation(), KUWAIBASERVICE_QNAME);
    }

    public KuwaibaService_Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), KUWAIBASERVICE_QNAME, features);
    }

    public KuwaibaService_Service(URL wsdlLocation) {
        super(wsdlLocation, KUWAIBASERVICE_QNAME);
    }

    public KuwaibaService_Service(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, KUWAIBASERVICE_QNAME, features);
    }

    public KuwaibaService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public KuwaibaService_Service(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns KuwaibaService
     */
    @WebEndpoint(name = "KuwaibaServicePort")
    public KuwaibaService getKuwaibaServicePort() {
        return super.getPort(new QName("http://ws.interfaces.kuwaiba.org/", "KuwaibaServicePort"), KuwaibaService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns KuwaibaService
     */
    @WebEndpoint(name = "KuwaibaServicePort")
    public KuwaibaService getKuwaibaServicePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://ws.interfaces.kuwaiba.org/", "KuwaibaServicePort"), KuwaibaService.class, features);
    }

    private static URL __getWsdlLocation() {
        if (KUWAIBASERVICE_EXCEPTION!= null) {
            throw KUWAIBASERVICE_EXCEPTION;
        }
        return KUWAIBASERVICE_WSDL_LOCATION;
    }

}
