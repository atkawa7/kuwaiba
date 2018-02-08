/**
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
 */
package org.inventory.core.templates.nodes;

import java.awt.Color;
import java.awt.Image;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Utils;

/**
 * A node representing a template special element.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class TemplateSpecialElementNode extends TemplateElementNode {
    private static final Image defaultSpecialIcon = Utils.createRectangleIcon(new Color(11, 91, 111), 
            Utils.DEFAULT_ICON_WIDTH, Utils.DEFAULT_ICON_HEIGHT);

    public TemplateSpecialElementNode(LocalObjectLight object) {
        super(object);
        icon =  defaultSpecialIcon;
    }
}
