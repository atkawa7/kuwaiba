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

import java.io.Serializable;

/**
 * Represents a view. A view is a graphical representation of a context. Examples are: a view describing
 * how buildings are connected in a city or the equipment inside a rack
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class View implements Serializable{
    /**
     * Background image
     */
    protected byte[] background;
    /**
     * Structure as an XML file
     */
    protected byte[] structure;
    /**
     * View description
     */
    protected String description;
    /**
     * What class this view is instance of. call it a "type" (i.e. RackView, DefaultView, etc)
     */
    private String viewClass;

    public byte[] getBackground() {
        return background;
    }

    public void setBackground(byte[] background) {
        this.background = background;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getStructure() {
        return structure;
    }

    public void setStructure(byte[] structure) {
        this.structure = structure;
    }

    public String getViewClass() {
        return viewClass;
    }

    public void setViewClass(String viewClass) {
        this.viewClass = viewClass;
    }
}
