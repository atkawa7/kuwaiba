/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.inventory.predefinedshapes.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.predefinedshapes.nodes.actions.CreateLayoutAction;
import org.inventory.predefinedshapes.nodes.actions.SetPaletteIconAction;
import org.openide.util.ImageUtilities;

/**
 * Represents a predefined shape as node
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PredefinedShapeNode extends ObjectNode {
    private static final Image defaultIcon = ImageUtilities.loadImage("org/inventory/predefinedshapes/res/list-type-item.png");
    
    public PredefinedShapeNode(LocalObjectLight lol) {
        super(lol, true);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action [] {
            new CreateLayoutAction(), 
            new SetPaletteIconAction()};
    }
    
    @Override
    public void setName(String newName) {
        super.setName(newName);
        //Refresh the cache
        CommunicationsStub.getInstance().getList(getObject().getClassName(), true, true);
    }
    
    @Override
    public Image getIcon(int i){
        return defaultIcon;
    }
    
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
}

