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

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.automation.tasks.TaskManagerService;
import org.inventory.automation.tasks.nodes.actions.TaskManagerActionFactory;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * The root node of the Task Manager module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TaskManagerRootNode extends AbstractNode {
    public static final String DEFAULT_ICON_PATH = "org/inventory/automation/tasks/res/root.png";
    
    public TaskManagerRootNode() {
        super(new TaskManagerRootChildren());
        setDisplayName("Available Tasks");
        setIconBaseWithExtension(DEFAULT_ICON_PATH);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { TaskManagerActionFactory.createCreateTaskAction() };
    }
    
    public static class TaskManagerRootChildren extends Children.Keys<LocalTask> {

        @Override
        public void addNotify() {
            List<LocalTask> tasks = CommunicationsStub.getInstance().getTasks();
            if (tasks == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                setKeys(Collections.EMPTY_SET);
            } else {
                Collections.sort(tasks);
                setKeys(tasks);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalTask key) {
            key.addChangeListener(TaskManagerService.getInstance());
            return new Node[] { new TaskNode(key) };
        }

        @Override
        protected void destroyNodes(Node[] nodes) {
            for (Node node : nodes)
                node.getLookup().lookup(LocalTask.class).removeChangeListener(TaskManagerService.getInstance());
        }
    }
}
