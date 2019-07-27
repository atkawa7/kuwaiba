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

import java.awt.Color;
import java.awt.Paint;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.model.ObjectState;

/**
 * A high contrast L&F to be used in dark backgrounds
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class HighContrastLookAndFeel extends LookFeel {

    private static final Color COLOR_BACKGROUND_NORMAL = Color.GREEN;
    
    private static final Color COLOR_SELECTED = new Color(55, 59, 91);
    private static final Color COLOR_HIGHLIGHTED = COLOR_SELECTED.darker ();
    private static final Color COLOR_HOVERED = COLOR_SELECTED.brighter ();
    private static final int MARGIN = 3;
    private static final int ARC = 10;
    private static final int MINI_THICKNESS = 1;

    private static final Border BORDER_NORMAL = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_BACKGROUND_NORMAL, COLOR_BACKGROUND_NORMAL.brighter());
    private static final Border BORDER_HOVERED = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_HOVERED, COLOR_HOVERED.darker ());
    private static final Border BORDER_SELECTED = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_SELECTED, COLOR_SELECTED.darker ());

    private static final Border MINI_BORDER_NORMAL = BorderFactory.createEmptyBorder (MINI_THICKNESS);
    private static final Border MINI_BORDER_HOVERED = BorderFactory.createRoundedBorder (MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, COLOR_HOVERED, COLOR_HOVERED.darker ());
    private static final Border MINI_BORDER_SELECTED = BorderFactory.createRoundedBorder (MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, COLOR_SELECTED, COLOR_SELECTED.darker ());
    
    
    private static HighContrastLookAndFeel instance;
    
    public static LookFeel getInstance () {
        return instance == null ? instance = new HighContrastLookAndFeel() : instance;
    }
    
    @Override
    public Paint getBackground () {
        return COLOR_BACKGROUND_NORMAL;
    }

    @Override
    public Color getForeground () {
        return Color.WHITE;
    }
    
    @Override
    public Color getLineColor (ObjectState state) {
        if (state.isHovered ())
            return COLOR_HOVERED;
        if (state.isSelected ())
            return COLOR_SELECTED;
        if (state.isHighlighted ()  || state.isFocused ())
            return COLOR_HIGHLIGHTED;
        return Color.WHITE;
    }

    @Override
    public Paint getBackground (ObjectState state) {
        if (state.isHovered ())
            return COLOR_HOVERED;
        if (state.isSelected ())
            return COLOR_SELECTED;
        if (state.isHighlighted ()  || state.isFocused ())
            return COLOR_HIGHLIGHTED;
        return COLOR_BACKGROUND_NORMAL;
    }

    @Override
    public Color getForeground (ObjectState state) {
        return state.isSelected () ? Color.WHITE : Color.BLACK;
    }
    
    @Override
    public Border getBorder (ObjectState state) {
        if (state.isHovered ())
            return BORDER_HOVERED;
        if (state.isSelected ())
            return BORDER_SELECTED;
        if (state.isFocused ())
            return BORDER_HOVERED;
        return BORDER_NORMAL;
    }

    @Override
    public Border getMiniBorder (ObjectState state) {
        if (state.isHovered ())
            return MINI_BORDER_HOVERED;
        if (state.isSelected ())
            return MINI_BORDER_SELECTED;
        if (state.isFocused ())
            return MINI_BORDER_HOVERED;
        return MINI_BORDER_NORMAL;
    }

    @Override
    public boolean getOpaque (ObjectState state) {
        return state.isHovered ()  ||  state.isSelected ();
    }
    
    @Override
    public int getMargin () {
        return MARGIN;
    }
}
