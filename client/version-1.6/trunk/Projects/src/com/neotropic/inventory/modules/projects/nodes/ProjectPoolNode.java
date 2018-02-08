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
package com.neotropic.inventory.modules.projects.nodes;

import com.neotropic.inventory.modules.projects.actions.AddProjectAction;
import com.neotropic.inventory.modules.projects.actions.DeleteProjectPoolAction;
import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.core.LocalPool;
import org.inventory.navigation.pools.nodes.PoolNode;
import org.openide.util.ImageUtilities;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectPoolNode extends PoolNode {
    private final Image icon = ImageUtilities.loadImage("com/neotropic/inventory/modules/projects/res/project-pool-icon.png");

    public ProjectPoolNode(LocalPool pool) {
        super(pool);
        setChildren(new ProjectPoolChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            AddProjectAction.getInstance(),
            null,
            DeleteProjectPoolAction.getIntance()
        };
    }
    
    @Override
    public String getDisplayName() {
        return String.format("%s [Pool of %s]", getPool().getName(), getPool().getClassName());
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
