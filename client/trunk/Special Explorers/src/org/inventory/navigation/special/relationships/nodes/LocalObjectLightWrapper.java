/*
 * Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.special.relationships.nodes;

import org.inventory.communications.core.LocalObjectLight;

/**
 * Wrapper used to allow that a LocalObjectLight can be repeated in an AbstractScene
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class LocalObjectLightWrapper {
    private static long idCounter = 0;
    private final long id;
    private final LocalObjectLight lolWrapped;    
    
    public LocalObjectLightWrapper(LocalObjectLight lol) {
        id = idCounter;
        lolWrapped = lol;
        idCounter += 1;
    }
    
    public LocalObjectLight getLocalObjectLightWrapped() {
        return lolWrapped;
    }        
    
    public long getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return lolWrapped.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        if (!(obj instanceof LocalObjectLightWrapper))
            return false;
        
        return this.getId() == ((LocalObjectLightWrapper) obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
