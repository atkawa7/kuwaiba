/**
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.visual.scene;

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * A connection widget representing a link or a container
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AbstractConnectionWidget extends ConnectionWidget implements SelectableWidget {
    /**
     * A node representing the wrapped object.
     */
    private ObjectNode node;

    public AbstractConnectionWidget(Scene scene, LocalObjectLight object) {
        super(scene);
        this.node = new ObjectNode(object);
        setToolTipText(object.toString());
        createActions(AbstractScene.ACTION_SELECT);
    }

    public LocalObjectLight getObject() {
        return node.getObject();
    }

    public void setObject(LocalObjectLight object) {
        this.node = new ObjectNode(object);
    }

    @Override
    public ObjectNode getNode() {
        return node;
    }
}
