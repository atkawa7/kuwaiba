/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.inventory.core.services.api.visual;

import java.awt.Point;

/**
 * Represents a label in an object view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface LocalLabel {
    /**
     * Used to indicate a horizontal orientation
     */
    public static final int ORIENTATION_HORIZONTAL = 1;
    /**
     * Used to indicate a vertical orientation
     */
    public static final int ORIENTATION_VERTICAL = 2;

    public int getOrientation();

    public Point getPosition();

    public String getText();
}
