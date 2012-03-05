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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;

/**
 * Session bean to implement the logic for webservice calls
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class WebServiceBean implements WebServiceBeanRemote {

    private MetadataEntityManagerRemote mem;
    
    private MetadataEntityManagerRemote getMemInstance(){
        try{
            if (mem == null) {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                mem = (MetadataEntityManagerRemote) registry.lookup(MetadataEntityManagerRemote.REFERENCE_MEM);
            }
        }
        catch(Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
            mem = null;
        }
        return mem;
    }
}
