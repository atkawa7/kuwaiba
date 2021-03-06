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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;

/**
 * Session bean implementing
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class ToolsBean implements ToolsBeanRemote {
    private static ApplicationEntityManagerRemote aem;
    @Override
    public void resetAdmin()  throws ServerSideException{
        try{
            getAEMInstance().setUserProperties("admin",null, "kuwaiba", "Tyler", "Durden", true, null, null);
        }catch(ApplicationObjectNotFoundException ex){ //If the user does not exist, create it
            try{
                getAEMInstance().createUser("admin", "kuwaiba", "Tyler", "Durden", true, null, null);
            }catch(RemoteException re){
                throw new ServerSideException(Level.SEVERE, re.getMessage());
            }catch(InvalidArgumentException ie){
                throw new ServerSideException(Level.SEVERE, ie.getMessage());
            }
        }catch(RemoteException ex){
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }catch(InvalidArgumentException ex){
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
        
    }

    @Override
    public void createDefaultGroups() throws ServerSideException{
        try {
            getAEMInstance().createGroup("Administrators", "Administrators Group", null, null);
            getAEMInstance().createGroup("Users", "Standard Users Group", null, null);
        } catch (Exception ex) {
            Logger.getLogger(ToolsBean.class.getName()).log(Level.INFO, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    private static ApplicationEntityManagerRemote getAEMInstance(){
        if (aem == null){
            try{
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                aem = (ApplicationEntityManagerRemote) registry.lookup(ApplicationEntityManagerRemote.REFERENCE_AEM);
            }catch(Exception ex){
                Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE,
                        ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
                aem = null;
            }
        }
        return aem;
    }
}
