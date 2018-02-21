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

import com.neotropic.inventory.modules.projects.ProjectsModuleService;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Children of <code>ProjectPoolNode</code>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectPoolChildren extends Children.Keys<LocalObjectLight> {
    
    @Override
    public void addNotify() {
        ProjectPoolNode selectedNode = (ProjectPoolNode) getNode();
        List<LocalObjectLight> projects = CommunicationsStub.getInstance().getProjectInProjectPool(selectedNode.getPool().getOid(), -1);
        
        if (projects == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup(
                ProjectsModuleService.bundle.getString("LBL_ERROR"), 
                NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            Collections.sort(projects);
            setKeys(projects);
        }
    }
    
    @Override
    public void removeNotify() {
        setKeys(Collections.EMPTY_LIST);
    }
        
    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new Node[] { new ProjectNode(key)};
    }
}
