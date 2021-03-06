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
package org.inventory.models.physicalconnections.scene;

import java.awt.Color;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;

/**
 * Repeated. Show be merged with the one from ObjectView
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface SelectableWidget {
    public static Color selectionColor = new Color(167, 223, 219);
    public ObjectNode getNode();
    public void reset();
    public void highlight();
}
