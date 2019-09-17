/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import com.neotropic.inventory.modules.projects.ProjectsModuleService;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.Node;

/**
 * <code>ProjectNode</code> children
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProjectChildren extends AbstractChildren {

    @Override
    public void addNotify() {
        ProjectNode selectedNode = (ProjectNode) getNode();
        String projectClass = selectedNode.getObject().getClassName();
        String projectId = selectedNode.getObject().getId();
        
        List<LocalObjectLight> activities = CommunicationsStub.getInstance()
            .getProjectActivities(projectClass, projectId);
        
        if (activities == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup(
                ProjectsModuleService.bundle.getString("LBL_ERROR"), 
                NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            Collections.sort(activities);
            setKeys(activities);
        }
    }
        
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new Node[] {new ActivityNode(key)};
    }
}
