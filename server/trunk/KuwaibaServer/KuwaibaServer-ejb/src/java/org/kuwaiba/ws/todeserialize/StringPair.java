/**
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

package org.kuwaiba.ws.todeserialize;

import java.io.Serializable;
import java.util.List;

/**
 * A StringPair mimics the behavior of a HashSet<String, String> entry, but with a webservice-friendly implementation. A normal HashMap is 
 * serialized in a weird way. The typical use case for this class is when you need to provide a set of parameters in a key-value fashion
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

public class StringPair implements Serializable {
    /**
     * The key
     */
    private String key;
    /**
     * The value. Typically, a serialized object or primitive type
     */
    private String value;

    /**
     * Arg-less constructor is required
     */
    public StringPair() {}

    public StringPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public static final String get(List<StringPair> listOfStringPairs, String key) {
        for (StringPair pair : listOfStringPairs)
            if (key.equals(pair.getKey()))
                return pair.getValue();
        
        return null;
    }
}
