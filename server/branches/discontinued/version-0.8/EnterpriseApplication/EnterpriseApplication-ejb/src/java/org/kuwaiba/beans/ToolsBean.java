/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>
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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;

/**
 * Session bean implementing
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class ToolsBean implements ToolsBeanRemote {
        
    @Override
    public void resetAdmin()  throws ServerSideException, NotAuthorizedException{
        
        try{
            PersistenceService.getInstance().getApplicationEntityManager().setUserProperties("admin",null, "kuwaiba", null, null, true, null, null, null, null);
        }catch(ApplicationObjectNotFoundException ex){ //If the user does not exist, create it
            try {
                PersistenceService.getInstance().getApplicationEntityManager().createUser("admin", "kuwaiba", "Radamel", "Falcao", true, null, null);
            }catch(InvalidArgumentException ie){
                throw new ServerSideException(Level.SEVERE, ie.getMessage());
            }
        }catch(InvalidArgumentException ex){
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public int[] executePatch() throws ServerSideException, NotAuthorizedException {
            return PersistenceService.getInstance().getApplicationEntityManager().executePatch();
    }
    
    @Override
    public boolean loadDataModel() throws ServerSideException{
        try{
            return PersistenceService.getInstance().getDataModelLoader().loadDataModel();
        }catch(DatabaseException ex){
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}