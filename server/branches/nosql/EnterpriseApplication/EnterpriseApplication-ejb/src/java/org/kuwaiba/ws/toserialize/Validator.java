/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kuwaiba.ws.toserialize;

/**
 * Validators are flags indicating things about objects. Of course, every instance may have
 * something to expose or not. For instance, a port has an indicator to mark it as "connected physically",
 * but a Building (so far) has nothing to "indicate". This is done in order to avoid a second call to query
 * for a particular information that could affect the performance. I.e:
 * Call 1: getPort (retrieving a LocalObjectLight)
 * Call 2: isThisPortConnected (retrieving a boolean according to a condition)
 *
 * With this method there's only one call
 * getPort (a LocalObjectLight with a flag to indicate that the port is connected)
 *
 * Why not use getPort retrieving a LocalObject? Well, because the condition might be complicated, and
 * it's easier to compute its value at server side. Besides, it can involve complex queries that would require
 * more calls to the webservice
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class Validator {
    /**
     * The name of this validator
     */
    private String label;
    /**
     * The value of this validator
     */
    private Boolean value;

    /**
     * Required by the serializer
     */
    public Validator(){}

    public Validator(String _label, Boolean _value){
        this.label = _label;
        this.value = _value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
