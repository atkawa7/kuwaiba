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

/**
 * Service for Projects Module
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectsModuleService {
    public static ResourceBundle bundle = ResourceBundle.getBundle("com/neotropic/inventory/modules/projects/Bundle");
    
    public ProjectsModuleService() {
    }
    
    public boolean isDataBaseUpdated() {
        return CommunicationsStub.getInstance().getMetaForClass(Constants.CLASS_GENERICPROJECT, true) == null ||
            CommunicationsStub.getInstance().getMetaForClass(Constants.CLASS_GENERICACTIVITY, true) == null;
    }
        
    public static List<LocalObjectLight> getAllProjects() {
        List<LocalPool> projectPools = CommunicationsStub.getInstance().getProjectPools();
        
        if (projectPools == null)
            return null;
        
        List<LocalObjectLight> result = new ArrayList<>();
        
        for (LocalPool projectPool : projectPools) {
            List<LocalObjectLight> projects = CommunicationsStub.getInstance().getPoolItems(projectPool.getOid());
            
            if (projects == null)
                continue;
            
            for (LocalObjectLight mainProject : projects)
                result.add(mainProject);
        }
        return result;
    }
}
