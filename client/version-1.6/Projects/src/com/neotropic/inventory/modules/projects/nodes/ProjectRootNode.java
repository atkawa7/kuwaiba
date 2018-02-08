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

import com.neotropic.inventory.modules.projects.actions.CreateProjectPoolAction;
import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.core.LocalPool;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;

/**
 * Root node of Projects see: <code>ProjectNode</code>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectRootNode extends AbstractNode {
    private static final String ICON_PATH = "com/neotropic/inventory/modules/projects/res/root.png";
    private static final Image icon = ImageUtilities.loadImage(ICON_PATH);
    
    public ProjectRootNode() {
        super(new ProjectRootChildren());
        setDisplayName("Projects");
    }
    
    public LocalPool getProjectRootPool() {
        return getLookup().lookup(LocalPool.class);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            CreateProjectPoolAction.getInstance()
        };
    }
    
    @Override
    public Image getIcon(int i) {
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
}
