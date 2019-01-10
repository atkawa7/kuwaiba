/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.core.visual.scene.SelectableConnectionWidget;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.model.ObjectState;

/**
 * Represents a node in the Graphical Physical Path view
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleConnectionWidget extends SelectableConnectionWidget {
    
    private Color originalColor;
    
    public SimpleConnectionWidget(PhysicalPathScene scene, LocalObjectLight object, Color originalColor) {
        super(scene, object);
        this.originalColor = originalColor;
        setRouter(scene.getRouter());
        setForeground(originalColor);
        setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        getLabelWidget().setVisible(false);
    }
    
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        
        if (state.isSelected())
            setBackground(Color.ORANGE);
        
        if (previousState.isSelected())
            setForeground(originalColor);
        
        setPaintControlPoints (state.isSelected());
    }
}
