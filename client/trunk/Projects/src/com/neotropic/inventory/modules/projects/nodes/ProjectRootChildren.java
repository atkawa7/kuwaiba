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

import com.neotropic.inventory.modules.projects.ProjectsModuleService;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.Node;

/**
 * <code>ProjectRootNode</code> children
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectRootChildren extends AbstractChildren {

    @Override
    public void addNotify() {
        ProjectRootNode selectedNode = (ProjectRootNode) getNode();
        List<LocalObjectLight> projects = CommunicationsStub.getInstance().getPoolItems(selectedNode.getProjectRootPool().getOid());
        
        if (projects == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup(
                ProjectsModuleService.bundle.getString("LBL_ERROR"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        } else {
            Collections.sort(projects);
            setKeys(projects);
        }
        // TODO: set nodes
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new Node[] {new ProjectNode(key)};
    }
    /*
        @Override
        public void addNotify() {
            ContractPoolNode selectedNode = (ContractPoolNode)getNode();
            List<LocalObjectLight> contracts = CommunicationsStub.getInstance().getPoolItems(selectedNode.getPool().getOid());
            
            if (contracts == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            } else {
                Collections.sort(contracts);
                setKeys(contracts);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node[] { new ContractNode(key)};
        }
    */
}