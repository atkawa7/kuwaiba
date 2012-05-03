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
    //NOTE: GIS views are not handled here
    /**
     * Id for a default view
     */
    public static final int TYPE_DEFAULT = 0;
    /**
     * Id for a view used for racks
     */
    public static final int TYPE_RACK = 1;
    /**
     * Id for a view used in equipment with slots and boards
     */
    public static final int TYPE_EQUIPMENT = 2;

    private Long id;
    /**
     * Relative path pointing to the background image
     */
    private String backgroundPath;
    /**
     * Structure as an XML document
     */
    private byte[] structure;
    /**
     * View type (see supported types above)
     */
    private int viewType;

    public View(Long id, int viewType) {
        this.id = id;
        this.viewType = viewType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public byte[] getStructure() {
        return structure;
    }

    public void setStructure(byte[] structure) {
        this.structure = structure;
    }
}
