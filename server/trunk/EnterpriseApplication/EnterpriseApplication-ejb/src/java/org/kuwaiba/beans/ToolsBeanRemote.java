/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
     */
    public void resetAdmin() throws Exception;

    /**
     * Created the default groups
     * @throws ServerSideException
     */
    public void createDefaultGroups() throws ServerSideException;
    
    /**
     * Tries to retrieve the references to the remote objects exposed by the Persistence Service
     * @throws ServerSideException
     */
    public void connect() throws ServerSideException;
}
