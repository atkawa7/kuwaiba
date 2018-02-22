/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.beans;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.util.patches.GenericPatch;
import org.kuwaiba.util.patches.GenericPatch.PatchResult;
import org.kuwaiba.util.patches.Patch01ResetPasswords;
import org.kuwaiba.util.patches.Patch02MigrateHardCodedReports;
import org.kuwaiba.util.patches.Patch03MigrateGroupLessUsers;
import org.kuwaiba.util.patches.Patch04ProjectsModuleSupport;
import org.kuwaiba.util.patches.Patch05RackViews;
import org.kuwaiba.util.patches.Patch06ConnectorMatching;
import org.kuwaiba.util.patches.Patch07PortTypeFix;
import org.kuwaiba.util.patches.Patch08DeviceLayout;
import org.kuwaiba.util.patches.Patch09EquipmentModel;

/**
 * Simple bean used to perform administrative tasks
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class ToolsBean implements ToolsBeanRemote {
    private List<GenericPatch> patches;
        
    @Override
    public void resetAdmin()  throws ServerSideException, NotAuthorizedException {
        
        try {
            PersistenceService.getInstance().getApplicationEntityManager().setUserProperties(UserProfile.DEFAULT_ADMIN,null, "kuwaiba", null, null, 1, UserProfile.USER_TYPE_GUI);
        }catch(ApplicationObjectNotFoundException ex){ //If the user does not exist the database might not be initialized, so display an error
            throw new ServerSideException("The user \"admin\" does not exist. Make sure you are using a database with a default schema.");
            
        } catch(InvalidArgumentException | IllegalStateException ex){
            throw new ServerSideException(ex.getMessage());
        }
        
    }
    
    @Override
    public void loadDataModel(byte[] dataModelFileAsByteArray) throws ServerSideException {
        try{
            PersistenceService.getInstance().getDataModelLoader().loadDataModel(dataModelFileAsByteArray);
        } catch (Exception ex) {
            throw new ServerSideException(ex.getMessage());
        }        
    }

    @Override
    public String[] executePatches(List<GenericPatch> patches) {
        String[] results = new String[patches.size()];
                
        for (int i = 0; i < patches.size(); i++) {
            PatchResult patchResult = patches.get(i).executePatch();
            for (String result : patchResult.getMessages())
                results[i] += result;
        }
        return results;
    }
    
    @Override
    public List<GenericPatch> getPatches() {
        if (patches == null) {
            patches = new ArrayList();            
            patches.add(new Patch01ResetPasswords());
            patches.add(new Patch02MigrateHardCodedReports());
            patches.add(new Patch03MigrateGroupLessUsers());
            patches.add(new Patch04ProjectsModuleSupport());
            patches.add(new Patch05RackViews());
            patches.add(new Patch06ConnectorMatching());
            patches.add(new Patch07PortTypeFix());
            patches.add(new Patch08DeviceLayout());
            patches.add(new Patch09EquipmentModel());
        }
        return patches;
    }
}