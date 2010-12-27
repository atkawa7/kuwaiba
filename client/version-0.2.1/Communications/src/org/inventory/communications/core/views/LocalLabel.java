/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.communications.core.views;

import java.awt.Point;

/**
 * Represents a label independent from the presentation layer. This class represents
 * a text label but it's independent from the visual library so it can be rendered using anything
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalLabel {
    /**
     * Used to indicate a horizontal orientation
     */
    public static final int ORIENTATION_HORIZONTAL = 1;
    /**
     * Used to indicate a vertical orientation
     */
    public static final int ORIENTATION_VERTICAL = 2;

    /**
     * The label text
     */
    private String text;
    /**
     * The label orientation
     */
    private int orientation;
    /**
     * X,Y absolute coordinates
     */
    private Point position;

    public LocalLabel(String _text, int _orientation, int x, int y){
        this.text = _text;
        this.orientation = _orientation;
        this.position = new Point(x, y);
    }

    public int getOrientation() {
        return orientation;
    }

    public Point getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }
}
