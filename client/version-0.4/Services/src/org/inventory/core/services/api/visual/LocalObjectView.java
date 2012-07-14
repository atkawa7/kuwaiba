/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.inventory.core.services.api.visual;

import java.awt.Image;
import java.util.List;
import javax.xml.stream.XMLStreamException;

/**
 * Interface representing an object view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface LocalObjectView {
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
    
    public List<LocalEdge> getEdges();

    public List<LocalLabel> getLabels();

    public List<LocalNode> getNodes();

    public Image getBackground();

    public int getViewType();

    public void parseXML(byte[] structure) throws XMLStreamException;

    public boolean isDirty();

    public void setDirty(boolean value);
}
