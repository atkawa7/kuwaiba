/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.util.patches;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.util.ChangeDescriptor;

/**
 * Patches the database so all users without group will be moved to a default group
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Patch03MigrateGroupLessUsers extends GenericPatch {
    
    public Patch03MigrateGroupLessUsers() {
    }

    @Override
    public String getId() {
        return "3";
    }

    @Override
    public String getTitle() {
        return "Migrate group-less users";
    }

    @Override
    public String getDescription() {
        return "All users without group will be moved to a default group so the new user management policies can be applied";
    }

    @Override
    public String getSourceVersion() {
        return "1.1.x";
    }

    @Override
    public String getTargetVersion() {
        return "1.5";
    }

    @Override
    public PatchResult executePatch() {
        PatchResult result = new PatchResult();
        result.setResultType(PatchResult.RESULT_SUCCESS);
        
        ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
                
        if (aem == null) {
            result.setResultType(PatchResult.RESULT_ERROR);
            return result;
        }
        
        try {
            List<UserProfile> allUsers = aem.getUsers();
            List<Long> usersToMove = new ArrayList<>();

            for (UserProfile user : allUsers) {
                if (aem.getGroupsForUser(user.getId()).isEmpty())
                    usersToMove.add(user.getId());
            }

            if (!usersToMove.isEmpty()) {
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd yyyy");
                String defaultGroupName = "Default Group " + formatter.format(Calendar.getInstance().getTime());

                aem.createGroup(defaultGroupName, "Default group created by the Migration Wizard", usersToMove);

                aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                        new ChangeDescriptor("reports", "", "", usersToMove.size() + " groups moved to " + defaultGroupName));
            }
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(ex.getMessage());
        }
        return result;
    }
    
    @Override
    public String getMandatory() {
        return "[Mandatory] ";
    }
    
}
