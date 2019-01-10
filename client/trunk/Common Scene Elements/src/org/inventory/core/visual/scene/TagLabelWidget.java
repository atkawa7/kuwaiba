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
import java.awt.Font;
import java.awt.Point;
import org.inventory.communications.core.LocalObjectLight;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * A label widget designed to be updated when an ObjectNodeWidget's position or bounds are updated
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TagLabelWidget extends LabelWidget implements Widget.Dependency {
    
    /**
     * Default border (rounded, dark gray background and black border line)
     */
    public static final Border DEFAULT_BORDER = BorderFactory.createRoundedBorder(5, 5, Color.DARK_GRAY, Color.BLACK);
    /**
     * Default font (size 10, Sans Serif)
     */
    public static final Font DEFAULT_SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    /**
     * The label will be placed on the attached widget
     */
    public static final int OVERLAPPED = 0;
    /**
     * The label will be placed on the top left corner
     */
    public static final int TOP_LEFT = 1;
    /**
     * The label will be placed on the top right corner
     */
    public static final int TOP_RIGHT = 2;
    /**
     * The label will be placed over the widget, centered
     */
    public static final int TOP = 3;
    /**
     * The label will be placed on the bottom left corner
     */
    public static final int BOTTOM_LEFT = 4;
    /**
     * The label will be placed on the top right corner
     */
    public static final int BOTTOM_RIGHT = 5;
    /**
     * The label will be placed under the widget, centered
     */
    public static final int BOTTOM = 6;
    /**
     * Widget this label is attached to
     */
    private Widget widget;
    /**
     * Widget position
     */
    private int position;
    
    public TagLabelWidget(AbstractScene<LocalObjectLight, LocalObjectLight> scene, ObjectNodeWidget widget) {
        super(scene);
        this.widget = widget;
        setForeground(Color.BLACK);
        setBorder(DEFAULT_BORDER);
        setFont(DEFAULT_SMALL_FONT);
        this.position = BOTTOM;
        setLabel(scene.findObject(widget).toString());
    }

    public TagLabelWidget(AbstractScene<LocalObjectLight, LocalObjectLight> scene, ObjectNodeWidget widget, 
            String label, int position) {
        this(scene, widget);
        setLabel(label);
        this.position = position;
    }  
    
    @Override
    public void revalidateDependency() {
        switch (position){
            case TOP:
                setPreferredLocation(new Point(widget.getPreferredLocation().x  + widget.getPreferredBounds().width / 2 - getPreferredBounds().width / 2, 
                        widget.getPreferredLocation().y - getPreferredBounds().height / 2));
                break;
            case TOP_LEFT:
                setPreferredLocation(new Point(widget.getPreferredLocation().x + widget.getPreferredBounds().width  - getPreferredBounds().width, 
                        widget.getPreferredLocation().y - getPreferredBounds().height / 2));
                break;
            case TOP_RIGHT:
                setPreferredLocation(new Point(widget.getPreferredLocation().x + widget.getPreferredBounds().width, 
                        widget.getPreferredLocation().y - getPreferredBounds().height / 2));
                break;    
            default:
            case BOTTOM:
                setPreferredLocation(new Point(widget.getPreferredLocation().x  + widget.getPreferredBounds().width / 2 - getPreferredBounds().width / 2, 
                        widget.getPreferredLocation().y + widget.getPreferredBounds().height / 2 + getPreferredBounds().height));
                break;
            case BOTTOM_LEFT:
                setPreferredLocation(new Point(widget.getPreferredLocation().x + widget.getPreferredBounds().width  - getPreferredBounds().width, 
                        widget.getPreferredLocation().y + widget.getPreferredBounds().height / 2 + getPreferredBounds().height));
                break;
            case BOTTOM_RIGHT:
                setPreferredLocation(new Point(widget.getPreferredLocation().x + widget.getPreferredBounds().width, 
                        widget.getPreferredLocation().y + widget.getPreferredBounds().height / 2 + getPreferredBounds().height));
                break;
        }
        
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
        revalidateDependency();
    }
}