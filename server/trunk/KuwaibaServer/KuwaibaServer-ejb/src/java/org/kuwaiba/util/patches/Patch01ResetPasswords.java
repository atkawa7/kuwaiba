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

import java.util.List;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.util.ChangeDescriptor;

/**
 * Patches the database will have their passwords reset to the same "username" value
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Patch01ResetPasswords extends GenericPatch {
    
    public Patch01ResetPasswords() {
    }

    @Override
    public String getId() {
        return "1";
    }

    @Override
    public String getTitle() {
        return "Reset passwords";
    }

    @Override
    public String getDescription() {
        return "All users in the database will have their passwords reset to the same &quot;username&quot; value. The passwords were stored as unsalted MD5 hashes until version 1.0 and in version 1.1 BCrypt is used instead";
    }

    @Override
    public String getSourceVersion() {
        return "1.0";
    }

    @Override
    public String getTargetVersion() {
        return "1.1.x";
    }

    @Override
    public PatchResult executePatch() {
        PatchResult result = new PatchResult();        
        ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
                
        if (aem == null) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add("The Persistence Service doesn't seem to be running. Passwords could no be reset.");
            return result;
        }
        
        try {
            //Reset passwords
            List<UserProfile> users = aem.getUsers();
            for (UserProfile user : users)
                aem.setUserProperties(user.getId(), null, user.getUserName(), //Sets the new password to the "username" value 
                        null, null, -1, UserProfile.USER_TYPE_GUI);

            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_MASSIVE_UPDATE_APPLICATION_OBJECT, 
                    new ChangeDescriptor("password", "", "", "Passwords reset due to security patch"));
            
            result.setResultType(PatchResult.RESULT_SUCCESS);
            
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
