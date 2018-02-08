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
package com.neotropic.inventory.modules.projects.nodes;

import com.neotropic.inventory.modules.projects.actions.AddActivityAction;
import com.neotropic.inventory.modules.projects.actions.DeleteProjectAction;
import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.ImageUtilities;

/**
 * Represent a project
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectNode extends ObjectNode {
    private static final String ICON_PATH="com/neotropic/inventory/modules/projects/res/project-icon.png";
    private static final Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);
    
    public ProjectNode(LocalObjectLight lol) {
        super(lol);
        setChildren(new ProjectChildren());
    }
        
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { 
            AddActivityAction.getInstance(), 
            null, 
            DeleteProjectAction.getIntance() };
    }
    
    @Override
    public Image getIcon(int i) {
        return defaultIcon;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
}
