/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.ws.toserialize.application;

import java.io.Serializable;

/**
 * Validators are flags indicating things about objects. Of course, every instance may have
 * something to expose or not. For instance, a port has an indicator to mark it as "connected physically",
 * but a Building (so far) has nothing to "indicate". This is done in order to avoid a second call to query
 * for a particular information that could affect the performance. I.e:
 * Call 1: getPort (retrieving a LocalObjectLight) <br />
 * Call 2: isThisPortConnected (retrieving a boolean according to a condition) <br />
 *
 * With this method there's only one call
 * getPort (a LocalObjectLight with a flag to indicate that the port is connected) <br />
 *
 * Why not use getPort retrieving a LocalObject? Well, because the condition might be complicated, and
 * it's easier to compute its value at server side. Besides, it can involve complex queries that would require
 * more calls to the webservice
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Validator implements Serializable {
    /**
     * The name of this validator
     */
    private String label;
    /**
     * The value of this validator
     */
    private int value;

    //No-arg constructor required
    public Validator(){}

    public Validator(String label, int value){
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
