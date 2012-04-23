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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;

/**
 * Session bean implementing
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class ToolsBean implements ToolsBeanRemote {
    private static ApplicationEntityManagerRemote aem;
    @Override
    public boolean resetAdmin() {
        try {
            List<Long> listGroups = new ArrayList<Long>();
            listGroups.add(getAEMInstance().createGroup("admins", "admins group"));
            listGroups.add(getAEMInstance().createGroup("users", "users group"));
            
            if (getAEMInstance().createUser("admin", "kuwaiba", "Administrator", "Dummy", true, null, listGroups) == null)
                return false;

            return true;
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (ObjectNotFoundException ex) {
            Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (RemoteException ex) {
            Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private static ApplicationEntityManagerRemote getAEMInstance(){
        if (aem == null){
            try{
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                aem = (ApplicationEntityManagerRemote) registry.lookup(ApplicationEntityManagerRemote.REFERENCE_AEM);
            }catch(Exception ex){
                Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE,
                        ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
                aem = null;
            }
        }
        return aem;
    }
}
