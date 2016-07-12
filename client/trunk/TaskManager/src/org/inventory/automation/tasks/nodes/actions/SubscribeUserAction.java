/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.automation.tasks.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.inventory.automation.tasks.nodes.TaskNode;
import org.inventory.automation.tasks.windows.UsersFrame;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalUserObject;
import org.openide.util.Utilities;

/**
 * Subscribes a user to a task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class SubscribeUserAction extends AbstractAction {
    
    public SubscribeUserAction() {
        putValue(NAME, "Subscribe User...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        TaskNode taskNode = Utilities.actionsGlobalContext().lookup(TaskNode.class);
        CommunicationsStub com = CommunicationsStub.getInstance();
        List<LocalUserObject> users = com.getUsers();
        UsersFrame frame = new UsersFrame(taskNode, users);
        frame.setVisible(true);
    }
    
}
