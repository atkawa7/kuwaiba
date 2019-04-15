/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.web.gui.miniapps.sync;

import java.util.Objects;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SyncProvider {
    private String name;
    private String value;

    SyncProvider(String name, String value) {
        this.name = name;
        this.value = value;                
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue(){ 
        return value;
    }

    public void setValue(String value) {
        this.value = value;            
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SyncProvider other = (SyncProvider) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }        

    @Override
    public String toString() {
        return value;
    }
}
