/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.core.services.api.nodes;

/**
 * A simple helper class composed by an integer value and a tag (string) that serves as textual representation of the integer value.
 * It could be used for example in combo boxes and lists that present a set of integer options
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class IntegerEntry {
    /**
     * The value of the entry
     */
    private int value;
    /**
     * The string used to display in the combo box or list as a representation of the value field
     */
    private String tag;

    public IntegerEntry(int value, String tag) {
        this.value = value;
        this.tag = tag;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
    @Override
    public String toString() {
        return tag;
    }
    
    @Override
    public boolean equals (Object obj){
        if (obj instanceof IntegerEntry) {
            if (((IntegerEntry)obj).getValue() == getValue())
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.value;
        return hash;
    }
}
