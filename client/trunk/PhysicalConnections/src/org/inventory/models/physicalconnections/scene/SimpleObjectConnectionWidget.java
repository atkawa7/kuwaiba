/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.models.physicalconnections.scene;

import java.awt.Color;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.widget.ConnectionWidget;

/**
 * Represents a node in the Graphical Physical Path view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SimpleObjectConnectionWidget extends ConnectionWidget implements SelectableWidget {
    
    private ObjectNode node;
    private Color originalColor;
    
    public SimpleObjectConnectionWidget(PhysicalPathScene scene, LocalObjectLight object, 
            Color originalColor) {
        super(scene);
        this.node = new ObjectNode(object, true);
        this.originalColor = originalColor;
        setRouter(scene.getRouter());
        setLineColor(originalColor);
        setToolTipText(object.toString());
        setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        getActions().addAction(scene.createSelectAction());
        getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
    }
    
    @Override
    public ObjectNode getNode(){
        return node;
    }
        @Override
    public void reset() {
        setLineColor(originalColor);
    }

    @Override
    public void highlight() {
        setLineColor(selectionColor);
    }
}