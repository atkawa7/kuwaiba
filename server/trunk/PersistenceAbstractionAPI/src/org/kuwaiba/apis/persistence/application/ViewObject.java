/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.application;

/**
 * Represents a view. A view is a graphical representation of a context. Examples are: a view describing
 * how buildings are connected in a city or the equipment inside a rack
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ViewObject extends ViewObjectLight {

    public ViewObject(long id, String name, String description, int viewType) {
        super (id, name, description, viewType);
    }

    /**
     * Background image
     */
    private byte[] backgroundPath;
    /**
     * Structure as an XML document
     */
    private byte[] structure;
    

    public byte[] getBackground() {
        return backgroundPath;
    }

    public void setBackground(byte[] backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public byte[] getStructure() {
        return structure;
    }

    public void setStructure(byte[] structure) {
        this.structure = structure;
    }
}
