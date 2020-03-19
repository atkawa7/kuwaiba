/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.web;

import javax.jws.WebService;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@WebService(endpointInterface = "org.neotropic.kuwaiba.web.KuwaibaSoapWebService")
public class KuwaibaSoapWebServiceImpl implements KuwaibaSoapWebService {
    
    private PersistenceService ps;

    public KuwaibaSoapWebServiceImpl(PersistenceService ps) {
        this.ps = ps;
    }
    
    
    
    @Override
    public String sayHello(String user, String password) {
        try {
            Session aSession = ps.getAem().createSession(user, password, 2, "127.0.0.1");
            ps.getAem().closeSession(aSession.getToken(), "127.0.0.1");
            return "Success!";
        } catch (ApplicationObjectNotFoundException ex) {
            return "User not found";
        } catch (NotAuthorizedException ex) {
            return "User or password incorrect";
        }
    }

}
