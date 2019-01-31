/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

import java.awt.BasicStroke;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.decorators.DotLineStroke;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;

/**
 * A connection widget representing a link or a container
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectConnectionWidget extends SelectableConnectionWidget {
    /**
     * Is this widget in high-contrast mode?
     */
    private boolean highContrast;
    /**
     * style for the line, dot line
     */
    public static final int DOT_LINE = 2;
    /**
     * line dot style
     */
    public static final int LINE = 1;
    
    public ObjectConnectionWidget(Scene scene, LocalObjectLight object, int lineStyle) {
        super(scene, object);
        createActions(AbstractScene.ACTION_SELECT);
        highContrast = false;
        if(lineStyle == DOT_LINE)
            this.setStroke(new DotLineStroke(1));
        
    }
    
    public void setHighContrast(boolean highContrast) {
        this.highContrast = highContrast;
        this.labelWidget.setOpaque(highContrast);
        notifyStateChanged(getState(), getState());
    }
    
    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);

        if (state.isSelected())
            setStroke((getStroke() instanceof DotLineStroke) ? new DotLineStroke(4) : new BasicStroke(4));
        else if (previousState.isSelected())
            setStroke((getStroke() instanceof DotLineStroke) ? new DotLineStroke(3) : new BasicStroke(3));
        
        if (!highContrast) {
            labelWidget.setForeground (getScene().getLookFeel().getForeground (state));
            labelWidget.setBackground(getScene().getLookFeel().getBackground(state));
            labelWidget.setBorder(getScene().getLookFeel().getBorder (state));
        } else {
            labelWidget.setForeground (HighContrastLookAndFeel.getInstance().getForeground (state));
            labelWidget.setBackground(HighContrastLookAndFeel.getInstance().getBackground(state));
            labelWidget.setBorder(HighContrastLookAndFeel.getInstance().getBorder (state));
        }
    }    
}
