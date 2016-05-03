/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package org.inventory.core.visual.export;

import java.util.ArrayList;

/**
 * Convenience class that represents a layer of objects, not from the graphical perspective, but as POJOS
 * so the can be easily serialized
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Layer {
    private ArrayList objects;
    private String name;

    public Layer(ArrayList objects, String name) {
        this.objects = objects;
        this.name = name;
    }

    public ArrayList getObjects() {
        return objects;
    }

    public void setObjects(ArrayList objects) {
        this.objects = objects;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
