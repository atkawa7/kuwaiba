/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

package org.inventory.core.visual.decorators;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import javax.swing.border.Border;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.anchor.PointShapeFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDNodeAnchor;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;

/**
 * Customizable color scheme used. Almost all of it is based on the VMDOriginalColorScheme
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CustomizableColorScheme extends VMDColorScheme{

    private Border nodeBorder;
    private Color headerColor;
    private Color bodyColor;
    private Color edgeColor;

    static final PointShape POINT_SHAPE_IMAGE = PointShapeFactory.createImagePointShape (ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-pin.png")); // NOI18N

    /**
     * Set the colors manually
     * @param bodyColor
     * @param edgeColor
     * @param border
     */
    public CustomizableColorScheme(Color bodyColor, Color edgeColor, Border border) {
        this.edgeColor = edgeColor;
        this.bodyColor = bodyColor;
        this.headerColor = bodyColor.darker();
        this.nodeBorder = border;
    }

    @Override
    public void installUI(VMDNodeWidget widget) {
        widget.setOpaque (true);
        widget.setBorder (nodeBorder);
        widget.setBackground(bodyColor);

        Widget header = widget.getHeader ();
        header.setBackground (headerColor);
        header.setOpaque (true);
    }

    @Override
    public void updateUI(VMDNodeWidget widget, ObjectState previousState, ObjectState state) {
        if (! previousState.isSelected ()  &&  state.isSelected ())
            widget.bringToFront ();
        else if (! previousState.isHovered ()  &&  state.isHovered ())
            widget.bringToFront ();

        Widget header = widget.getHeader ();
        header.setOpaque (state.isSelected ());
        //header.setBorder (state.isFocused () || state.isHovered () ? BORDER_PIN_HOVERED : BORDER_PIN);
    }

    @Override
    public void installUI(VMDPinWidget widget) {
        widget.setBackground(bodyColor.brighter());
        widget.setOpaque (false);
    }

    @Override
    public void updateUI(VMDPinWidget widget, ObjectState previousState, ObjectState state) {
        widget.setOpaque (state.isSelected ());
    }

    @Override
    public void installUI(VMDConnectionWidget widget) {
        widget.setForeground(edgeColor);
        widget.setSourceAnchorShape (AnchorShape.NONE);
        widget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        widget.setPaintControlPoints (true);
    }
    
    @Override
    public void updateUI(VMDConnectionWidget widget, ObjectState previousState, ObjectState state) {
        if (state.isHovered ())
            widget.setForeground (edgeColor.brighter());
        else if (state.isSelected ())
            widget.setForeground (edgeColor);
        else if (state.isHighlighted ())
            widget.setForeground (edgeColor.darker());
        else if (state.isFocused ())
            widget.setForeground (edgeColor.darker());
        else
            widget.setForeground (edgeColor);

        if (state.isSelected ()) {
            widget.setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
            widget.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
        } else {
            widget.setControlPointShape (PointShape.NONE);
            widget.setEndPointShape (POINT_SHAPE_IMAGE);
        }
    }

    @Override
    public boolean isNodeMinimizeButtonOnRight(VMDNodeWidget widget) {
        return false;
    }

    @Override
    public Image getMinimizeWidgetImage(VMDNodeWidget widget) {
        return widget.isMinimized ()
                ? ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-expand.png") // NOI18N
                : ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-collapse.png"); // NOI18N
    }

    @Override
    public Widget createPinCategoryWidget(VMDNodeWidget widget, String categoryDisplayName) {
        return createPinCategoryWidgetCore (widget, categoryDisplayName, true);
    }

    @Override
    public int getNodeAnchorGap(VMDNodeAnchor anchor) {
        return 8;
    }

    private Widget createPinCategoryWidgetCore (VMDNodeWidget widget, String categoryDisplayName, boolean changeFont) {
        Scene scene = widget.getScene ();
        LabelWidget label = new LabelWidget (scene, categoryDisplayName);
        label.setOpaque (true);
        label.setBackground (headerColor);
        label.setForeground (headerColor.darker());
        if (changeFont) {
            Font fontPinCategory = scene.getDefaultFont ().deriveFont (10.0f);
            label.setFont (fontPinCategory);
        }
        label.setAlignment (LabelWidget.Alignment.CENTER);
        label.setCheckClipping (true);
        return label;
    }
}
