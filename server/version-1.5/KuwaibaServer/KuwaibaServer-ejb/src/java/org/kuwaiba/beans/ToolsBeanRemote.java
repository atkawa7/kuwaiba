/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import javax.ejb.Remote;
import org.kuwaiba.exceptions.ServerSideException;

/**
 * Misc management tools
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Remote
public interface ToolsBeanRemote {
    /**
     * Resets/create admin account (sets it to user <b>admin</b> password <b>kuwaiba</b>)
     * @throws Exception 
     */
    public void resetAdmin() throws Exception;
    
    /**
     * load the initial data model 
     * @param dataModelFileAsByteArray The file to be processed, uploaded by the user
     * @throws ServerSideException 
     */
    public void loadDataModel(byte[] dataModelFileAsByteArray) throws ServerSideException;
    /**
     * Executes the selected paches
     * @param patches A string array with the ids of the patches to be executed.
     * @return The error messages (if any) after applying the patches. Null entries mean successful execution for the corresponding patches
     */
    public String[] executePatches(String[] patches);
}
