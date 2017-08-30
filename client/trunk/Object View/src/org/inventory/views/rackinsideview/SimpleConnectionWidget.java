/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.rackinsideview;

import java.awt.Color;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.HighContrastLookAndFeel;
import org.inventory.core.visual.scene.SelectableConnectionWidget;
import org.inventory.views.rackview.scene.RackInsideViewScene;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.model.ObjectState;

/**
 * Represents a connection between ports in the rack inside view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SimpleConnectionWidget extends SelectableConnectionWidget {
    
    private Color originalColor;
    
    public SimpleConnectionWidget(RackInsideViewScene scene, LocalObjectLight object, Color originalColor) {
        super(scene, object);
        setRouter(scene.getRouter());
        this.originalColor = originalColor;
        setEndPointShape(PointShape.SQUARE_FILLED_BIG);
    }
    
     /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        
        if (state.isSelected()) {
            labelWidget.setForeground (Color.WHITE);
            labelWidget.setBackground(Color.BLUE);
            //labelWidget.setBorder(getScene().getLookFeel().getBorder (state));
        } else if (previousState.isSelected()){
            labelWidget.setForeground (Color.BLACK);
            labelWidget.setBackground(Color.WHITE);
            //labelWidget.setBorder(HighContrastLookAndFeel.getInstance().getBorder (state));
            setForeground(originalColor);
        }
    }
}