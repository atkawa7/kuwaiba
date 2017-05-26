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
package com.neotropic.inventory.modules.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Service for Projects Module
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectsModuleService {
    public static ResourceBundle bundle = ResourceBundle.getBundle("com/neotropic/inventory/modules/projects/Bundle");
    
    public ProjectsModuleService() {
        
    }
    
    public LocalPool getProjectRootPool() {
        LocalPool rootPool = CommunicationsStub.getInstance().getProjectsRootPool(Constants.CLASS_GENERICPROJECT);
        if (rootPool != null)
            return rootPool;
        else {
            NotificationUtil.getInstance().showSimplePopup("Error", 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        }
    }
    
    public static List<LocalObjectLight> getAllProjects() {
        LocalPool rootPool = CommunicationsStub.getInstance().getProjectsRootPool(Constants.CLASS_GENERICPROJECT);
        
        List<LocalObjectLight> mainProjects = CommunicationsStub.getInstance().getProjectsFromProjectsRootPool(rootPool.getOid(), -1);
            
        List<LocalObjectLight> result = new ArrayList();
        
        for (LocalObjectLight mainProject : mainProjects) {
            result.add(mainProject);
            result.addAll(getProjectsInProjects(mainProject.getClassName(), mainProject.getOid()));
        }
        return result;
    }
    
    private static List<LocalObjectLight> getProjectsInProjects(String projectClass, long projectId) {
        List<LocalObjectLight> subProjects = CommunicationsStub.getInstance().getProjectsFromProject(projectClass, projectId);
        
        List<LocalObjectLight> result = new ArrayList();
        
        for (LocalObjectLight subProject : subProjects) {
            result.add(subProject);
            result.addAll(getProjectsInProjects(subProject.getClassName(), subProject.getOid()));
        }
        
        return result;        
    }
}
