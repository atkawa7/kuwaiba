/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.automation.tasks.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.inventory.automation.tasks.nodes.actions.TaskManagerActionFactory;
import org.inventory.communications.core.LocalUserObjectLight;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * Node that wraps a user object (one of the users subscribed to this task)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TaskUserNode extends AbstractNode {
    
    private static final Image defaultIcon = ImageUtilities.loadImage("org/inventory/automation/tasks/res/user.png");
    
    public TaskUserNode(LocalUserObjectLight user) {
        super(Children.LEAF, Lookups.singleton(user));
        setDisplayName(user.toString());
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { TaskManagerActionFactory.createUnsubscribeUserAction() };
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return defaultIcon;
    }

    @Override
    public Image getIcon(int type) {
        return defaultIcon;
    }
}
