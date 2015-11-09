/*
 *  Copyright 2010-2015 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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
import javax.swing.BorderFactory;
import org.netbeans.api.visual.vmd.VMDColorScheme;

/**
 * Factory used to provide default color schemes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ColorSchemeFactory {

    private static VMDColorScheme greenScheme;
    private static VMDColorScheme blueScheme;
    private static VMDColorScheme yellowScheme;
    private static VMDColorScheme grayScheme;

    /**
     * Gets the default greened color scheme
     * @return the scheme
     */
    public static VMDColorScheme getGreenScheme(){
        if (greenScheme == null){
            Color greenColor = new Color(230, 255, 100);
            greenScheme = new CustomizableColorScheme(greenColor, Color.black,
                    BorderFactory.createLineBorder(greenColor.darker()));
        }
        return greenScheme;
    }

    /**
     * Gets the default greened color scheme
     * @return the scheme
     */
    public static VMDColorScheme getBlueScheme(){
        if (blueScheme == null){
            Color blueColor = new Color(195, 195, 255);
            blueScheme = new CustomizableColorScheme(blueColor, Color.black,
                    BorderFactory.createLineBorder(blueColor.darker()));
        }
        return blueScheme;
    }

    /**
     * Gets the default yellowed color scheme
     * @return the scheme
     */
    public static VMDColorScheme getYellowScheme(){
        if (yellowScheme == null){
            Color yellowColor = new Color(255, 230, 130);
            yellowScheme = new CustomizableColorScheme(yellowColor, Color.black,
                    BorderFactory.createLineBorder(yellowColor.darker()));
        }
        return yellowScheme;
    }

    /**
     * Gets the default grayed color scheme
     * @return the scheme
     */
    public static VMDColorScheme getGrayScheme(){
        if (grayScheme == null){
            Color grayColor = new Color(220, 220, 220);
            grayScheme = new CustomizableColorScheme(grayColor, Color.black,
                    BorderFactory.createLineBorder(grayColor.darker()));
        }
        return grayScheme;
    }
}
